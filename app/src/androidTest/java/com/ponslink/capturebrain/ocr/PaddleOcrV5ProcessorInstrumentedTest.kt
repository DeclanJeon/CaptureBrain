package com.ponslink.capturebrain.ocr

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.net.Uri
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File
import java.io.FileOutputStream

@RunWith(AndroidJUnit4::class)
class PaddleOcrV5ProcessorInstrumentedTest {
    @Test
    fun recognizeGeneratedKoreanScreenshot_doesNotCrashAndReturnsText() = runBlocking {
        val context = ApplicationProvider.getApplicationContext<android.content.Context>()
        val bitmap = Bitmap.createBitmap(1080, 720, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        canvas.drawColor(Color.WHITE)

        val titlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.BLACK
            textSize = 72f
            isFakeBoldText = true
        }
        val bodyPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.rgb(30, 30, 30)
            textSize = 48f
        }
        canvas.drawText("캡처브레인 OCR 테스트", 80f, 160f, titlePaint)
        canvas.drawText("구글 드라이브 자동 분류 저장", 80f, 260f, bodyPaint)
        canvas.drawText("CaptureBrain 12345", 80f, 340f, bodyPaint)

        val file = File(context.cacheDir, "paddle_ocr_test.png")
        FileOutputStream(file).use { bitmap.compress(Bitmap.CompressFormat.PNG, 100, it) }

        val result = PaddleOcrV5Processor(context).recognize(Uri.fromFile(file)).getOrThrow()
        assertTrue("Expected OCR text to be non-blank, got: '${result.originalText}'", result.originalText.isNotBlank())
    }
}
