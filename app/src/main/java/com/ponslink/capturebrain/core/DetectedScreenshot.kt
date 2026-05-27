package com.ponslink.capturebrain.core

import android.net.Uri

data class DetectedScreenshot(
    val mediaStoreId: String,
    val uri: Uri,
    val displayName: String,
    val capturedAtMillis: Long,
    val sizeBytes: Long,
    val relativePath: String?,
    val imageHashHint: String
)
