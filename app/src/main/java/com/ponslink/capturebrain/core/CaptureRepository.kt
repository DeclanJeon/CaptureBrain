package com.ponslink.capturebrain.core

import android.net.Uri
import com.ponslink.capturebrain.data.CaptureItemDao
import com.ponslink.capturebrain.data.CaptureItemEntity
import com.ponslink.capturebrain.data.ProcessingStatus
import com.ponslink.capturebrain.drive.DriveUploader
import com.ponslink.capturebrain.drive.UploadBundle
import com.ponslink.capturebrain.ocr.OcrProcessor
import java.util.UUID

class CaptureRepository(
    private val dao: CaptureItemDao,
    private val screenshotDetector: ScreenshotDetector,
    private val ocrProcessor: OcrProcessor,
    private val driveUploader: DriveUploader,
    private val analyzer: LocalAnalyzer = LocalAnalyzer(),
    private val markdownGenerator: MarkdownGenerator = MarkdownGenerator()
) {
    suspend fun importRecentScreenshots(daysBack: Int = 1): Int {
        var inserted = 0
        screenshotDetector.scanRecent(daysBack).forEach { detected ->
            val item = CaptureItemEntity(
                id = UUID.randomUUID().toString(),
                mediaStoreId = detected.mediaStoreId,
                imageUri = detected.uri.toString(),
                imageHash = detected.imageHashHint,
                fileName = detected.displayName,
                capturedAtMillis = detected.capturedAtMillis,
                detectedAtMillis = System.currentTimeMillis(),
                status = ProcessingStatus.QUEUED
            )
            if (dao.insertIgnore(item) > 0) inserted++
        }
        return inserted
    }

    suspend fun retryFailed(): Int = dao.retryFailed()

    suspend fun processNext(limit: Int = 1): Int {
        val nextItems = dao.nextByStatuses(
            statuses = listOf(ProcessingStatus.DETECTED, ProcessingStatus.QUEUED, ProcessingStatus.FAILED_UPLOAD),
            limit = limit
        )
        nextItems.forEach { processItem(it) }
        return nextItems.size
    }

    suspend fun processItem(item: CaptureItemEntity) {
        dao.updateStatus(item.id, ProcessingStatus.PROCESSING)
        val ocr = ocrProcessor.recognize(Uri.parse(item.imageUri)).getOrElse { error ->
            dao.updateStatus(item.id, ProcessingStatus.FAILED_OCR, error.message ?: "OCR failed")
            return
        }
        val analysis = analyzer.analyze(ocr.originalText)
        val markdown = markdownGenerator.generate(item, ocr, analysis)
        val nextStatus = if (analysis.sensitivity.isSensitive) ProcessingStatus.SENSITIVE_PENDING else ProcessingStatus.MARKDOWN_CREATED
        dao.updateAnalysis(
            id = item.id,
            status = nextStatus,
            ocrText = ocr.originalText,
            layoutText = ocr.layoutPreservedText,
            ocrConfidence = ocr.confidence,
            title = analysis.title,
            summary = analysis.summary,
            category = analysis.category,
            subcategory = analysis.subcategory,
            tags = analysis.tags,
            entitiesJson = analysis.entitiesJson,
            isSensitive = analysis.sensitivity.isSensitive,
            sensitivityReason = analysis.sensitivity.reason,
            markdown = markdown
        )
        if (!analysis.sensitivity.isSensitive) upload(item.copy(markdown = markdown), analysis.folderPath)
    }

    suspend fun upload(item: CaptureItemEntity, folderPath: String) {
        val markdown = item.markdown ?: run {
            dao.updateStatus(item.id, ProcessingStatus.FAILED_UPLOAD, "Markdown is missing")
            return
        }
        dao.updateStatus(item.id, ProcessingStatus.UPLOADING)
        val result = driveUploader.upload(
            UploadBundle(
                imageUri = Uri.parse(item.imageUri),
                imageFileName = item.fileName,
                markdownFileName = item.fileName.substringBeforeLast('.', item.fileName) + ".md",
                markdown = markdown,
                folderPath = folderPath,
                metadataJson = buildMetadataJson(item, folderPath)
            )
        )
        result.fold(
            onSuccess = { uploaded ->
                dao.updateUploadResult(
                    id = item.id,
                    status = ProcessingStatus.COMPLETED,
                    driveImageFileId = uploaded.imageFileId,
                    driveMarkdownFileId = uploaded.markdownFileId,
                    driveFolderId = uploaded.folderId,
                    errorMessage = null
                )
            },
            onFailure = { error ->
                val details = listOfNotNull(
                    error::class.simpleName,
                    error.message,
                    error.cause?.message
                ).joinToString(": ").ifBlank { "Drive upload failed" }
                dao.updateUploadResult(
                    id = item.id,
                    status = ProcessingStatus.FAILED_UPLOAD,
                    driveImageFileId = null,
                    driveMarkdownFileId = null,
                    driveFolderId = null,
                    errorMessage = details
                )
            }
        )
    }

    private fun buildMetadataJson(item: CaptureItemEntity, folderPath: String): String = """
        {
          "id": "${item.id}",
          "mediaStoreId": "${item.mediaStoreId}",
          "fileName": "${item.fileName}",
          "capturedAtMillis": ${item.capturedAtMillis},
          "folderPath": "${folderPath}",
          "title": ${item.title?.jsonString() ?: "null"},
          "category": ${item.category?.jsonString() ?: "null"},
          "subcategory": ${item.subcategory?.jsonString() ?: "null"},
          "isSensitive": ${item.isSensitive}
        }
    """.trimIndent()

    private fun String.jsonString(): String = buildString {
        append('"')
        this@jsonString.forEach { char ->
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
