package com.dapm.ailearning.Datos

data class Frase(
    val id: Int,
    val frase: String
)


data class JsonResponse(
    val unik: List<Frase>
)

