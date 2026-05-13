package com.github.pcholt.locationprovider.geofence

import com.github.pcholt.locationprovider.model.GeofenceShape
import com.github.pcholt.locationprovider.model.LatLng
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.pow
import kotlin.math.sin
import kotlin.math.sqrt

internal object HaversineCalculator {

    private const val EARTH_RADIUS_METERS = 6_371_000.0

    fun distanceMeters(lat1: Double, lng1: Double, lat2: Double, lng2: Double): Double {
        val dLat = Math.toRadians(lat2 - lat1)
        val dLng = Math.toRadians(lng2 - lng1)
        val a = sin(dLat / 2).pow(2) +
                cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) * sin(dLng / 2).pow(2)
        val c = 2 * atan2(sqrt(a), sqrt(1 - a))
        return EARTH_RADIUS_METERS * c
    }

    fun isInsideCircle(point: LatLng, circle: GeofenceShape.Circle): Boolean =
        distanceMeters(
            point.latitude, point.longitude,
            circle.centerLatitude, circle.centerLongitude
        ) <= circle.radiusMeters
}
