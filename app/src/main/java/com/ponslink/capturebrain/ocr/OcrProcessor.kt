package com.ponslink.capturebrain.ocr

import android.net.Uri

data class RecognizedTextBlock(
    val text: String,
    val left: Int,
    val top: Int,
    val right: Int,
    val bottom: Int
)

data class OcrResult(
    val originalText: String,
    val layoutPreservedText: String,
    val confidence: Float?,
    val blocks: List<RecognizedTextBlock>
)

interface OcrProcessor {
    suspend fun recognize(imageUri: Uri): Result<OcrResult>
}
