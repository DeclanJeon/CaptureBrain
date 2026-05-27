package com.ponslink.capturebrain.data

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "capture_items",
    indices = [
        Index(value = ["mediaStoreId"], unique = true),
        Index(value = ["imageHash"], unique = true),
        Index(value = ["status"]),
        Index(value = ["capturedAtMillis"])
    ]
)
data class CaptureItemEntity(
    @PrimaryKey val id: String,
    val mediaStoreId: String,
    val imageUri: String,
    val imageHash: String,
    val fileName: String,
    val capturedAtMillis: Long,
    val detectedAtMillis: Long,
    val status: ProcessingStatus,
    val sourceApp: String? = null,
    val ocrText: String? = null,
    val layoutText: String? = null,
    val ocrConfidence: Float? = null,
    val title: String? = null,
    val summary: String? = null,
    val category: String? = null,
    val subcategory: String? = null,
    val tags: List<String> = emptyList(),
    val entitiesJson: String? = null,
    val isSensitive: Boolean = false,
    val sensitivityReason: String? = null,
    val markdown: String? = null,
    val driveImageFileId: String? = null,
    val driveMarkdownFileId: String? = null,
    val driveFolderId: String? = null,
    val errorMessage: String? = null,
    val retryCount: Int = 0,
    val updatedAtMillis: Long = detectedAtMillis
)
