package com.dapm.ailearning.SolicituLocal

data class MistralRequest(
    val model: String,
    val prompt: String,
    val stream: Boolean
)

