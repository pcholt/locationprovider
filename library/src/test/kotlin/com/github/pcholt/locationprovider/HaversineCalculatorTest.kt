package com.github.pcholt.locationprovider

import com.github.pcholt.locationprovider.geofence.HaversineCalculator
import com.github.pcholt.locationprovider.model.GeofenceShape
import com.github.pcholt.locationprovider.model.LatLng
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class HaversineCalculatorTest {

    @Test
    fun `distance between identical points is zero`() {
        assertEquals(0.0, HaversineCalculator.distanceMeters(51.5, -0.1, 51.5, -0.1), 0.001)
    }

    @Test
    fun `London to Paris is approximately 340km`() {
        // London: 51.5074, -0.1278  |  Paris: 48.8566, 2.3522
        val distance = HaversineCalculator.distanceMeters(51.5074, -0.1278, 48.8566, 2.3522)
        assertEquals(340_000.0, distance, 5_000.0)
    }

    @Test
    fun `point 1m inside circle boundary is inside`() {
        val circle = GeofenceShape.Circle(51.5, -0.1, 100.0)
        val point = LatLng(51.5, -0.1 + metersToLng(99.0, 51.5))
        assertTrue(HaversineCalculator.isInsideCircle(point, circle))
    }

    @Test
    fun `point 1m outside circle boundary is outside`() {
        val circle = GeofenceShape.Circle(51.5, -0.1, 100.0)
        val point = LatLng(51.5, -0.1 + metersToLng(101.0, 51.5))
        assertFalse(HaversineCalculator.isInsideCircle(point, circle))
    }

    @Test
    fun `centre point is inside circle`() {
        val circle = GeofenceShape.Circle(40.0, 0.0, 1000.0)
        assertTrue(HaversineCalculator.isInsideCircle(LatLng(40.0, 0.0), circle))
    }

    // Converts east-west metres at a given latitude to longitude degrees
    private fun metersToLng(meters: Double, lat: Double): Double {
        val earthCircumference = 2 * Math.PI * 6_371_000.0
        val latRad = Math.toRadians(lat)
        return (meters / (earthCircumference * Math.cos(latRad))) * 360.0
    }
}
