package com.ponslink.capturebrain.core

import android.content.ContentUris
import android.content.Context
import android.provider.MediaStore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class MediaStoreScreenshotScanner(
    private val context: Context
) : ScreenshotDetector {
    override suspend fun scanRecent(daysBack: Int): List<DetectedScreenshot> = withContext(Dispatchers.IO) {
        val sinceSeconds = (System.currentTimeMillis() / 1000L) - daysBack.coerceAtLeast(1) * 86_400L
        val projection = arrayOf(
            MediaStore.Images.Media._ID,
            MediaStore.Images.Media.DISPLAY_NAME,
            MediaStore.Images.Media.DATE_ADDED,
            MediaStore.Images.Media.SIZE,
            MediaStore.Images.Media.RELATIVE_PATH
        )
        val selection = "${MediaStore.Images.Media.DATE_ADDED} >= ?"
        val sort = "${MediaStore.Images.Media.DATE_ADDED} DESC"
        val result = mutableListOf<DetectedScreenshot>()
        context.contentResolver.query(
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
            projection,
            selection,
            arrayOf(sinceSeconds.toString()),
            sort
        )?.use { cursor ->
            val idColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media._ID)
            val nameColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DISPLAY_NAME)
            val dateColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATE_ADDED)
            val sizeColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.SIZE)
            val pathColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.RELATIVE_PATH)
            while (cursor.moveToNext()) {
                val id = cursor.getLong(idColumn)
                val name = cursor.getString(nameColumn).orEmpty()
                val relativePath = cursor.getString(pathColumn)
                if (!looksLikeScreenshot(name, relativePath)) continue
                val dateAddedSeconds = cursor.getLong(dateColumn)
                val size = cursor.getLong(sizeColumn)
                val uri = ContentUris.withAppendedId(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, id)
                result += DetectedScreenshot(
                    mediaStoreId = id.toString(),
                    uri = uri,
                    displayName = name.ifBlank { "screenshot-$id.png" },
                    capturedAtMillis = dateAddedSeconds * 1000L,
                    sizeBytes = size,
                    relativePath = relativePath,
                    imageHashHint = "$id:$dateAddedSeconds:$size"
                )
            }
        }
        result
    }

    private fun looksLikeScreenshot(name: String, relativePath: String?): Boolean {
        val value = listOfNotNull(name, relativePath).joinToString("/").lowercase()
        return "screenshot" in value || "screenshots" in value || "스크린샷" in value
    }
}
