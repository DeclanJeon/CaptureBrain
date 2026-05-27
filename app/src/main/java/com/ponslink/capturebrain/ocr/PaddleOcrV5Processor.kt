package com.ponslink.capturebrain.ocr

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import ai.onnxruntime.OnnxTensor
import ai.onnxruntime.OrtEnvironment
import ai.onnxruntime.OrtSession
import ai.onnxruntime.OrtSession.SessionOptions
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.nio.FloatBuffer
import kotlin.math.ceil
import kotlin.math.max

/**
 * PaddleOCR v5 mobile processor: det → cls → rec pipeline.
 * Uses ONNX Runtime for inference on device.
 */
class PaddleOcrV5Processor(
    private val context: Context
) : OcrProcessor {

    private val env = OrtEnvironment.getEnvironment()
    private var detSession: OrtSession? = null
    private var clsSession: OrtSession? = null
    private var recSession: OrtSession? = null
    private var dict: List<String> = emptyList()

    private suspend fun ensureInitialized() = withContext(Dispatchers.IO) {
        if (detSession != null) return@withContext
        val opts = SessionOptions().apply { setOptimizationLevel(SessionOptions.OptLevel.ALL_OPT) }

        detSession = env.createSession(copyAssetToCache("paddleocr/det.onnx"), opts)
        clsSession = env.createSession(copyAssetToCache("paddleocr/cls.onnx"), opts)
        recSession = env.createSession(copyAssetToCache("paddleocr/rec_korean.onnx"), opts)

        dict = context.assets.open("paddleocr/ppocrv5_dict.txt").bufferedReader()
            .readLines().filter { it.isNotEmpty() }
    }

    private fun copyAssetToCache(assetPath: String): String {
        val outFile = File(context.cacheDir, assetPath.replace("/", "_"))
        if (!outFile.exists()) {
            outFile.parentFile?.mkdirs()
            context.assets.open(assetPath).use { input ->
                FileOutputStream(outFile).use { output -> input.copyTo(output) }
            }
        }
        return outFile.absolutePath
    }

    override suspend fun recognize(imageUri: Uri): Result<OcrResult> = runCatching {
        ensureInitialized()
        val bitmap = withContext(Dispatchers.IO) {
            context.contentResolver.openInputStream(imageUri)?.use {
                BitmapFactory.decodeStream(it)
            } ?: throw IllegalStateException("Cannot open image: $imageUri")
        }

        // 1. Detection
        val detResult = runDetection(bitmap)

        // 2. For each text region: cls → rec
        val blocks = mutableListOf<RecognizedTextBlock>()
        val confidences = mutableListOf<Float>()
        for (box in detResult) {
            val cropped = cropAndRotate(bitmap, box)
            if (cropped.width < 10 || cropped.height < 10) continue

            // Direction classification
            val isReversed = runClassification(cropped)
            val oriented = if (isReversed) rotate180(cropped) else cropped

            // Text recognition
            val (text, conf) = runRecognition(oriented)
            if (text.isBlank()) continue
            confidences.add(conf)

            val minX = box.map { it.first }.min().toInt()
            val minY = box.map { it.second }.min().toInt()
            val maxX = box.map { it.first }.max().toInt()
            val maxY = box.map { it.second }.max().toInt()

            blocks.add(RecognizedTextBlock(
                text = text,
                left = minX,
                top = minY,
                right = maxX,
                bottom = maxY
            ))
        }

        OcrResult(
            originalText = blocks.joinToString("\n") { it.text },
            layoutPreservedText = blocks.sortedWith(compareBy<RecognizedTextBlock> { it.top }.thenBy { it.left })
                .joinToString("\n") { it.text },
            confidence = confidences.takeIf { it.isNotEmpty() }?.average()?.toFloat(),
            blocks = blocks
        )
    }

    // ─── Detection ──────────────────────────────────────────

    private fun runDetection(bitmap: Bitmap): List<List<Pair<Float, Float>>> {
        val resizeLong = 960
        val ratio = resizeLong.toFloat() / max(bitmap.width, bitmap.height)
        val newW = (bitmap.width * ratio).toInt().coerceAtLeast(32)
        val newH = (bitmap.height * ratio).toInt().coerceAtLeast(32)
        // Round to 32
        val dstW = ceil(newW / 32f).toInt() * 32
        val dstH = ceil(newH / 32f).toInt() * 32

        val resized = Bitmap.createScaledBitmap(bitmap, dstW, dstH, true)

        // ImageNet normalize: mean=[0.485,0.456,0.406], std=[0.229,0.224,0.225], BGR
        val mean = floatArrayOf(0.485f, 0.456f, 0.406f)
        val std = floatArrayOf(0.229f, 0.224f, 0.225f)
        val imgData = FloatArray(3 * dstW * dstH)
        val pixels = IntArray(dstW * dstH)
        resized.getPixels(pixels, 0, dstW, 0, 0, dstW, dstH)

        for (i in pixels.indices) {
            val px = pixels[i]
            // BGR order
            imgData[i] = ((px and 0xFF) / 255f - mean[0]) / std[0]               // B
            imgData[dstW * dstH + i] = (((px shr 8) and 0xFF) / 255f - mean[1]) / std[1] // G
            imgData[2 * dstW * dstH + i] = (((px shr 16) and 0xFF) / 255f - mean[2]) / std[2] // R
        }

        val shape = longArrayOf(1, 3, dstH.toLong(), dstW.toLong())
        val inputTensor = OnnxTensor.createTensor(env, FloatBuffer.wrap(imgData), shape)

        val session = detSession ?: return emptyList()
        val inputName = session.inputNames.iterator().next()
        val results = session.run(mapOf(inputName to inputTensor))
        val rawOutput = results[0].value as Array<Array<Array<FloatArray>>>
        val output = rawOutput[0][0]

        // DB post-process: thresh=0.3, box_thresh=0.6, unclip_ratio=1.5
        val predH = output.size
        val predW = output[0].size
        val thresh = 0.3f
        val boxThresh = 0.6f
        val unclipRatio = 1.5f

        val boxes = mutableListOf<List<Pair<Float, Float>>>()

        // Connected component on binary mask
        val mask = Array(predH) { BooleanArray(predW) }
        for (y in 0 until predH) {
            for (x in 0 until predW) {
                mask[y][x] = output[y][x] > thresh
            }
        }

        val visited = Array(predH) { BooleanArray(predW) }
        for (y in 0 until predH) {
            for (x in 0 until predW) {
                if (mask[y][x] && !visited[y][x]) {
                    val component = floodFill(mask, visited, x, y, predW, predH)
                    if (component.size < 10) continue

                    // Check mean score
                    val meanScore = component.map { output[it.second][it.first] }.average().toFloat()
                    if (meanScore < boxThresh) continue

                    // Get bounding box from component, then expand (unclip)
                    val box = minAreaRect(component)
                    val expandedBox = expandBox(box, unclipRatio)

                    // Scale back to original bitmap coordinates
                    val scaleX = bitmap.width.toFloat() / dstW
                    val scaleY = bitmap.height.toFloat() / dstH
                    val scaledBox = expandedBox.map { Pair(it.first * scaleX, it.second * scaleY) }
                    boxes.add(scaledBox)
                }
            }
        }

        return boxes
    }

    private fun floodFill(
        mask: Array<BooleanArray>, visited: Array<BooleanArray>,
        startX: Int, startY: Int, w: Int, h: Int
    ): List<Pair<Int, Int>> {
        val result = mutableListOf<Pair<Int, Int>>()
        val queue = ArrayDeque<Pair<Int, Int>>()
        queue.add(Pair(startX, startY))
        visited[startY][startX] = true

        while (queue.isNotEmpty()) {
            val (x, y) = queue.removeFirst()
            result.add(Pair(x, y))
            for ((dx, dy) in listOf(Pair(1, 0), Pair(-1, 0), Pair(0, 1), Pair(0, -1))) {
                val nx = x + dx
                val ny = y + dy
                if (nx in 0 until w && ny in 0 until h && mask[ny][nx] && !visited[ny][nx]) {
                    visited[ny][nx] = true
                    queue.add(Pair(nx, ny))
                }
            }
        }
        return result
    }

    private fun minAreaRect(points: List<Pair<Int, Int>>): List<Pair<Float, Float>> {
        if (points.isEmpty()) return emptyList()
        val minX = points.minOf { it.first }.toFloat()
        val maxX = points.maxOf { it.first }.toFloat()
        val minY = points.minOf { it.second }.toFloat()
        val maxY = points.maxOf { it.second }.toFloat()
        return listOf(
            Pair(minX, minY), Pair(maxX, minY),
            Pair(maxX, maxY), Pair(minX, maxY)
        )
    }

    private fun expandBox(box: List<Pair<Float, Float>>, ratio: Float): List<Pair<Float, Float>> {
        val cx = box.map { it.first }.average().toFloat()
        val cy = box.map { it.second }.average().toFloat()
        return box.map { Pair(cx + (it.first - cx) * ratio, cy + (it.second - cy) * ratio) }
    }

    // ─── Classification ─────────────────────────────────────

    private fun runClassification(bitmap: Bitmap): Boolean {
        val clsH = 80
        val clsW = 160
        val ratio = clsH.toFloat() / bitmap.height
        val resizedW = (bitmap.width * ratio).toInt().coerceIn(1, clsW)
        val resized = Bitmap.createScaledBitmap(bitmap, resizedW, clsH, true)

        val imgData = FloatArray(3 * clsH * clsW)
        val pixels = IntArray(resizedW * clsH)
        resized.getPixels(pixels, 0, resizedW, 0, 0, resizedW, clsH)

        val mean = floatArrayOf(0.5f, 0.5f, 0.5f)
        val std = floatArrayOf(0.5f, 0.5f, 0.5f)
        for (y in 0 until clsH) {
            for (x in 0 until resizedW) {
                val srcIdx = y * resizedW + x
                val dstIdx = y * clsW + x
                val px = pixels[srcIdx]
                imgData[dstIdx] = ((px and 0xFF) / 255f - mean[0]) / std[0]
                imgData[clsW * clsH + dstIdx] = (((px shr 8) and 0xFF) / 255f - mean[1]) / std[1]
                imgData[2 * clsW * clsH + dstIdx] = (((px shr 16) and 0xFF) / 255f - mean[2]) / std[2]
            }
        }

        val shape = longArrayOf(1, 3, clsH.toLong(), clsW.toLong())
        val inputTensor = OnnxTensor.createTensor(env, FloatBuffer.wrap(imgData), shape)

        val session = clsSession ?: return false
        val inputName = session.inputNames.iterator().next()
        val results = session.run(mapOf(inputName to inputTensor))
        val output = (results[0].value as Array<FloatArray>)[0]

        // 2 classes: [0°, 180°]
        return output[1] > output[0]
    }

    // ─── Recognition ────────────────────────────────────────

    private fun runRecognition(bitmap: Bitmap): Pair<String, Float> {
        val recH = 48
        val recW = 320
        val ratio = recH.toFloat() / bitmap.height
        val resizedW = (bitmap.width * ratio).toInt().coerceIn(1, recW)

        val resized = Bitmap.createScaledBitmap(bitmap, resizedW, recH, true)

        val imgData = FloatArray(3 * recH * recW)
        val pixels = IntArray(resizedW * recH)
        resized.getPixels(pixels, 0, resizedW, 0, 0, resizedW, recH)

        val mean = floatArrayOf(0.5f, 0.5f, 0.5f)
        val std = floatArrayOf(0.5f, 0.5f, 0.5f)
        for (y in 0 until recH) {
            for (x in 0 until resizedW) {
                val srcIdx = y * resizedW + x
                val dstIdx = y * recW + x
                val px = pixels[srcIdx]
                imgData[dstIdx] = ((px and 0xFF) / 255f - mean[0]) / std[0]
                imgData[recW * recH + dstIdx] = (((px shr 8) and 0xFF) / 255f - mean[1]) / std[1]
                imgData[2 * recW * recH + dstIdx] = (((px shr 16) and 0xFF) / 255f - mean[2]) / std[2]
            }
        }

        val shape = longArrayOf(1, 3, recH.toLong(), recW.toLong())
        val inputTensor = OnnxTensor.createTensor(env, FloatBuffer.wrap(imgData), shape)

        val session = recSession ?: return Pair("", 0f)
        val inputName = session.inputNames.iterator().next()
        val results = session.run(mapOf(inputName to inputTensor))
        val output = results[0].value as Array<Array<FloatArray>>

        // CTC decode
        val seqLen = output[0].size
        val numClasses = output[0][0].size
        val sb = StringBuilder()
        var lastIdx = -1
        var totalConf = 0f
        var confCount = 0

        for (t in 0 until seqLen) {
            var maxIdx = 0
            var maxVal = output[0][t][0]
            for (c in 1 until numClasses) {
                if (output[0][t][c] > maxVal) {
                    maxVal = output[0][t][c]
                    maxIdx = c
                }
            }
            // Blank is index 0 (or dict.size for blank — PaddleOCR uses dict.size as blank)
            if (maxIdx != lastIdx && maxIdx != 0 && maxIdx < dict.size) {
                sb.append(dict[maxIdx])
                totalConf += maxVal
                confCount++
            }
            lastIdx = maxIdx
        }

        val avgConf = if (confCount > 0) totalConf / confCount else 0f
        return Pair(sb.toString(), avgConf)
    }

    // ─── Helpers ────────────────────────────────────────────

    private fun cropAndRotate(bitmap: Bitmap, box: List<Pair<Float, Float>>): Bitmap {
        val minX = box.map { it.first }.min().toInt().coerceAtLeast(0)
        val minY = box.map { it.second }.min().toInt().coerceAtLeast(0)
        val maxX = box.map { it.first }.max().toInt().coerceAtMost(bitmap.width)
        val maxY = box.map { it.second }.max().toInt().coerceAtMost(bitmap.height)

        val w = max(maxX - minX, 1)
        val h = max(maxY - minY, 1)
        return Bitmap.createBitmap(bitmap, minX, minY, w, h)
    }

    private fun rotate180(bitmap: Bitmap): Bitmap {
        val matrix = android.graphics.Matrix().apply { postRotate(180f) }
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
    }
}
