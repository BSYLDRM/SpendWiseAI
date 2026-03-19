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

class GeminiRestClient(
    private val apiKey: String,
    private val modelName: String = "gemini-1.5-flash"
) {
    suspend fun generateContentText(prompt: String): String = withContext(Dispatchers.IO) {
        val encodedKey = URLEncoder.encode(apiKey, "UTF-8")
        val endpoint =
            "https://generativelanguage.googleapis.com/v1beta/models/$modelName:generateContent?key=$encodedKey"

        val payload = JSONObject().apply {
            put(
                "contents",
                JSONArray().put(
                    JSONObject().apply {
                        put(
                            "parts",
                            JSONArray().put(
                                JSONObject().apply { put("text", prompt) }
                            )
                        )
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

        val response = if (connection.responseCode in 200..299) {
            connection.inputStream
        } else {
            connection.errorStream ?: connection.inputStream
        }

        val responseBody = BufferedReader(InputStreamReader(response)).use { it.readText() }
        val json = JSONObject(responseBody)

        val text = json
            .optJSONArray("candidates")
            ?.optJSONObject(0)
            ?.optJSONObject("content")
            ?.optJSONArray("parts")
            ?.optJSONObject(0)
            ?.optString("text")
            .orEmpty()

        if (text.isBlank()) {
            // Include response body to help diagnose prompt/model issues.
            throw IllegalStateException("Gemini response did not contain text. Body=$responseBody")
        }

        text
    }
}

