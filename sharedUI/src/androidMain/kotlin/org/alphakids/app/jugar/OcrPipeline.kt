package org.alphakids.app.jugar

import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.lifecycle.ViewModel
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import kotlin.concurrent.Volatile
import org.alphakids.app.game.domain.model.OcrFrameGate
import org.alphakids.app.game.domain.model.OcrFrameStats

/**
 * The single owner of the OCR resources of one scanner screen: the ML Kit
 * recognizer (native memory), the camera analysis thread and the session
 * stats.
 *
 * Scoped to the scanner's navigation back stack entry, so it outlives the
 * portrait/landscape layout switch (which disposes and recreates the camera
 * preview composable) and is cleared exactly once, when the screen leaves
 * the back stack. The camera use cases are bound to that same entry's
 * lifecycle and unbound by [CameraView] when it leaves composition, so no
 * frame reaches this pipeline after it is cleared.
 */
internal class OcrPipeline : ViewModel() {

    private val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
    private val mainHandler = Handler(Looper.getMainLooper())
    private val stats = OcrFrameStats()

    /** Camera analysis thread; frames are analyzed one at a time. */
    val analysisExecutor: ExecutorService = Executors.newSingleThreadExecutor()

    /** Receives each non-empty read on the main thread. Main thread only. */
    var onTextDetected: ((String) -> Unit)? = null

    val analyzer = TextRecognitionAnalyzer(
        recognizer = recognizer,
        gate = OcrFrameGate(),
        stats = stats,
    ) { read ->
        mainHandler.post { onTextDetected?.invoke(read) }
    }

    override fun onCleared() {
        mainHandler.removeCallbacksAndMessages(null)
        onTextDetected = null
        recognizer.close()
        analysisExecutor.shutdown()

        val snapshot = stats.snapshot
        OcrPipelineMetrics.lastSession = snapshot
        // Numbers only — the recognized text is never logged.
        Log.d(
            TAG,
            "session analyzed=${snapshot.framesAnalyzed} skipped=${snapshot.framesSkipped} " +
                "failed=${snapshot.framesFailed} meanMs=${snapshot.meanLatencyMs} maxMs=${snapshot.maxLatencyMs}",
        )
    }

    private companion object {
        const val TAG = "OcrPipeline"
    }
}

/**
 * Stats of the last finished scanner session, readable from a debugger or a
 * benchmark harness. Per-frame latency and frame counters are also emitted
 * as the `ocr.recognize` trace section and the `ocr.frames.analyzed` /
 * `ocr.frames.skipped` counters for Perfetto.
 */
object OcrPipelineMetrics {
    @Volatile
    var lastSession: OcrFrameStats.Snapshot? = null
        internal set
}
