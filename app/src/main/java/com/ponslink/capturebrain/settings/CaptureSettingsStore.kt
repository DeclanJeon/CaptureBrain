package com.ponslink.capturebrain.settings

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.runBlocking

private val Context.captureBrainDataStore: DataStore<Preferences> by preferencesDataStore(
    name = "capturebrain_settings"
)

data class CaptureSettings(
    val autoDetectionEnabled: Boolean = true,
    val wifiOnlyUpload: Boolean = false,
    val recentImportDays: Int = 1,
    val processingBatchLimit: Int = 5,
    val rootFolderName: String = "CaptureBrain"
)

/**
 * DataStore-backed settings for automation, manual import, and upload constraints.
 *
 * This covers FR-003 (automatic detection ON/OFF), FR-004 (recent import window),
 * FR-055 (root folder name), FR-056 (Wi-Fi only upload), and NFR-013 (backoff/constraints).
 */
class CaptureSettingsStore(private val context: Context) {
    val settingsFlow = context.captureBrainDataStore.data.map { prefs -> prefs.toCaptureSettings() }

    suspend fun snapshot(): CaptureSettings = settingsFlow.first()

    fun blockingSnapshot(): CaptureSettings = runBlocking { snapshot() }

    suspend fun setAutoDetectionEnabled(enabled: Boolean) {
        context.captureBrainDataStore.edit { prefs -> prefs[Keys.AUTO_DETECTION_ENABLED] = enabled }
    }

    suspend fun setWifiOnlyUpload(enabled: Boolean) {
        context.captureBrainDataStore.edit { prefs -> prefs[Keys.WIFI_ONLY_UPLOAD] = enabled }
    }

    suspend fun setRecentImportDays(days: Int) {
        val safeDays = days.coerceIn(1, 30)
        context.captureBrainDataStore.edit { prefs -> prefs[Keys.RECENT_IMPORT_DAYS] = safeDays }
    }

    suspend fun setProcessingBatchLimit(limit: Int) {
        val safeLimit = limit.coerceIn(1, 50)
        context.captureBrainDataStore.edit { prefs -> prefs[Keys.PROCESSING_BATCH_LIMIT] = safeLimit }
    }

    suspend fun setRootFolderName(name: String) {
        val safeName = name.trim().ifBlank { DEFAULT_ROOT_FOLDER_NAME }
        context.captureBrainDataStore.edit { prefs -> prefs[Keys.ROOT_FOLDER_NAME] = safeName }
    }

    private fun Preferences.toCaptureSettings(): CaptureSettings = CaptureSettings(
        autoDetectionEnabled = this[Keys.AUTO_DETECTION_ENABLED] ?: true,
        wifiOnlyUpload = this[Keys.WIFI_ONLY_UPLOAD] ?: false,
        recentImportDays = this[Keys.RECENT_IMPORT_DAYS] ?: 1,
        processingBatchLimit = this[Keys.PROCESSING_BATCH_LIMIT] ?: 5,
        rootFolderName = this[Keys.ROOT_FOLDER_NAME] ?: DEFAULT_ROOT_FOLDER_NAME
    )

    private object Keys {
        val AUTO_DETECTION_ENABLED = booleanPreferencesKey("auto_detection_enabled")
        val WIFI_ONLY_UPLOAD = booleanPreferencesKey("wifi_only_upload")
        val RECENT_IMPORT_DAYS = intPreferencesKey("recent_import_days")
        val PROCESSING_BATCH_LIMIT = intPreferencesKey("processing_batch_limit")
        val ROOT_FOLDER_NAME = stringPreferencesKey("root_folder_name")
    }

    companion object {
        const val DEFAULT_ROOT_FOLDER_NAME = "CaptureBrain"
    }
}
