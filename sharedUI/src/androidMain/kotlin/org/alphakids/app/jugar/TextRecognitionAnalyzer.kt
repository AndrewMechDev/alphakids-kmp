package org.alphakids.app.jugar

import android.os.Build
import android.os.Trace
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import com.google.android.gms.tasks.Tasks
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognizer
import java.util.concurrent.TimeUnit
import kotlin.time.TimeSource
import org.alphakids.app.game.domain.model.OcrFrameGate
import org.alphakids.app.game.domain.model.OcrFrameStats

/**
 * CameraX analyzer that feeds the frames let through by [gate] to the ML Kit
 * [recognizer], one at a time, on the camera analysis thread.
 *
 * Recognition is awaited synchronously: the analysis thread is a dedicated
 * single-thread executor, so blocking it serializes recognitions, keeps the
 * gate and stats single-threaded, and lets the `ocr.recognize` trace section
 * begin and end on the same thread (a Perfetto/systrace requirement). Every
 * [ImageProxy] is closed on every path — skipped, failed or recognized — or
 * CameraX would stop delivering frames.
 *
 * Does not own [recognizer]: [OcrPipeline] creates and closes it.
 *
 * @param onTextDetected Called on the analysis thread with each non-empty
 *   read, trimmed and uppercased. The recognized text is never logged.
 */
internal class TextRecognitionAnalyzer(
    private val recognizer: TextRecognizer,
    private val gate: OcrFrameGate,
    private val stats: OcrFrameStats,
    private val onTextDetected: (String) -> Unit,
) : ImageAnalysis.Analyzer {

    @OptIn(ExperimentalGetImage::class)
    override fun analyze(imageProxy: ImageProxy) {
        if (!gate.tryAcquire()) {
            stats.recordSkipped()
            imageProxy.close()
            publishCounters()
            return
        }
        val start = TimeSource.Monotonic.markNow()
        Trace.beginSection(TRACE_RECOGNIZE)
        try {
            val mediaImage = imageProxy.image
            if (mediaImage == null) {
                stats.recordFailed()
                return
            }
            val input = InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)
            val text = Tasks.await(recognizer.process(input), RECOGNITION_TIMEOUT_MS, TimeUnit.MILLISECONDS).text
            stats.recordRecognized(start.elapsedNow())

            val read = text.trim().uppercase()
            if (read.isNotEmpty()) onTextDetected(read)
        } catch (interrupted: InterruptedException) {
            stats.recordFailed()
            Thread.currentThread().interrupt()
        } catch (failure: Exception) {
            // ExecutionException / TimeoutException, or a recognizer already
            // closed because the screen is going away: drop this frame.
            stats.recordFailed()
        } finally {
            Trace.endSection()
            gate.release()
            imageProxy.close()
            publishCounters()
        }
    }

    private fun publishCounters() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val snapshot = stats.snapshot
            Trace.setCounter(COUNTER_ANALYZED, snapshot.framesAnalyzed.toLong())
            Trace.setCounter(COUNTER_SKIPPED, snapshot.framesSkipped.toLong())
        }
    }

    private companion object {
        const val TRACE_RECOGNIZE = "ocr.recognize"
        const val COUNTER_ANALYZED = "ocr.frames.analyzed"
        const val COUNTER_SKIPPED = "ocr.frames.skipped"

        /**
         * Safety bound only, not a tuning value: a recognition that never
         * completes must not pin the analysis thread forever.
         */
        const val RECOGNITION_TIMEOUT_MS = 2_000L
    }
}
