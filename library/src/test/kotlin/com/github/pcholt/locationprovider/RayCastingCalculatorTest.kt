package com.github.pcholt.locationprovider

import com.github.pcholt.locationprovider.geofence.RayCastingCalculator
import com.github.pcholt.locationprovider.model.GeofenceShape
import com.github.pcholt.locationprovider.model.LatLng
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class RayCastingCalculatorTest {

    // Unit square: (0,0) → (1,0) → (1,1) → (0,1)
    // lat = y-axis, lng = x-axis
    private val unitSquare = GeofenceShape.Polygon(
        listOf(
            LatLng(0.0, 0.0),
            LatLng(0.0, 1.0),
            LatLng(1.0, 1.0),
            LatLng(1.0, 0.0)
        )
    )

    @Test
    fun `centre of unit square is inside`() {
        assertTrue(RayCastingCalculator.isInsidePolygon(LatLng(0.5, 0.5), unitSquare))
    }

    @Test
    fun `point outside unit square is outside`() {
        assertFalse(RayCastingCalculator.isInsidePolygon(LatLng(2.0, 2.0), unitSquare))
    }

    @Test
    fun `point to the left of unit square is outside`() {
        assertFalse(RayCastingCalculator.isInsidePolygon(LatLng(0.5, -0.5), unitSquare))
    }

    @Test
    fun `point below unit square is outside`() {
        assertFalse(RayCastingCalculator.isInsidePolygon(LatLng(-0.5, 0.5), unitSquare))
    }

    @Test
    fun `centroid of triangle is inside`() {
        val triangle = GeofenceShape.Polygon(
            listOf(LatLng(0.0, 0.0), LatLng(0.0, 6.0), LatLng(6.0, 3.0))
        )
        // centroid ≈ (2.0, 3.0)
        assertTrue(RayCastingCalculator.isInsidePolygon(LatLng(2.0, 3.0), triangle))
    }

    @Test
    fun `point outside triangle is outside`() {
        val triangle = GeofenceShape.Polygon(
            listOf(LatLng(0.0, 0.0), LatLng(0.0, 6.0), LatLng(6.0, 3.0))
        )
        assertFalse(RayCastingCalculator.isInsidePolygon(LatLng(5.0, 0.5), triangle))
    }

    @Test
    fun `L-shaped polygon — point in arm is inside`() {
        // L-shape vertices (clockwise):
        // (0,0), (0,2), (1,2), (1,1), (2,1), (2,0)
        val lShape = GeofenceShape.Polygon(
            listOf(
                LatLng(0.0, 0.0), LatLng(2.0, 0.0), LatLng(2.0, 1.0),
                LatLng(1.0, 1.0), LatLng(1.0, 2.0), LatLng(0.0, 2.0)
            )
        )
        assertTrue(RayCastingCalculator.isInsidePolygon(LatLng(0.5, 0.5), lShape))  // lower arm
        assertTrue(RayCastingCalculator.isInsidePolygon(LatLng(0.5, 1.5), lShape))  // upper arm
    }

    @Test
    fun `L-shaped polygon — point in concave notch is outside`() {
        val lShape = GeofenceShape.Polygon(
            listOf(
                LatLng(0.0, 0.0), LatLng(2.0, 0.0), LatLng(2.0, 1.0),
                LatLng(1.0, 1.0), LatLng(1.0, 2.0), LatLng(0.0, 2.0)
            )
        )
        assertFalse(RayCastingCalculator.isInsidePolygon(LatLng(1.5, 1.5), lShape))
    }
}
