package com.dapm.ailearning.Solicitudnuve

data class GeminiRequest(
    val contents: List<Content>
) {
    data class Content(
        val parts: List<Part>
    )

    data class Part(
        val text: String
    )
}