package com.dapm.ailearning.SolicituLocal

import android.content.Context
import android.content.Intent
import android.util.Log
import com.dapm.ailearning.Aprende.Aprendeporrepeticion
import com.dapm.ailearning.Datos.Frase
import com.google.gson.Gson
import com.google.gson.JsonSyntaxException
import okhttp3.OkHttpClient
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object ApiService {

    var frasesList: List<Frase>? = null

    fun solicitarAPI(Tema: String, context: Context, onFailure: (Throwable?) -> Unit) {
        val okHttpClient = OkHttpClient.Builder()
            .connectTimeout(60, TimeUnit.SECONDS)
            .readTimeout(60, TimeUnit.SECONDS)
            .writeTimeout(60, TimeUnit.SECONDS)
            .build()

        // Configura Retrofit
        val retrofit = Retrofit.Builder()
            .baseUrl("http://192.168.0.101:11434/api/")
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        // Crea una instancia de la API
        val mistralApi = retrofit.create(MistralApi::class.java)

        // Construye el prompt con el formato requerido
        val prompt = "Genera un JSON con una lista de 4 frases en inglés de nivel básico - avanzado sobre $Tema. La estructura debe ser: [{ \"ID\": <número>, \"frase\": \"<String>\" }, ...]"
        Log.d("ApiService", "Prompt construido: $prompt") // Log para verificar el prompt construido

        // Crea el objeto de la solicitud con modelo y stream predeterminados
        val request = MistralRequest("mistral", prompt, false)

        // Realiza la solicitud
        val call = mistralApi.generatePrompt(request)

        call.enqueue(object : Callback<MistralResponse> {
            override fun onResponse(call: Call<MistralResponse>, response: Response<MistralResponse>) {
                if (response.isSuccessful) {
                    val mistralResponse = response.body()
                    if (mistralResponse != null && mistralResponse.done) {
                        val jsonFrases = mistralResponse.response
                        Log.d("ApiService", "Respuesta cruda: $jsonFrases") // Log para la respuesta cruda de la API
                        try {
                            // Cambiar aquí para deserializar directamente a una lista de Frase
                            frasesList = Gson().fromJson(jsonFrases, Array<Frase>::class.java).toList()
                            Log.d("ApiService", "Frases deserializadas: ${frasesList?.size}") // Log para verificar el número de frases deserializadas

                            // Iniciar el Activity y pasar las frases
                            val intent = Intent(context, Aprendeporrepeticion::class.java)
                            intent.putExtra("frases", Gson().toJson(frasesList))
                            context.startActivity(intent)
                        } catch (e: JsonSyntaxException) {
                            Log.e("ApiService", "Error de formato en la respuesta JSON: ${e.message}") // Log para errores de deserialización
                            onFailure(Exception("Error de formato en la respuesta JSON: ${e.message}"))
                        }
                    } else {
                        Log.e("ApiService", "Error: Respuesta de la API no está completa o no se pudo procesar")
                        onFailure(Exception("Error: Respuesta de la API no está completa o no se pudo procesar"))
                    }
                } else {
                    Log.e("ApiService", "Error en la respuesta de la API: Código ${response.code()}")
                    onFailure(Exception("Error en la respuesta de la API: Código ${response.code()}"))
                }
            }

            override fun onFailure(call: Call<MistralResponse>, t: Throwable) {
                Log.e("ApiService", "Error en la solicitud: ${t.message}") // Log para errores de la solicitud
                onFailure(t)
            }
        })
    }
}
