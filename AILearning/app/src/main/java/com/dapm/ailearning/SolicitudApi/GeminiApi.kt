package com.dapm.ailearning.SolicitudApi

import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Path

interface GeminiApi {
    @POST("models/{fullModelName}:generateContent")
    fun generateContent(
        @Path("fullModelName") fullModelName: String,
        @Body request: GeminiRequest
    ): Call<GeminiResponse>
}