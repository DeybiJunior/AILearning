package com.dapm.ailearning.Aprende.DateQuiz

data class LeccionJson(
    val ID: Int,
    val reading: String,
    val quiz: List<Quiz>
)