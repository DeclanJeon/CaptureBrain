package com.ponslink.capturebrain.drive

import android.content.Context
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.Scope
import com.google.api.services.drive.DriveScopes

object GoogleDriveSignInConfig {
    val driveFileScope: Scope = Scope(DriveScopes.DRIVE_FILE)

    fun signInOptions(): GoogleSignInOptions = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
        .requestEmail()
        .requestScopes(driveFileScope)
        .build()

    fun lastSignedInAccount(context: Context): GoogleSignInAccount? = GoogleSignIn.getLastSignedInAccount(context.applicationContext)

    fun persistAuthorizedAccount(context: Context, account: GoogleSignInAccount): Boolean {
        val hasDriveFileScope = GoogleSignIn.hasPermissions(account, driveFileScope)
        val email = account.email
        if (hasDriveFileScope && !email.isNullOrBlank()) {
            DriveAccountStore(context).saveAccountName(email)
            return true
        }
        return false
    }
}
