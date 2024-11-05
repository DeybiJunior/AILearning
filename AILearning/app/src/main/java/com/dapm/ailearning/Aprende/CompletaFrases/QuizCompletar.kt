package com.dapm.ailearning.Aprende.CompletaFrases

data class QuizCompletar(
    val frase: String,
    val options: List<String>,
    val correct_answer: String
)