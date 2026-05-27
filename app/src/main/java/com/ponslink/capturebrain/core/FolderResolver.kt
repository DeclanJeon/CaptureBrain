package com.ponslink.capturebrain.core

class FolderResolver {
    fun resolve(ocrText: String, title: String? = null): String {
        val haystack = listOfNotNull(title, ocrText).joinToString(" ").lowercase()
        return when {
            listOf("error", "exception", "stack trace", "오류", "에러").any { it in haystack } -> "03_Development/Errors"
            listOf("api", "sdk", "docs", "documentation", "문서").any { it in haystack } -> "03_Development/Docs"
            listOf("price", "pricing", "요금", "가격", "₩", "$", "plan").any { it in haystack } -> "04_Business/Pricing"
            listOf("competitor", "landing", "cta", "광고", "마케팅").any { it in haystack } -> "04_Business/Marketing"
            listOf("lecture", "course", "강의", "학습", "study").any { it in haystack } -> "01_Learning/General"
            listOf("twitter", "x.com", "instagram", "threads", "sns").any { it in haystack } -> "05_Content/Social"
            listOf("cart", "shop", "product", "상품", "구매").any { it in haystack } -> "06_Shopping/Products"
            else -> "00_Inbox"
        }
    }
}
