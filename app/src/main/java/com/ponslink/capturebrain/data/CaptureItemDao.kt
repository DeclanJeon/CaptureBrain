package com.ponslink.capturebrain.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface CaptureItemDao {
    @Query("SELECT * FROM capture_items ORDER BY capturedAtMillis DESC LIMIT :limit")
    fun recent(limit: Int = 50): Flow<List<CaptureItemEntity>>

    @Query("SELECT * FROM capture_items WHERE id = :id LIMIT 1")
    suspend fun findById(id: String): CaptureItemEntity?

    @Query("SELECT * FROM capture_items WHERE mediaStoreId = :mediaStoreId LIMIT 1")
    suspend fun findByMediaStoreId(mediaStoreId: String): CaptureItemEntity?

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertIgnore(item: CaptureItemEntity): Long

    @Query("SELECT * FROM capture_items WHERE status IN (:statuses) ORDER BY detectedAtMillis ASC LIMIT :limit")
    suspend fun nextByStatuses(statuses: List<ProcessingStatus>, limit: Int): List<CaptureItemEntity>

    @Query("UPDATE capture_items SET status = :status, errorMessage = :errorMessage, updatedAtMillis = :updatedAtMillis WHERE id = :id")
    suspend fun updateStatus(id: String, status: ProcessingStatus, errorMessage: String? = null, updatedAtMillis: Long = System.currentTimeMillis())

    @Query("""
        UPDATE capture_items
        SET status = :status,
            ocrText = :ocrText,
            layoutText = :layoutText,
            ocrConfidence = :ocrConfidence,
            title = :title,
            summary = :summary,
            category = :category,
            subcategory = :subcategory,
            tags = :tags,
            entitiesJson = :entitiesJson,
            isSensitive = :isSensitive,
            sensitivityReason = :sensitivityReason,
            markdown = :markdown,
            updatedAtMillis = :updatedAtMillis
        WHERE id = :id
    """)
    suspend fun updateAnalysis(
        id: String,
        status: ProcessingStatus,
        ocrText: String?,
        layoutText: String?,
        ocrConfidence: Float?,
        title: String?,
        summary: String?,
        category: String?,
        subcategory: String?,
        tags: List<String>,
        entitiesJson: String?,
        isSensitive: Boolean,
        sensitivityReason: String?,
        markdown: String?,
        updatedAtMillis: Long = System.currentTimeMillis()
    )

    @Query("""
        UPDATE capture_items
        SET status = :status,
            driveImageFileId = :driveImageFileId,
            driveMarkdownFileId = :driveMarkdownFileId,
            driveFolderId = :driveFolderId,
            errorMessage = :errorMessage,
            updatedAtMillis = :updatedAtMillis
        WHERE id = :id
    """)
    suspend fun updateUploadResult(
        id: String,
        status: ProcessingStatus,
        driveImageFileId: String?,
        driveMarkdownFileId: String?,
        driveFolderId: String?,
        errorMessage: String?,
        updatedAtMillis: Long = System.currentTimeMillis()
    )

    @Query("UPDATE capture_items SET status = 'QUEUED', retryCount = retryCount + 1, errorMessage = NULL, updatedAtMillis = :updatedAtMillis WHERE status IN ('FAILED_OCR', 'FAILED_UPLOAD', 'PROCESSING', 'UPLOADING')")
    suspend fun retryFailed(updatedAtMillis: Long = System.currentTimeMillis()): Int
}
