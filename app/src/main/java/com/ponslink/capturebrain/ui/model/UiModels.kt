package com.ponslink.capturebrain.ui.model

enum class CaptureStatus(val label: String) {
    Detected("감지됨"),
    Queued("대기 중"),
    Processing("처리 중"),
    MarkdownCreated("Markdown 생성"),
    Uploading("업로드 중"),
    Completed("완료"),
    FailedOcr("OCR 실패"),
    FailedUpload("업로드 실패"),
    SensitivePending("민감정보 확인")
}

data class CaptureItemUi(
    val id: String,
    val title: String,
    val sourceApp: String,
    val capturedAt: String,
    val category: String,
    val subcategory: String,
    val tags: List<String>,
    val ocrText: String,
    val summary: String,
    val markdownPreview: String,
    val drivePath: String,
    val status: CaptureStatus,
    val confidence: Float,
    val isSensitive: Boolean = false
)

data class QueueSummaryUi(
    val todayCompleted: Int,
    val queued: Int,
    val failed: Int,
    val sensitivePending: Int
)

data class SettingsUi(
    val rootFolderName: String,
    val wifiOnlyUpload: Boolean,
    val ocrLanguages: List<String>,
    val aiAnalysisEnabled: Boolean,
    val sensitiveConfirmationEnabled: Boolean,
    val markdownTemplate: String
)

object CaptureBrainSampleData {
    val settings = SettingsUi(
        rootFolderName = "CaptureBrain",
        wifiOnlyUpload = true,
        ocrLanguages = listOf("한국어", "English", "日本語"),
        aiAnalysisEnabled = true,
        sensitiveConfirmationEnabled = true,
        markdownTemplate = "# {{title}}\n\n## Original Text\n{{ocr_text}}\n\n## Summary\n{{summary}}"
    )

    val captures = listOf(
        CaptureItemUi(
            id = "sample-dev-error",
            title = "Kotlin Coroutine Timeout Error",
            sourceApp = "Chrome",
            capturedAt = "오늘 14:32",
            category = "03_Development",
            subcategory = "Errors",
            tags = listOf("Kotlin", "Coroutine", "Error", "Debug"),
            ocrText = "TimeoutCancellationException: Timed out waiting for 30000 ms\nRetrofit suspend call did not complete.",
            summary = "Retrofit suspend 호출이 지정된 시간 내 종료되지 않은 오류 캡처입니다. 네트워크 재시도와 타임아웃 정책 확인이 필요합니다.",
            markdownPreview = "# Kotlin Coroutine Timeout Error\n\n- Source app: Chrome\n- Category: 03_Development / Errors\n- OCR confidence: 0.91\n\n## Original Text\nTimeoutCancellationException...",
            drivePath = "CaptureBrain/03_Development/Errors/2026/05/",
            status = CaptureStatus.Completed,
            confidence = 0.91f
        ),
        CaptureItemUi(
            id = "sample-pricing",
            title = "SaaS Pricing Reference",
            sourceApp = "Samsung Internet",
            capturedAt = "오늘 13:05",
            category = "04_Business",
            subcategory = "Pricing",
            tags = listOf("Pricing", "SaaS", "Competitor"),
            ocrText = "Pro plan $12/mo\nTeam plan $29/mo\nUnlimited exports included",
            summary = "경쟁 SaaS의 가격표와 플랜별 기능 차이를 저장한 캡처입니다.",
            markdownPreview = "# SaaS Pricing Reference\n\n## Original Text\nPro plan $12/mo...",
            drivePath = "CaptureBrain/04_Business/Pricing/2026/05/",
            status = CaptureStatus.SensitivePending,
            confidence = 0.84f,
            isSensitive = true
        ),
        CaptureItemUi(
            id = "sample-lecture",
            title = "AI Agent Architecture Lecture",
            sourceApp = "YouTube",
            capturedAt = "어제 21:14",
            category = "01_Learning",
            subcategory = "AI",
            tags = listOf("AI", "Agent", "Learning"),
            ocrText = "Planner → Tool Use → Execution → Reflection\nMemory stores context across turns.",
            summary = "AI Agent 실행 루프와 메모리 구조를 설명하는 강의 화면입니다.",
            markdownPreview = "# AI Agent Architecture Lecture\n\n## Layout-Preserved Notes\nPlanner → Tool Use...",
            drivePath = "CaptureBrain/01_Learning/AI/2026/05/",
            status = CaptureStatus.FailedUpload,
            confidence = 0.88f
        )
    )

    val queueSummary = QueueSummaryUi(
        todayCompleted = 12,
        queued = 3,
        failed = 1,
        sensitivePending = 1
    )
}
