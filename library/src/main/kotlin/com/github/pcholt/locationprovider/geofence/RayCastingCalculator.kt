package com.github.pcholt.locationprovider.geofence

import com.github.pcholt.locationprovider.model.GeofenceShape
import com.github.pcholt.locationprovider.model.LatLng

internal object RayCastingCalculator {

    /**
     * Ray-casting algorithm: casts a horizontal ray from [point] in the +longitude direction
     * and counts edge crossings. Odd count = inside, even = outside.
     *
     * Limitation: antimeridian-crossing polygons (vertices spanning ±180°) are not supported.
     */
    fun isInsidePolygon(point: LatLng, polygon: GeofenceShape.Polygon): Boolean {
        val vertices = polygon.vertices
        var inside = false
        var j = vertices.size - 1

        for (i in vertices.indices) {
            val vi = vertices[i]
            val vj = vertices[j]
            if ((vi.longitude > point.longitude) != (vj.longitude > point.longitude) &&
                point.latitude < (vj.latitude - vi.latitude) *
                (point.longitude - vi.longitude) /
                (vj.longitude - vi.longitude) + vi.latitude
            ) {
                inside = !inside
            }
            j = i
        }
        return inside
    }
}
