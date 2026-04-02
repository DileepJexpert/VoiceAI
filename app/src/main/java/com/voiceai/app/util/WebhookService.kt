package com.voiceai.app.util

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.stringPreferencesKey
import com.google.gson.Gson
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class WebhookService @Inject constructor(
    private val okHttpClient: OkHttpClient,
    private val dataStore: DataStore<Preferences>
) {

    companion object {
        private val WEBHOOK_URL_KEY = stringPreferencesKey("webhook_url")
        private val JSON_MEDIA_TYPE = "application/json; charset=utf-8".toMediaType()
    }

    private val gson = Gson()

    suspend fun sendEvent(eventType: String, data: Map<String, Any>) {
        val webhookUrl = getWebhookUrl() ?: return

        val payload = mapOf(
            "event_type" to eventType,
            "timestamp" to System.currentTimeMillis(),
            "data" to data
        )

        val jsonBody = gson.toJson(payload)
        val requestBody = jsonBody.toRequestBody(JSON_MEDIA_TYPE)

        val request = Request.Builder()
            .url(webhookUrl)
            .post(requestBody)
            .addHeader("Content-Type", "application/json")
            .build()

        okHttpClient.newCall(request).execute().use { response ->
            if (!response.isSuccessful) {
                throw RuntimeException("Webhook request failed with status: ${response.code}")
            }
        }
    }

    private suspend fun getWebhookUrl(): String? {
        return dataStore.data.map { preferences ->
            preferences[WEBHOOK_URL_KEY]
        }.first()
    }
}
