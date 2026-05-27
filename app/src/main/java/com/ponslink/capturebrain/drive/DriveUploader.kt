package com.ponslink.capturebrain.drive

import android.net.Uri

data class UploadBundle(
    val imageUri: Uri,
    val imageFileName: String,
    val markdownFileName: String,
    val markdown: String,
    val folderPath: String,
    val metadataFileName: String = "metadata.json",
    val metadataJson: String = "{}"
)

data class UploadResult(
    val imageFileId: String,
    val markdownFileId: String,
    val folderId: String,
    val metadataFileId: String? = null
)

interface DriveUploader {
    suspend fun upload(bundle: UploadBundle): Result<UploadResult>
}

class UnconnectedDriveUploader : DriveUploader {
    override suspend fun upload(bundle: UploadBundle): Result<UploadResult> = Result.failure(
        IllegalStateException("Google Drive 계정이 연결되지 않았습니다. 설정에서 Google 로그인과 Drive 권한을 먼저 연결하세요.")
    )
}
