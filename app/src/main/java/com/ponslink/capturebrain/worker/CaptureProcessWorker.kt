package com.ponslink.capturebrain.worker

import android.content.Context
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.Data
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.ponslink.capturebrain.core.CaptureRepository
import com.ponslink.capturebrain.core.MediaStoreScreenshotScanner
import com.ponslink.capturebrain.data.CaptureBrainDatabase
import com.ponslink.capturebrain.drive.DriveAccountStore
import com.ponslink.capturebrain.drive.UnconnectedDriveUploader
import com.ponslink.capturebrain.drive.UserGoogleDriveUploader
import com.ponslink.capturebrain.ocr.MlKitOcrProcessor
import com.ponslink.capturebrain.settings.CaptureSettingsStore
import java.util.concurrent.TimeUnit

class CaptureProcessWorker(
    appContext: Context,
    params: WorkerParameters
) : CoroutineWorker(appContext, params) {
    override suspend fun doWork(): Result {
        val db = CaptureBrainDatabase.get(applicationContext)
        val settings = CaptureSettingsStore(applicationContext).snapshot()
        val accountStore = DriveAccountStore(applicationContext)
        val accountName = accountStore.getAccountName()
        val driveUploader = if (accountName.isNullOrBlank()) {
            UnconnectedDriveUploader()
        } else {
            UserGoogleDriveUploader(
                context = applicationContext,
                accountName = accountName,
                rootFolderName = settings.rootFolderName.ifBlank { accountStore.getRootFolderName() }
            )
        }
        val repository = CaptureRepository(
            dao = db.captureItemDao(),
            screenshotDetector = MediaStoreScreenshotScanner(applicationContext),
            ocrProcessor = MlKitOcrProcessor(applicationContext),
            driveUploader = driveUploader
        )
        repository.importRecentScreenshots(
            daysBack = inputData.getInt(KEY_DAYS_BACK, settings.recentImportDays)
        )
        if (inputData.getBoolean(KEY_RETRY_FAILED, false)) {
            repository.retryFailed()
        }
        repository.processNext(
            limit = inputData.getInt(KEY_LIMIT, settings.processingBatchLimit)
        )
        return Result.success()
    }

    companion object {
        const val UNIQUE_WORK_NAME = "capturebrain-process-queue"
        const val PERIODIC_SCAN_WORK_NAME = "capturebrain-periodic-screenshot-scan"
        const val KEY_DAYS_BACK = "daysBack"
        const val KEY_LIMIT = "limit"
        const val KEY_RETRY_FAILED = "retryFailed"

        fun enqueue(context: Context, daysBack: Int? = null, limit: Int? = null) {
            val settings = CaptureSettingsStore(context).blockingSnapshot()
            val request = OneTimeWorkRequestBuilder<CaptureProcessWorker>()
                .setConstraints(buildNetworkConstraints(settings.wifiOnlyUpload))
                .setInputData(buildInputData(daysBack ?: settings.recentImportDays, limit ?: settings.processingBatchLimit))
                .build()
            WorkManager.getInstance(context).enqueueUniqueWork(
                UNIQUE_WORK_NAME,
                ExistingWorkPolicy.APPEND_OR_REPLACE,
                request
            )
        }

        fun importRecentNow(context: Context, daysBack: Int) {
            enqueue(context = context, daysBack = daysBack.coerceIn(1, 30), limit = 25)
        }

        fun retryFailedNow(context: Context) {
            val settings = CaptureSettingsStore(context).blockingSnapshot()
            val request = OneTimeWorkRequestBuilder<CaptureProcessWorker>()
                .setConstraints(buildNetworkConstraints(settings.wifiOnlyUpload))
                .setInputData(
                    buildInputData(settings.recentImportDays, settings.processingBatchLimit, retryFailed = true)
                )
                .build()
            WorkManager.getInstance(context).enqueueUniqueWork(
                UNIQUE_WORK_NAME,
                ExistingWorkPolicy.REPLACE,
                request
            )
        }

        fun schedulePeriodicScan(context: Context) {
            val settings = CaptureSettingsStore(context).blockingSnapshot()
            if (!settings.autoDetectionEnabled) {
                cancelPeriodicScan(context)
                return
            }
            val request = PeriodicWorkRequestBuilder<CaptureProcessWorker>(6, TimeUnit.HOURS)
                .setConstraints(buildNetworkConstraints(settings.wifiOnlyUpload))
                .setInputData(buildInputData(settings.recentImportDays, settings.processingBatchLimit))
                .build()
            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                PERIODIC_SCAN_WORK_NAME,
                ExistingPeriodicWorkPolicy.UPDATE,
                request
            )
        }

        fun cancelPeriodicScan(context: Context) {
            WorkManager.getInstance(context).cancelUniqueWork(PERIODIC_SCAN_WORK_NAME)
        }

        private fun buildNetworkConstraints(wifiOnlyUpload: Boolean): Constraints {
            val networkType = if (wifiOnlyUpload) NetworkType.UNMETERED else NetworkType.CONNECTED
            return Constraints.Builder()
                .setRequiredNetworkType(networkType)
                .build()
        }

        private fun buildInputData(daysBack: Int, limit: Int, retryFailed: Boolean = false): Data = Data.Builder()
            .putInt(KEY_DAYS_BACK, daysBack.coerceIn(1, 30))
            .putInt(KEY_LIMIT, limit.coerceIn(1, 50))
            .putBoolean(KEY_RETRY_FAILED, retryFailed)
            .build()
    }
}
