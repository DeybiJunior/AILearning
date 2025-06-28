package com.dapm.ailearning.Solicitudnuve

import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface GeminiApi {
    @POST("models/{fullModelName}:generateContent")
    fun generateContent(
        @Path("fullModelName") fullModelName: String,
        @Query("key") apiKey: String,
        @Body request: GeminiRequest
    ): Call<GeminiResponse>
}