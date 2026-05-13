package com.github.pcholt.locationprovider

import android.content.Context
import com.github.pcholt.locationprovider.geofence.GeofenceChecker
import com.github.pcholt.locationprovider.model.GeofenceResult
import com.github.pcholt.locationprovider.model.GeofenceShape
import com.github.pcholt.locationprovider.model.LatLng
import com.github.pcholt.locationprovider.model.LocationResult
import com.github.pcholt.locationprovider.provider.LocationProviderChain
import kotlinx.coroutines.flow.Flow

class LocationProvider(
    context: Context,
    private val config: LocationConfig = LocationConfig()
) {
    private val chain = LocationProviderChain(context.applicationContext, config)
    private val geofenceChecker = GeofenceChecker()

    /**
     * One-shot location fetch using the GPS → Network → IP fallback chain.
     * Suspends until a result is obtained or all providers are exhausted.
     */
    suspend fun getLocation(): LocationResult = chain.getLocation()

    /**
     * Continuous location updates as a cold Flow.
     * Emits from the highest-priority available provider.
     * Cancel the collecting coroutine to stop updates.
     */
    fun locationUpdates(): Flow<LocationResult> = chain.locationUpdates()

    /**
     * Checks whether [location] is inside [shape]. Pure synchronous computation.
     */
    fun isInsideGeofence(location: LocationResult.Success, shape: GeofenceShape): Boolean =
        geofenceChecker.isInside(LatLng(location.latitude, location.longitude), shape)

    /**
     * Fetches the current location then checks it against [shape].
     */
    suspend fun checkGeofence(shape: GeofenceShape): GeofenceResult {
        return when (val result = getLocation()) {
            is LocationResult.Success -> {
                val point = LatLng(result.latitude, result.longitude)
                if (geofenceChecker.isInside(point, shape)) {
                    GeofenceResult.Inside(result, shape)
                } else {
                    GeofenceResult.Outside(result, shape)
                }
            }
            else -> GeofenceResult.LocationFailed(result)
        }
    }
}
