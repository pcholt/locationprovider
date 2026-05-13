package com.github.pcholt.locationprovider.model

sealed class GeofenceShape {

    data class Circle(
        val centerLatitude: Double,
        val centerLongitude: Double,
        val radiusMeters: Double
    ) : GeofenceShape()

    /**
     * Polygon defined by an ordered list of vertices. Automatically closed.
     * Minimum 3 vertices required. Antimeridian-crossing polygons are not supported.
     */
    data class Polygon(
        val vertices: List<LatLng>
    ) : GeofenceShape() {
        init {
            require(vertices.size >= 3) { "Polygon requires at least 3 vertices" }
        }
    }
}
