package com.dapm.ailearning.SolicitudApi

import android.content.Context
import android.util.Log
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.IOException
import java.util.concurrent.TimeUnit

object ApiService {

    private const val BASE_URL_GEMINI = "https://generativelanguage.googleapis.com/v1beta/"
    private const val DEFAULT_API_KEY = "AIzaSyABR_EFoUdy1r5YYG8_f0mUoqbaTdC2yDU" // fallback solo en dev

    private const val MODEL_NAME = "gemini-3-flash-preview"
    // Función auxiliar para obtener la key guardada por el usuario
    private fun obtenerApiKey(context: Context): String {
        val sharedPref = context.getSharedPreferences("user_data", Context.MODE_PRIVATE)
        val userKey = sharedPref.getString("gemini_api_key", null)
        return if (!userKey.isNullOrEmpty()) userKey else DEFAULT_API_KEY
    }

    fun solicitarAGemini(
        prompt: String,
        context: Context,
        onFailure: (Throwable?) -> Unit,
        onSuccess: (String) -> Unit
    ) {
        val apiKey = obtenerApiKey(context)

        val loggingInterceptor = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }

        val apiKeyInterceptor = object : Interceptor {
            @Throws(IOException::class)
            override fun intercept(chain: Interceptor.Chain): okhttp3.Response {
                val originalRequest = chain.request()
                val newUrl = originalRequest.url.newBuilder()
                    .addQueryParameter("key", apiKey)
                    .build()
                val newRequest = originalRequest.newBuilder()
                    .url(newUrl)
                    .build()
                return chain.proceed(newRequest)
            }
        }

        val okHttpClient = OkHttpClient.Builder()
            .connectTimeout(60, TimeUnit.SECONDS)
            .readTimeout(60, TimeUnit.SECONDS)
            .writeTimeout(60, TimeUnit.SECONDS)
            .addInterceptor(apiKeyInterceptor)
            .addInterceptor(loggingInterceptor)
            .build()

        val retrofit = Retrofit.Builder()
            .baseUrl(BASE_URL_GEMINI)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        val geminiApi = retrofit.create(GeminiApi::class.java)

        val geminiPrompt = GeminiRequest.Content(
            parts = listOf(GeminiRequest.Part(text = prompt))
        )
        val geminiRequest = GeminiRequest(contents = listOf(geminiPrompt))

        Log.d("ApiService", "Usando key: ${apiKey.take(8)}... | Prompt: $prompt")

        val call = geminiApi.generateContent(MODEL_NAME, geminiRequest)

        call.enqueue(object : Callback<GeminiResponse> {
            override fun onResponse(call: Call<GeminiResponse>, response: Response<GeminiResponse>) {
                if (response.isSuccessful) {
                    val geminiResponse = response.body()
                    if (geminiResponse != null && geminiResponse.candidates.isNotEmpty()) {
                        val responseText = geminiResponse.candidates[0].content.parts[0].text
                        onSuccess(responseText)
                    } else {
                        onFailure(Exception("Respuesta vacía de Gemini"))
                    }
                } else {
                    val errorCode = response.code()
                    val errorMsg = response.errorBody()?.string()
                    Log.e("ApiService", "Error $errorCode: $errorMsg")

                    // ✅ Mensaje de error más claro si es problema de autenticación
                    val mensaje = if (errorCode == 400 || errorCode == 403) {
                        "API Key inválida. Verifica tu clave en el Perfil."
                    } else {
                        "Error en Gemini: $errorCode"
                    }
                    onFailure(Exception(mensaje))
                }
            }

            override fun onFailure(call: Call<GeminiResponse>, t: Throwable) {
                onFailure(t)
            }
        })
    }
}
