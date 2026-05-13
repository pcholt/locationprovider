package com.github.pcholt.locationprovider

data class LocationConfig(
    val gpsTimeoutMs: Long = 15_000L,
    val networkTimeoutMs: Long = 10_000L,
    val ipTimeoutMs: Long = 8_000L,
    val minUpdateIntervalMs: Long = 5_000L,
    val minUpdateDistanceMeters: Float = 10f,
    val bigDataCloudApiKey: String = ""
)
