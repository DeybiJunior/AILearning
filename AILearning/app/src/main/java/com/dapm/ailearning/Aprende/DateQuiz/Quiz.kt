package com.dapm.ailearning.Aprende.DateQuiz

data class Quiz(
    val question: String,
    val options: List<String>,
    val correct_answer: String
)