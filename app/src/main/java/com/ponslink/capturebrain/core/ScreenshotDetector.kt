package com.ponslink.capturebrain.core

interface ScreenshotDetector {
    suspend fun scanRecent(daysBack: Int = 1): List<DetectedScreenshot>
}
