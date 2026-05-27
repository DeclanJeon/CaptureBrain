package com.ponslink.capturebrain.data

enum class ProcessingStatus {
    DETECTED,
    QUEUED,
    PROCESSING,
    MARKDOWN_CREATED,
    UPLOADING,
    COMPLETED,
    FAILED_OCR,
    FAILED_UPLOAD,
    SKIPPED,
    SENSITIVE_PENDING
}
