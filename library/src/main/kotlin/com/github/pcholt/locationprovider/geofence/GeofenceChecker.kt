package com.github.pcholt.locationprovider.geofence

import com.github.pcholt.locationprovider.model.GeofenceShape
import com.github.pcholt.locationprovider.model.LatLng

class GeofenceChecker {

    fun isInside(point: LatLng, shape: GeofenceShape): Boolean = when (shape) {
        is GeofenceShape.Circle  -> HaversineCalculator.isInsideCircle(point, shape)
        is GeofenceShape.Polygon -> RayCastingCalculator.isInsidePolygon(point, shape)
    }
}
