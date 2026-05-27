package com.ponslink.capturebrain.core

import com.ponslink.capturebrain.data.CaptureItemEntity
import com.ponslink.capturebrain.ocr.OcrResult
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MarkdownGenerator {
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US)

    fun generate(item: CaptureItemEntity, ocr: OcrResult, analysis: AnalysisResult): String {
        val capturedAt = dateFormat.format(Date(item.capturedAtMillis))
        val processedAt = dateFormat.format(Date())
        return buildString {
            appendLine("# ${analysis.title.escapeMarkdownTitle()}")
            appendLine()
            appendLine("- Captured at: $capturedAt")
            appendLine("- Processed at: $processedAt")
            appendLine("- Source app: ${item.sourceApp ?: "unknown"}")
            appendLine("- Category: ${analysis.category}")
            appendLine("- Subcategory: ${analysis.subcategory}")
            appendLine("- Language: auto")
            appendLine("- OCR confidence: ${ocr.confidence ?: "unknown"}")
            appendLine("- Original image: ${item.fileName}")
            appendLine("- Drive folder: ${analysis.folderPath}")
            appendLine()
            appendLine("---")
            appendLine()
            appendLine("## Original Text")
            appendLine()
            appendLine(ocr.originalText.ifBlank { "(empty OCR result)" })
            appendLine()
            appendLine("---")
            appendLine()
            appendLine("## Layout-Preserved Notes")
            appendLine()
            appendLine(ocr.layoutPreservedText.ifBlank { "(layout text unavailable)" })
            appendLine()
            appendLine("---")
            appendLine()
            appendLine("## Summary")
            appendLine()
            appendLine(analysis.summary)
            appendLine()
            appendLine("---")
            appendLine()
            appendLine("## Key Points")
            appendLine()
            analysis.keyPoints.ifEmpty { listOf("No key points extracted") }
                .forEach { appendLine("- $it") }
            appendLine()
            appendLine("---")
            appendLine()
            appendLine("## Entities")
            appendLine()
            appendLine("```json")
            appendLine(analysis.entitiesJson)
            appendLine("```")
            appendLine()
            appendLine("---")
            appendLine()
            appendLine("## Tags")
            appendLine()
            appendLine(analysis.tags.joinToString(" ") { "#" + it.replace(" ", "-") })
            appendLine()
            appendLine("---")
            appendLine()
            appendLine("## Processing Metadata")
            appendLine()
            appendLine("```json")
            appendLine("{")
            appendLine("  \"captureItemId\": ${item.id.quoteJson()},")
            appendLine("  \"mediaStoreId\": ${item.mediaStoreId.quoteJson()},")
            appendLine("  \"sensitive\": ${analysis.sensitivity.isSensitive},")
            appendLine("  \"sensitivityReason\": ${analysis.sensitivity.reason?.quoteJson() ?: "null"}")
            appendLine("}")
            appendLine("```")
        }
    }

    private fun String.escapeMarkdownTitle(): String = replace("\n", " ").trim().ifBlank { "Untitled Screenshot" }

    private fun String.quoteJson(): String = buildString {
        append('"')
        this@quoteJson.forEach { char ->
            when (char) {
                '\\' -> append("\\\\")
                '"' -> append("\\\"")
                '\n' -> append("\\n")
                '\r' -> append("\\r")
                '\t' -> append("\\t")
                else -> append(char)
            }
        }
        append('"')
    }
}
