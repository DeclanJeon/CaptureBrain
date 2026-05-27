package com.ponslink.capturebrain.core

class LocalAnalyzer(
    private val folderResolver: FolderResolver = FolderResolver()
) {
    fun analyze(ocrText: String): AnalysisResult {
        val normalized = ocrText.lineSequence().map { it.trim() }.filter { it.isNotEmpty() }.toList()
        val title = normalized.firstOrNull()?.take(80) ?: "Untitled Screenshot"
        val folder = folderResolver.resolve(ocrText, title)
        val category = folder.substringBefore('/').removePrefix("00_").removePrefix("01_").removePrefix("03_").removePrefix("04_").removePrefix("05_").removePrefix("06_")
        val subcategory = folder.substringAfter('/', missingDelimiterValue = "Inbox")
        val tags = buildSet {
            add(category)
            if (subcategory != "Inbox") add(subcategory)
            if (ocrText.contains("AI", ignoreCase = true)) add("AI")
            if (ocrText.contains("API", ignoreCase = true)) add("API")
        }.map { it.replace(" ", "-") }
        val sensitive = detectSensitivity(ocrText)
        return AnalysisResult(
            title = title,
            category = category,
            subcategory = subcategory,
            folderPath = folder,
            summary = if (normalized.isEmpty()) "OCR 텍스트가 비어 있습니다." else normalized.take(3).joinToString(" ").take(240),
            keyPoints = normalized.take(5),
            tags = tags,
            sensitivity = sensitive
        )
    }

    private fun detectSensitivity(text: String): SensitivityResult {
        val patterns = listOf("password", "비밀번호", "주민등록", "카드번호", "계좌", "token", "secret")
        val match = patterns.firstOrNull { text.contains(it, ignoreCase = true) }
        return if (match == null) SensitivityResult(false) else SensitivityResult(true, "민감 키워드 감지: $match")
    }
}
