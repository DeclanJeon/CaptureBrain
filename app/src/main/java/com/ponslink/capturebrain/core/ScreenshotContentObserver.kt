package com.ponslink.capturebrain.core

import android.content.Context
import android.database.ContentObserver
import android.net.Uri
import android.os.Handler
import android.os.Looper
import android.provider.MediaStore
import com.ponslink.capturebrain.worker.CaptureProcessWorker

/**
 * MediaStore ContentObserver entry point for FR-001/FR-005.
 *
 * Android does not deliver the inserted image payload directly. The observer should enqueue
 * WorkManager, and the worker performs an idempotent MediaStore scan so missed or duplicated
 * callbacks do not create duplicate CaptureItem rows.
 */
class ScreenshotContentObserver(
    private val context: Context,
    handler: Handler = Handler(Looper.getMainLooper()),
    private val onScreenshotChanged: () -> Unit = { CaptureProcessWorker.enqueue(context) }
) : ContentObserver(handler) {

    fun register() {
        context.contentResolver.registerContentObserver(
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
            true,
            this
        )
    }

    fun unregister() {
        context.contentResolver.unregisterContentObserver(this)
    }

    override fun onChange(selfChange: Boolean) {
        onScreenshotChanged()
    }

    override fun onChange(selfChange: Boolean, uri: Uri?) {
        onScreenshotChanged()
    }

    override fun onChange(selfChange: Boolean, uris: Collection<Uri>, flags: Int) {
        onScreenshotChanged()
    }
}
