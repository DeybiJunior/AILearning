package com.dapm.ailearning.SolicitudApi

import android.content.Context

object GeminiApiKeyStorage {
    const val GOOGLE_AI_STUDIO_URL = "https://aistudio.google.com/app/apikey"

    private const val PREFERENCES_NAME = "user_data"
    private const val API_KEY_PREFERENCE = "gemini_api_key"
    private const val MINIMUM_KEY_LENGTH = 20
    private const val MAXIMUM_KEY_LENGTH = 200

    fun get(context: Context): String? {
        return context.getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE)
            .getString(API_KEY_PREFERENCE, null)
            ?.let(::normalize)
            ?.takeIf(String::isNotEmpty)
    }

    fun save(context: Context, rawApiKey: String) {
        val apiKey = normalize(rawApiKey)
        val editor = context.getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE).edit()

        if (apiKey.isEmpty()) {
            editor.remove(API_KEY_PREFERENCE)
        } else {
            editor.putString(API_KEY_PREFERENCE, apiKey)
        }

        editor.apply()
    }

    fun clear(context: Context) {
        save(context, "")
    }

    fun normalize(rawApiKey: String): String {
        return rawApiKey.trim().removeSurrounding("\"").trim()
    }

    fun hasValidFormat(rawApiKey: String): Boolean {
        val apiKey = normalize(rawApiKey)
        return apiKey.length in MINIMUM_KEY_LENGTH..MAXIMUM_KEY_LENGTH &&
                apiKey.none(Char::isWhitespace)
    }
}
