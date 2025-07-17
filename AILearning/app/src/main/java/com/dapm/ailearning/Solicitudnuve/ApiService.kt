package com.dapm.ailearning.Solicitudnuve

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
    // Reemplaza con tu clave de API de Gemini (¡Mantén esta clave segura!)
    private const val API_KEY = "xxxxxxxxxxxxxxxxx"
    private const val MODEL_NAME = "gemini-2.0-flash"

    fun solicitarAGemini(prompt: String, context: Context, onFailure: (Throwable?) -> Unit, onSuccess: (String) -> Unit) {
        val loggingInterceptor = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }

        val apiKeyInterceptor = object : Interceptor {
            @Throws(IOException::class)
            override fun intercept(chain: Interceptor.Chain): okhttp3.Response {
                val originalRequest = chain.request()
                val newUrl = originalRequest.url.newBuilder()
                    .addQueryParameter("key", API_KEY)
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
            .addInterceptor(loggingInterceptor) // Opcional: para ver los logs de la petición y respuesta
            .build()

        // Configura Retrofit para la API de Gemini
        val retrofit = Retrofit.Builder()
            .baseUrl(BASE_URL_GEMINI)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        // Crea una instancia de la API de Gemini
        val geminiApi = retrofit.create(GeminiApi::class.java)

        // Construye el contenido del prompt para Gemini
        val geminiPrompt = GeminiRequest.Content(
            parts = listOf(GeminiRequest.Part(text = prompt))
        )
        val geminiRequest = GeminiRequest(contents = listOf(geminiPrompt))

        // Log para verificar el prompt construido para Gemini
        Log.d("ApiService", "Prompt construido para Gemini: $prompt")

        // Realiza la solicitud a la API de Gemini
        val call = geminiApi.generateContent(MODEL_NAME, API_KEY, geminiRequest)

        call.enqueue(object : Callback<GeminiResponse> {
            override fun onResponse(call: Call<GeminiResponse>, response: Response<GeminiResponse>) {
                if (response.isSuccessful) {
                    val geminiResponse = response.body()
                    if (geminiResponse != null && geminiResponse.candidates.isNotEmpty() && geminiResponse.candidates[0].content.parts.isNotEmpty()) {
                        val responseText = geminiResponse.candidates[0].content.parts[0].text
                        Log.d("ApiService", "Respuesta cruda de Gemini: $responseText") // Log para la respuesta cruda de la API de Gemini
                        try {
                            // Intentar deserializar la respuesta directamente a JSON (String)
                            onSuccess(responseText)
                        } catch (e: Exception) {
                            Log.e("ApiService", "Error al procesar la respuesta de Gemini: ${e.message}")
                            onFailure(Exception("Error al procesar la respuesta de Gemini: ${e.message}"))
                        }
                    } else {
                        Log.e("ApiService", "Error: Respuesta de Gemini incompleta o sin contenido")
                        onFailure(Exception("Error: Respuesta de Gemini incompleta o sin contenido"))
                    }
                } else {
                    Log.e("ApiService", "Error en la respuesta de Gemini: Código ${response.code()} - ${response.errorBody()?.string()}")
                    onFailure(Exception("Error en la respuesta de Gemini: Código ${response.code()}"))
                }
            }

            override fun onFailure(call: Call<GeminiResponse>, t: Throwable) {
                Log.e("ApiService", "Error en la solicitud a Gemini: ${t.message}")
                onFailure(t)
            }
        })
    }
}