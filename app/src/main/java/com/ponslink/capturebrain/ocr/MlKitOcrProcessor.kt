package com.ponslink.capturebrain.ocr

import android.content.Context
import android.net.Uri
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.korean.KoreanTextRecognizerOptions
import kotlinx.coroutines.tasks.await

class MlKitOcrProcessor(
    private val context: Context
) : OcrProcessor {
    private val recognizer = TextRecognition.getClient(
        KoreanTextRecognizerOptions.Builder().build()
    )

    override suspend fun recognize(imageUri: Uri): Result<OcrResult> = runCatching {
        val input = InputImage.fromFilePath(context, imageUri)
        val text = recognizer.process(input).await()
        val blocks = text.textBlocks.mapNotNull { block ->
            val box = block.boundingBox ?: return@mapNotNull null
            RecognizedTextBlock(
                text = block.text,
                left = box.left,
                top = box.top,
                right = box.right,
                bottom = box.bottom
            )
        }
        OcrResult(
            originalText = text.text,
            layoutPreservedText = blocks.sortedWith(compareBy<RecognizedTextBlock> { it.top }.thenBy { it.left })
                .joinToString(separator = "\n") { it.text },
            confidence = null,
            blocks = blocks
        )
    }
}
