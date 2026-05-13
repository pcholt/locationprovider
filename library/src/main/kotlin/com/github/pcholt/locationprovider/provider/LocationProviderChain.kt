package com.github.pcholt.locationprovider.provider

import android.content.Context
import com.github.pcholt.locationprovider.LocationConfig
import com.github.pcholt.locationprovider.model.LocationResult
import com.github.pcholt.locationprovider.model.ProviderSource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.withTimeoutOrNull

internal class LocationProviderChain(
    context: Context,
    private val config: LocationConfig
) {
    private val strategies: List<LocationStrategy> = listOf(
        GpsLocationProvider(context),
        NetworkLocationProvider(context),
        IpLocationProvider(config)
    )

    suspend fun getLocation(): LocationResult {
        for (strategy in strategies) {
            if (!strategy.isAvailable()) continue
            val result = withTimeoutOrNull(timeoutFor(strategy)) { strategy.fetchLocation() }
            if (result is LocationResult.Success) return result
        }
        return LocationResult.Unavailable
    }

    /**
     * Emits location updates from the highest-priority available provider.
     * If that provider becomes disabled, the flow ends (caller can restart or switch).
     */
    fun locationUpdates(): Flow<LocationResult> = flow {
        for (strategy in strategies) {
            if (!strategy.isAvailable()) continue
            emitAll(strategy.updates(config.minUpdateIntervalMs, config.minUpdateDistanceMeters))
            return@flow
        }
        // All hardware providers unavailable — emit a single IP fix
        val ipStrategy = strategies.first { it.source() == ProviderSource.IP }
        emit(ipStrategy.fetchLocation())
    }

    private fun timeoutFor(strategy: LocationStrategy): Long = when (strategy.source()) {
        ProviderSource.GPS     -> config.gpsTimeoutMs
        ProviderSource.NETWORK -> config.networkTimeoutMs
        ProviderSource.IP      -> config.ipTimeoutMs
    }
}
