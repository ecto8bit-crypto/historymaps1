package com.example.historymaps.model

import com.example.historymaps.geo.GeoUtils
import org.osmdroid.util.GeoPoint

class FrontModel(
    private val landingPoint: GeoPoint
) {

    fun frontForDay(day: Int): List<GeoPoint> {
        val radiusKm = 5.0 + day * 8.0
        val points = mutableListOf<GeoPoint>()

        for (angle in 0..360 step 5) {
            val point = GeoUtils.destinationPoint(
                landingPoint,
                angle.toDouble(),
                radiusKm
            )
            points.add(point)
        }

        return points
    }
}


