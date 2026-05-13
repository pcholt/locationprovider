package com.github.pcholt.locationprovider.model

sealed class LocationResult {

    data class Success(
        val latitude: Double,
        val longitude: Double,
        val accuracyMeters: Float,
        val source: ProviderSource,
        val timestampMs: Long = System.currentTimeMillis()
    ) : LocationResult()

    data class Failure(
        val reason: FailureReason,
        val cause: Throwable? = null
    ) : LocationResult()

    object Unavailable : LocationResult()
}

enum class FailureReason {
    PERMISSION_DENIED,
    ALL_PROVIDERS_FAILED,
    TIMEOUT,
    NETWORK_ERROR,
    PROVIDER_DISABLED
}
