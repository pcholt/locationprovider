package com.github.pcholt.locationprovider.http

import java.net.HttpURLConnection
import java.net.URL

internal object HttpClient {

    fun get(url: String, timeoutMs: Int = 8_000): String {
        val connection = URL(url).openConnection() as HttpURLConnection
        connection.apply {
            requestMethod = "GET"
            connectTimeout = timeoutMs
            readTimeout = timeoutMs
            setRequestProperty("Accept", "application/json")
            setRequestProperty("User-Agent", "locationprovider-android/1.0")
        }
        return try {
            connection.inputStream.bufferedReader().use { it.readText() }
        } finally {
            connection.disconnect()
        }
    }
}
