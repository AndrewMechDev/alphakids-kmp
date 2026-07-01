package org.alphakids.app.camera

import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions

/**
 * [ImageAnalysis.Analyzer] that processes camera frames with ML Kit
 * on-device Text Recognition. Fires [onTextDetected] when non-empty
 * text is recognized.
 */
class TextRecognitionAnalyzer(
    private val onTextDetected: (String) -> Unit,
) : ImageAnalysis.Analyzer {

    private val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)

    @OptIn(ExperimentalGetImage::class)
    override fun analyze(imageProxy: ImageProxy) {
        val mediaImage = imageProxy.image
        if (mediaImage != null) {
            val inputImage = InputImage.fromMediaImage(
                mediaImage,
                imageProxy.imageInfo.rotationDegrees,
            )
            recognizer.process(inputImage)
                .addOnSuccessListener { result ->
                    val text = result.text.trim().uppercase()
                    if (text.isNotEmpty()) {
                        onTextDetected(text)
                    }
                }
                .addOnCompleteListener {
                    imageProxy.close()
                }
        } else {
            imageProxy.close()
        }
    }
}
