package org.alphakids.app.jugar

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.util.Size
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST
import androidx.camera.core.Preview
import androidx.camera.core.UseCase
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.viewmodel.compose.viewModel
import org.alphakids.app.components.AlphaPrimaryButton
import org.alphakids.app.game.domain.model.OcrPipelineConfig

/**
 * Android implementation of CameraView using CameraX + ML Kit OCR.
 *
 * Handles runtime camera permission request automatically.
 */
@Composable
actual fun CameraView(
    modifier: Modifier,
    onTextDetected: (String) -> Unit,
    onError: (String) -> Unit,
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    var hasCameraPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.CAMERA,
            ) == PackageManager.PERMISSION_GRANTED
        )
    }

    // Permission launcher
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
    ) { granted ->
        hasCameraPermission = granted
        if (!granted) {
            onError("Permiso de cámara denegado")
        }
    }

    // One OCR pipeline per scanner screen (scoped to its back stack entry):
    // rotating swaps the portrait/landscape layout, which disposes this
    // composable and creates a new one, but the recognizer is kept.
    val pipeline = viewModel { OcrPipeline() }
    val currentOnTextDetected by rememberUpdatedState(onTextDetected)
    val currentOnError by rememberUpdatedState(onError)

    DisposableEffect(pipeline) {
        val listener: (String) -> Unit = { currentOnTextDetected(it) }
        pipeline.onTextDetected = listener
        onDispose {
            if (pipeline.onTextDetected === listener) pipeline.onTextDetected = null
        }
    }

    if (!hasCameraPermission) {
        // Show permission request button
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center,
        ) {
            AlphaPrimaryButton(
                text = "Permitir cámara",
                onClick = { permissionLauncher.launch(Manifest.permission.CAMERA) },
            )
        }
    } else {
        val binding = remember(lifecycleOwner, pipeline) { CameraBinding(lifecycleOwner, pipeline) }
        DisposableEffect(binding) {
            onDispose { binding.release() }
        }

        // CameraX preview
        AndroidView(
            modifier = modifier.clip(RoundedCornerShape(20.dp)),
            factory = { ctx ->
                PreviewView(ctx).apply {
                    // SurfaceView (the PERFORMANCE default) composites in a
                    // separate hardware layer outside Compose's normal view
                    // ordering/clipping, which lets the live preview visually
                    // overlap sibling composables (e.g. the letter slots row)
                    // while it resizes. COMPATIBLE forces a TextureView,
                    // which respects Compose layout order and clipping.
                    implementationMode = PreviewView.ImplementationMode.COMPATIBLE
                    binding.bind(ctx, this) { message -> currentOnError(message) }
                }
            },
        )
    }
}

/**
 * The camera use cases bound by one [CameraView] instance. [release] clears
 * the analyzer and unbinds exactly these use cases when the preview leaves
 * composition (screen closed, or layout switched on rotation), so a stale
 * preview surface or analyzer never stays attached to the camera. Main
 * thread only.
 */
private class CameraBinding(
    private val lifecycleOwner: LifecycleOwner,
    private val pipeline: OcrPipeline,
) {
    private var provider: ProcessCameraProvider? = null
    private var analysis: ImageAnalysis? = null
    private var useCases: List<UseCase> = emptyList()
    private var released = false

    fun bind(context: Context, previewView: PreviewView, onError: (String) -> Unit) {
        val providerFuture = ProcessCameraProvider.getInstance(context)
        providerFuture.addListener({
            // The composable may already be gone when the provider is ready.
            if (released) return@addListener
            val cameraProvider = providerFuture.get()

            val preview = Preview.Builder().build().also {
                it.setSurfaceProvider(previewView.surfaceProvider)
            }

            val imageAnalysis = ImageAnalysis.Builder()
                .setTargetResolution(Size(OcrPipelineConfig.TARGET_WIDTH, OcrPipelineConfig.TARGET_HEIGHT))
                .setBackpressureStrategy(STRATEGY_KEEP_ONLY_LATEST)
                .build()
                .also { it.setAnalyzer(pipeline.analysisExecutor, pipeline.analyzer) }

            try {
                cameraProvider.unbindAll()
                cameraProvider.bindToLifecycle(
                    lifecycleOwner,
                    CameraSelector.DEFAULT_BACK_CAMERA,
                    preview,
                    imageAnalysis,
                )
                provider = cameraProvider
                analysis = imageAnalysis
                useCases = listOf(preview, imageAnalysis)
            } catch (exc: Exception) {
                imageAnalysis.clearAnalyzer()
                onError("Error al iniciar la cámara: ${exc.message}")
            }
        }, ContextCompat.getMainExecutor(context))
    }

    fun release() {
        released = true
        analysis?.clearAnalyzer()
        provider?.unbind(*useCases.toTypedArray())
        analysis = null
        provider = null
        useCases = emptyList()
    }
}
