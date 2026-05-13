package com.github.pcholt.locationprovider.model

sealed class GeofenceResult {
    data class Inside(val location: LocationResult.Success, val shape: GeofenceShape) : GeofenceResult()
    data class Outside(val location: LocationResult.Success, val shape: GeofenceShape) : GeofenceResult()
    data class LocationFailed(val result: LocationResult) : GeofenceResult()
}
