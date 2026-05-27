package com.ponslink.capturebrain.core

data class SensitivityResult(
    val isSensitive: Boolean,
    val reason: String? = null
)

data class AnalysisResult(
    val title: String,
    val category: String,
    val subcategory: String,
    val folderPath: String,
    val summary: String,
    val keyPoints: List<String>,
    val tags: List<String>,
    val entitiesJson: String = "{}",
    val sensitivity: SensitivityResult = SensitivityResult(false)
)
