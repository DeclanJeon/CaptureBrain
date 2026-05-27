package com.ponslink.capturebrain

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.lifecycleScope
import com.google.android.gms.auth.UserRecoverableAuthException
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.common.api.Scope
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential
import com.google.api.client.googleapis.extensions.android.gms.auth.UserRecoverableAuthIOException
import com.google.api.services.drive.DriveScopes
import com.ponslink.capturebrain.core.ScreenshotContentObserver
import com.ponslink.capturebrain.data.CaptureBrainDatabase
import com.ponslink.capturebrain.data.CaptureItemEntity
import com.ponslink.capturebrain.data.ProcessingStatus
import com.ponslink.capturebrain.drive.DriveAccountStore
import com.ponslink.capturebrain.settings.CaptureSettingsStore
import com.ponslink.capturebrain.ui.CaptureBrainApp
import com.ponslink.capturebrain.ui.model.CaptureItemUi
import com.ponslink.capturebrain.ui.model.CaptureStatus
import com.ponslink.capturebrain.ui.model.QueueSummaryUi
import com.ponslink.capturebrain.ui.theme.CaptureBrainTheme
import com.ponslink.capturebrain.worker.CaptureProcessWorker
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MainActivity : ComponentActivity() {
    private lateinit var accountStore: DriveAccountStore
    private lateinit var settingsStore: CaptureSettingsStore
    private var screenshotObserver: ScreenshotContentObserver? = null
    private var connectedDriveAccount by mutableStateOf<String?>(null)
    private var driveConnectionError by mutableStateOf<String?>(null)
    private var imagePermissionGranted by mutableStateOf(false)
    private var imagePermissionDenied by mutableStateOf(false)
    private var pendingDriveAccountName: String? = null

    private val imagePermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        imagePermissionGranted = granted
        imagePermissionDenied = !granted
        if (granted) {
            startAutomationFromSettings()
        } else {
            screenshotObserver?.unregister()
            screenshotObserver = null
            CaptureProcessWorker.cancelPeriodicScan(this)
        }
    }

    private val googleDriveSignInLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
        try {
            val account = task.getResult(ApiException::class.java)
            val accountName = account.email
            if (accountName.isNullOrBlank()) {
                driveConnectionError = "Google 계정 이메일을 확인할 수 없습니다."
            } else {
                completeDriveConnection(accountName)
            }
        } catch (error: ApiException) {
            driveConnectionError = "Google Drive 연결 실패: ${error.statusCode}"
        }
    }

    private val driveConsentLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) {
        pendingDriveAccountName?.let { accountName -> completeDriveConnection(accountName) }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        accountStore = DriveAccountStore(this)
        settingsStore = CaptureSettingsStore(this)
        connectedDriveAccount = accountStore.getAccountName()
        imagePermissionGranted = hasImageReadPermission()
        imagePermissionDenied = false
        startAutomationFromSettings()
        setContent {
            val recentItems by CaptureBrainDatabase.get(this)
                .captureItemDao()
                .recent(50)
                .collectAsStateWithLifecycle(emptyList())
            val captureItems = recentItems.map { it.toUi() }
            val settings by settingsStore.settingsFlow.collectAsStateWithLifecycle(settingsStore.blockingSnapshot())
            CaptureBrainTheme {
                CaptureBrainApp(
                    connectedDriveAccount = connectedDriveAccount,
                    driveConnectionError = driveConnectionError,
                    imagePermissionGranted = imagePermissionGranted,
                    imagePermissionDenied = imagePermissionDenied,
                    rootFolderName = settings.rootFolderName,
                    summary = recentItems.toQueueSummary(),
                    captures = captureItems,
                    onRootFolderChange = { name ->
                        lifecycleScope.launch {
                            settingsStore.setRootFolderName(name)
                        }
                    },
                    onRequestImagePermission = { requestImagePermission() },
                    onConnectDrive = { startGoogleDriveSignIn() },
                    onDisconnectDrive = { disconnectGoogleDrive() },
                    onImportRecent = { CaptureProcessWorker.importRecentNow(this, 7) },
                    onRetryFailed = { CaptureProcessWorker.retryFailedNow(this) }
                )
            }
        }
        connectedDriveAccount?.let { completeDriveConnection(it) }
    }

    override fun onDestroy() {
        screenshotObserver?.unregister()
        screenshotObserver = null
        super.onDestroy()
    }

    private fun startAutomationFromSettings() {
        lifecycleScope.launch {
            if (!hasImageReadPermission()) {
                imagePermissionGranted = false
                screenshotObserver?.unregister()
                screenshotObserver = null
                CaptureProcessWorker.cancelPeriodicScan(this@MainActivity)
                return@launch
            }
            imagePermissionGranted = true
            imagePermissionDenied = false
            val settings = settingsStore.snapshot()
            if (settings.autoDetectionEnabled) {
                if (screenshotObserver == null) {
                    screenshotObserver = ScreenshotContentObserver(this@MainActivity).also { it.register() }
                }
                CaptureProcessWorker.schedulePeriodicScan(this@MainActivity)
                CaptureProcessWorker.enqueue(
                    context = this@MainActivity,
                    daysBack = settings.recentImportDays,
                    limit = settings.processingBatchLimit
                )
            } else {
                screenshotObserver?.unregister()
                screenshotObserver = null
                CaptureProcessWorker.cancelPeriodicScan(this@MainActivity)
            }
        }
    }

    private fun hasImageReadPermission(): Boolean {
        val permission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            Manifest.permission.READ_MEDIA_IMAGES
        } else {
            Manifest.permission.READ_EXTERNAL_STORAGE
        }
        return ContextCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_GRANTED
    }

    private fun requestImagePermission() {
        val permission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            Manifest.permission.READ_MEDIA_IMAGES
        } else {
            Manifest.permission.READ_EXTERNAL_STORAGE
        }
        imagePermissionLauncher.launch(permission)
    }

    private fun startGoogleDriveSignIn() {
        val signInOptions = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestEmail()
            .requestScopes(Scope(DriveScopes.DRIVE_FILE))
            .build()
        googleDriveSignInLauncher.launch(GoogleSignIn.getClient(this, signInOptions).signInIntent)
    }

    private fun disconnectGoogleDrive() {
        val signInOptions = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestEmail()
            .requestScopes(Scope(DriveScopes.DRIVE_FILE))
            .build()
        GoogleSignIn.getClient(this, signInOptions).signOut()
        accountStore.clearAccountName()
        connectedDriveAccount = null
        driveConnectionError = null
        pendingDriveAccountName = null
    }

    private fun completeDriveConnection(accountName: String) {
        pendingDriveAccountName = accountName
        lifecycleScope.launch {
            try {
                withContext(Dispatchers.IO) {
                    GoogleAccountCredential.usingOAuth2(
                        applicationContext,
                        listOf(DriveScopes.DRIVE_FILE)
                    ).apply {
                        selectedAccountName = accountName
                    }.token
                }
                accountStore.saveAccountName(accountName)
                connectedDriveAccount = accountName
                pendingDriveAccountName = null
                driveConnectionError = null
                CaptureProcessWorker.retryFailedNow(this@MainActivity)
            } catch (error: UserRecoverableAuthIOException) {
                driveConnectionError = "Google Drive 권한 동의가 추가로 필요합니다. 표시되는 Google 화면에서 허용을 눌러주세요."
                driveConsentLauncher.launch(error.intent)
            } catch (error: UserRecoverableAuthException) {
                val intent = error.intent
                if (intent != null) {
                    driveConnectionError = "Google Drive 권한 동의가 추가로 필요합니다. 표시되는 Google 화면에서 허용을 눌러주세요."
                    driveConsentLauncher.launch(intent)
                } else {
                    driveConnectionError = "Google Drive 연결 실패: ${error::class.simpleName}: ${error.message ?: "권한 동의 화면을 열 수 없습니다."}"
                }
            } catch (error: Exception) {
                driveConnectionError = "Google Drive 연결 실패: ${error::class.simpleName}: ${error.message ?: "unknown error"}"
            }
        }
    }

    private fun CaptureItemEntity.toUi(): CaptureItemUi {
        val categoryValue = category ?: when (status) {
            ProcessingStatus.FAILED_UPLOAD, ProcessingStatus.FAILED_OCR -> "오류"
            ProcessingStatus.QUEUED, ProcessingStatus.DETECTED -> "대기"
            else -> "CaptureBrain"
        }
        val subcategoryValue = subcategory ?: status.name.lowercase().replace('_', ' ')
        return CaptureItemUi(
            id = id,
            title = title ?: fileName,
            sourceApp = sourceApp ?: "Screenshot",
            capturedAt = formatCaptureTime(capturedAtMillis),
            category = categoryValue,
            subcategory = subcategoryValue,
            tags = tags,
            ocrText = ocrText ?: errorMessage ?: "OCR 대기 중",
            summary = summary ?: errorMessage ?: statusLabel(status),
            markdownPreview = markdown ?: "Markdown 생성 대기 중",
            drivePath = driveFolderId ?: "Drive 업로드 대기 중",
            status = status.toUiStatus(),
            confidence = ocrConfidence ?: 0f,
            isSensitive = isSensitive
        )
    }

    private fun List<CaptureItemEntity>.toQueueSummary(): QueueSummaryUi = QueueSummaryUi(
        todayCompleted = count { it.status == ProcessingStatus.COMPLETED && isToday(it.capturedAtMillis) },
        queued = count { it.status in setOf(ProcessingStatus.DETECTED, ProcessingStatus.QUEUED, ProcessingStatus.PROCESSING, ProcessingStatus.MARKDOWN_CREATED, ProcessingStatus.UPLOADING) },
        failed = count { it.status in setOf(ProcessingStatus.FAILED_OCR, ProcessingStatus.FAILED_UPLOAD) },
        sensitivePending = count { it.status == ProcessingStatus.SENSITIVE_PENDING }
    )

    private fun ProcessingStatus.toUiStatus(): CaptureStatus = when (this) {
        ProcessingStatus.DETECTED -> CaptureStatus.Detected
        ProcessingStatus.QUEUED -> CaptureStatus.Queued
        ProcessingStatus.PROCESSING -> CaptureStatus.Processing
        ProcessingStatus.MARKDOWN_CREATED -> CaptureStatus.MarkdownCreated
        ProcessingStatus.UPLOADING -> CaptureStatus.Uploading
        ProcessingStatus.COMPLETED -> CaptureStatus.Completed
        ProcessingStatus.FAILED_OCR -> CaptureStatus.FailedOcr
        ProcessingStatus.FAILED_UPLOAD -> CaptureStatus.FailedUpload
        ProcessingStatus.SKIPPED -> CaptureStatus.FailedUpload
        ProcessingStatus.SENSITIVE_PENDING -> CaptureStatus.SensitivePending
    }

    private fun statusLabel(status: ProcessingStatus): String = when (status) {
        ProcessingStatus.DETECTED -> "스크린샷 감지됨"
        ProcessingStatus.QUEUED -> "처리 대기 중"
        ProcessingStatus.PROCESSING -> "OCR 처리 중"
        ProcessingStatus.MARKDOWN_CREATED -> "Markdown 생성 완료"
        ProcessingStatus.UPLOADING -> "Drive 업로드 중"
        ProcessingStatus.COMPLETED -> "완료"
        ProcessingStatus.FAILED_OCR -> "OCR 실패"
        ProcessingStatus.FAILED_UPLOAD -> "Drive 업로드 실패"
        ProcessingStatus.SKIPPED -> "건너뜀"
        ProcessingStatus.SENSITIVE_PENDING -> "민감정보 확인 필요"
    }

    private fun formatCaptureTime(millis: Long): String = SimpleDateFormat("MM-dd HH:mm", Locale.KOREA).format(Date(millis))

    private fun isToday(millis: Long): Boolean {
        val dayFormat = SimpleDateFormat("yyyyMMdd", Locale.KOREA)
        return dayFormat.format(Date(millis)) == dayFormat.format(Date())
    }

}
