package com.dapm.ailearning.SolicituLocal

import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.POST

interface MistralApi {
    @POST("generate")
    fun generatePrompt(@Body request: MistralRequest): Call<MistralResponse>
}

