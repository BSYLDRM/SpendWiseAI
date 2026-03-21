package com.example.spendwiseai.ai.gemini

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder
import android.util.Base64
import android.util.Log

class GeminiRestClient(
    private val apiKey: String
) {
    private val tag = "GeminiRestClient"

    // ZORUNLU KILDIĞIMIZ MODEL: Uygulama sadece bunu kullanacak!
    private val modelName = "gemini-2.5-flash"

    suspend fun generateContentText(prompt: String): String = withContext(Dispatchers.IO) {
        val encodedKey = URLEncoder.encode(apiKey, "UTF-8")
        val endpoint =
            "https://generativelanguage.googleapis.com/v1beta/models/$modelName:generateContent?key=$encodedKey"

        val payload = JSONObject().apply {
            put(
                "contents",
                JSONArray().put(
                    JSONObject().apply {
                        put("parts", JSONArray().put(JSONObject().apply { put("text", prompt) }))
                    }
                )
            )
            put(
                "generationConfig",
                JSONObject().apply {
                    put("temperature", 0.2)
                    put("maxOutputTokens", 300)
                }
            )
        }

        val url = URL(endpoint)
        val connection = (url.openConnection() as HttpURLConnection).apply {
            requestMethod = "POST"
            doOutput = true
            setRequestProperty("Content-Type", "application/json; charset=utf-8")
        }

        connection.outputStream.bufferedWriter().use { it.write(payload.toString()) }

        val responseCode = connection.responseCode
        val responseStream = connection.errorStream ?: connection.inputStream
        val responseBody = BufferedReader(InputStreamReader(responseStream)).use { it.readText() }

        if (responseCode !in 200..299) {
            Log.e(tag, "Gemini text request failed. url=$endpoint code=$responseCode body=$responseBody")
            throw IllegalStateException("Gemini text request failed. code=$responseCode body=$responseBody")
        }

        val json = JSONObject(responseBody)
        val text = json.optJSONArray("candidates")
            ?.optJSONObject(0)
            ?.optJSONObject("content")
            ?.optJSONArray("parts")
            ?.optJSONObject(0)
            ?.optString("text").orEmpty()

        if (text.isBlank()) {
            throw IllegalStateException("Gemini response did not contain text.")
        }

        text
    }

    suspend fun generateContentTextWithImage(
        prompt: String,
        imageBytes: ByteArray,
        mimeType: String = "image/jpeg"
    ): String = withContext(Dispatchers.IO) {
        val encodedKey = URLEncoder.encode(apiKey, "UTF-8")
        val endpoint =
            "https://generativelanguage.googleapis.com/v1beta/models/$modelName:generateContent?key=$encodedKey"

        val base64 = Base64.encodeToString(imageBytes, Base64.NO_WRAP)

        val payload = JSONObject().apply {
            put(
                "contents",
                JSONArray().put(
                    JSONObject().apply {
                        put(
                            "parts",
                            JSONArray()
                                .put(JSONObject().apply { put("text", prompt) })
                                .put(
                                    JSONObject().apply {
                                        put(
                                            "inlineData",
                                            JSONObject().apply {
                                                put("mimeType", mimeType)
                                                put("data", base64)
                                            }
                                        )
                                    }
                                )
                        )
                    }
                )
            )
            put(
                "generationConfig",
                JSONObject().apply {
                    put("temperature", 0.2)
                    put("maxOutputTokens", 30)
                }
            )
        }

        val url = URL(endpoint)
        val connection = (url.openConnection() as HttpURLConnection).apply {
            requestMethod = "POST"
            doOutput = true
            setRequestProperty("Content-Type", "application/json; charset=utf-8")
        }

        connection.outputStream.bufferedWriter().use { it.write(payload.toString()) }

        val responseCode = connection.responseCode
        val responseStream = connection.errorStream ?: connection.inputStream
        val responseBody = BufferedReader(InputStreamReader(responseStream)).use { it.readText() }

        if (responseCode !in 200..299) {
            Log.e(tag, "Gemini vision request failed. url=$endpoint code=$responseCode body=$responseBody")
            throw IllegalStateException("Gemini vision request failed. code=$responseCode body=$responseBody")
        }

        val json = JSONObject(responseBody)
        val text = json.optJSONArray("candidates")
            ?.optJSONObject(0)
            ?.optJSONObject("content")
            ?.optJSONArray("parts")
            ?.optJSONObject(0)
            ?.optString("text").orEmpty()

        if (text.isBlank()) {
            throw IllegalStateException("Gemini response did not contain text.")
        }

        text
    }
}