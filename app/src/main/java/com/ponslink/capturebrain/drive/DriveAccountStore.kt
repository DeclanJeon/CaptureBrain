package com.ponslink.capturebrain.drive

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey

class DriveAccountStore(context: Context) {
    private val appContext = context.applicationContext
    private val prefs: SharedPreferences by lazy { createPreferences() }

    fun getAccountName(): String? = prefs.getString(KEY_ACCOUNT_NAME, null)

    fun hasConnectedAccount(): Boolean = !getAccountName().isNullOrBlank()

    fun saveAccountName(accountName: String) {
        require(accountName.isNotBlank()) { "Google account name is required" }
        prefs.edit()
            .putString(KEY_ACCOUNT_NAME, accountName)
            .apply()
    }

    fun clearAccountName() {
        prefs.edit()
            .remove(KEY_ACCOUNT_NAME)
            .apply()
    }

    fun getRootFolderName(): String = prefs.getString(KEY_ROOT_FOLDER_NAME, DEFAULT_ROOT_FOLDER_NAME)
        ?.ifBlank { DEFAULT_ROOT_FOLDER_NAME }
        ?: DEFAULT_ROOT_FOLDER_NAME

    fun saveRootFolderName(rootFolderName: String) {
        prefs.edit()
            .putString(KEY_ROOT_FOLDER_NAME, rootFolderName.ifBlank { DEFAULT_ROOT_FOLDER_NAME })
            .apply()
    }

    private fun createPreferences(): SharedPreferences = runCatching {
        val masterKey = MasterKey.Builder(appContext)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()
        EncryptedSharedPreferences.create(
            appContext,
            PREFS_NAME,
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
    }.getOrElse {
        // Fallback only stores account email and root folder name. OAuth tokens stay in Google Play services/AccountManager.
        appContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    companion object {
        private const val PREFS_NAME = "capturebrain_drive_account"
        private const val KEY_ACCOUNT_NAME = "account_name"
        private const val KEY_ROOT_FOLDER_NAME = "root_folder_name"
        const val DEFAULT_ROOT_FOLDER_NAME = "CaptureBrain"
    }
}
