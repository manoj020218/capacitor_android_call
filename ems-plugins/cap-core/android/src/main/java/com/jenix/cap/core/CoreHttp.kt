package com.jenix.cap.core

import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject

class CoreHttp(private val store: CoreStore) {
    private val client = OkHttpClient()
    private val jsonType = "application/json".toMediaType()

    fun postJson(path: String, body: JSONObject, preferDeviceToken: Boolean = false): JSONObject {
        val url = "${store.baseUrl() ?: error("Plugin not configured")}$path"
        val request = Request.Builder()
            .url(url)
            .headers(buildHeaders(preferDeviceToken))
            .post(body.toString().toRequestBody(jsonType))
            .build()
        client.newCall(request).execute().use { response ->
            val text = response.body?.string().orEmpty()
            if (!response.isSuccessful) error(text.ifBlank { "HTTP ${response.code}" })
            return JSONObject(text)
        }
    }

    fun patchJson(path: String, body: JSONObject, preferDeviceToken: Boolean = true): JSONObject {
        val url = "${store.baseUrl() ?: error("Plugin not configured")}$path"
        val request = Request.Builder()
            .url(url)
            .headers(buildHeaders(preferDeviceToken))
            .patch(body.toString().toRequestBody(jsonType))
            .build()
        client.newCall(request).execute().use { response ->
            val text = response.body?.string().orEmpty()
            if (!response.isSuccessful) error(text.ifBlank { "HTTP ${response.code}" })
            return JSONObject(text)
        }
    }

    private fun buildHeaders(preferDeviceToken: Boolean): okhttp3.Headers {
        val token = if (preferDeviceToken) {
            store.deviceToken() ?: store.userAccessToken()
        } else {
            store.userAccessToken() ?: store.deviceToken()
        }
        val builder = okhttp3.Headers.Builder()
        val headers = store.defaultHeaders()
        headers.keys().forEach { key -> builder.add(key, headers.getString(key)) }
        if (token != null) builder.add("Authorization", "Bearer $token")
        return builder.build()
    }
}
