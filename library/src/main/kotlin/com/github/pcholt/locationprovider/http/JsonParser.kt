package com.github.pcholt.locationprovider.http

import org.json.JSONObject

internal object JsonParser {

    /**
     * Parses a BigDataCloud ip-geolocation-with-confidence response.
     * Returns (latitude, longitude, accuracyMeters) or null on failure.
     */
    fun parseBigDataCloud(json: String): Triple<Double, Double, Float>? {
        return try {
            val obj = JSONObject(json)
            val loc = obj.optJSONObject("location") ?: return null
            val lat = loc.optDouble("latitude", Double.NaN)
            val lng = loc.optDouble("longitude", Double.NaN)
            if (lat.isNaN() || lng.isNaN()) return null
            // accuracyRadius is in km; convert to metres, default 50km if missing
            val accuracyKm = loc.optInt("accuracyRadius", 50)
            Triple(lat, lng, accuracyKm * 1000f)
        } catch (e: Exception) {
            null
        }
    }
}
