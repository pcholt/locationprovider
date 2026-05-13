package com.github.pcholt.locationprovider.provider

import com.github.pcholt.locationprovider.model.LocationResult
import com.github.pcholt.locationprovider.model.ProviderSource
import kotlinx.coroutines.flow.Flow

internal interface LocationStrategy {
    fun isAvailable(): Boolean
    suspend fun fetchLocation(): LocationResult
    fun updates(minIntervalMs: Long, minDistanceM: Float): Flow<LocationResult>
    fun source(): ProviderSource
}
