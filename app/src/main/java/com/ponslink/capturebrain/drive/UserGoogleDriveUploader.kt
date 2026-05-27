package com.ponslink.capturebrain.drive

import android.content.Context
import com.google.api.client.extensions.android.http.AndroidHttp
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential
import com.google.api.client.http.ByteArrayContent
import com.google.api.client.http.InputStreamContent
import com.google.api.client.json.gson.GsonFactory
import com.google.api.services.drive.Drive
import com.google.api.services.drive.DriveScopes
import com.google.api.services.drive.model.File as DriveFile
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class UserGoogleDriveUploader(
    private val context: Context,
    private val accountName: String,
    private val rootFolderName: String = DriveAccountStore.DEFAULT_ROOT_FOLDER_NAME
) : DriveUploader {
    private val appContext = context.applicationContext

    private val drive: Drive by lazy {
        val credential = GoogleAccountCredential.usingOAuth2(
            appContext,
            listOf(DriveScopes.DRIVE_FILE)
        ).apply {
            selectedAccountName = accountName
        }

        Drive.Builder(
            AndroidHttp.newCompatibleTransport(),
            GsonFactory.getDefaultInstance(),
            credential
        )
            .setApplicationName("CaptureBrain")
            .build()
    }

    override suspend fun upload(bundle: UploadBundle): Result<UploadResult> = withContext(Dispatchers.IO) {
        runCatching {
            require(accountName.isNotBlank()) { "Google Drive account is not connected." }

            val folderId = ensureFolderPath(listOf(rootFolderName) + bundle.folderPath.pathSegments())
            val imageFileId = uploadImage(folderId, bundle)
            val markdownFileId = uploadTextFile(
                folderId = folderId,
                fileName = bundle.markdownFileName,
                mimeType = "text/markdown; charset=utf-8",
                body = bundle.markdown
            )
            val metadataFileId = uploadTextFile(
                folderId = folderId,
                fileName = bundle.metadataFileName,
                mimeType = "application/json; charset=utf-8",
                body = bundle.metadataJson
            )

            UploadResult(
                imageFileId = imageFileId,
                markdownFileId = markdownFileId,
                folderId = folderId,
                metadataFileId = metadataFileId
            )
        }
    }

    private fun ensureFolderPath(segments: List<String>): String {
        var parentId = DRIVE_ROOT
        segments
            .map { it.trim().safeDriveName() }
            .filter { it.isNotEmpty() }
            .forEach { folderName ->
                parentId = findOrCreateFolder(folderName, parentId)
            }
        return parentId
    }

    private fun findOrCreateFolder(name: String, parentId: String): String {
        val query = "mimeType='$FOLDER_MIME_TYPE' " +
            "and name='${name.escapeDriveQuery()}' " +
            "and '${parentId.escapeDriveQuery()}' in parents " +
            "and trashed=false"

        val existing = drive.files().list()
            .setSpaces("drive")
            .setQ(query)
            .setFields("files(id,name)")
            .setPageSize(1)
            .execute()
            .files
            .firstOrNull()

        if (existing != null) return existing.id

        val metadata = DriveFile().apply {
            this.name = name
            mimeType = FOLDER_MIME_TYPE
            parents = listOf(parentId)
        }

        return drive.files().create(metadata)
            .setFields("id")
            .execute()
            .id
    }

    private fun uploadImage(folderId: String, bundle: UploadBundle): String {
        val mimeType = appContext.contentResolver.getType(bundle.imageUri) ?: "image/png"
        val inputStream = appContext.contentResolver.openInputStream(bundle.imageUri)
            ?: error("Cannot open image uri: ${bundle.imageUri}")

        val metadata = DriveFile().apply {
            name = bundle.imageFileName.safeDriveName()
            parents = listOf(folderId)
        }

        return inputStream.use { stream ->
            drive.files().create(metadata, InputStreamContent(mimeType, stream))
                .setFields("id")
                .execute()
                .id
        }
    }

    private fun uploadTextFile(folderId: String, fileName: String, mimeType: String, body: String): String {
        val metadata = DriveFile().apply {
            name = fileName.safeDriveName()
            parents = listOf(folderId)
        }
        val content = ByteArrayContent(mimeType, body.toByteArray(Charsets.UTF_8))

        return drive.files().create(metadata, content)
            .setFields("id")
            .execute()
            .id
    }

    private fun String.pathSegments(): List<String> = split('/')
        .map { it.trim() }
        .filter { it.isNotBlank() }

    private fun String.safeDriveName(): String = replace(Regex("[\\u0000-\\u001f]"), " ").trim().ifBlank { "Untitled" }

    private fun String.escapeDriveQuery(): String = replace("\\", "\\\\").replace("'", "\\'")

    companion object {
        private const val DRIVE_ROOT = "root"
        private const val FOLDER_MIME_TYPE = "application/vnd.google-apps.folder"
    }
}
