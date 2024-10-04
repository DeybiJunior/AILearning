package com.dapm.ailearning.SolicituLocal

data class MistralResponse(
    val model: String,
    val created_at: String,
    val response: String,
    val done: Boolean,
    val done_reason: String
)