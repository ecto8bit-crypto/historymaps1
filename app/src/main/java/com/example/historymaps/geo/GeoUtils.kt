package com.example.historymaps.geo

import org.osmdroid.util.GeoPoint
import kotlin.math.*

object GeoUtils {

    fun destinationPoint(
        start: GeoPoint,
        bearing: Double,
        distanceKm: Double
    ): GeoPoint {

        val R = 6371.0 // радиус Земли
        val brng = Math.toRadians(bearing)
        val d = distanceKm / R

        val lat1 = Math.toRadians(start.latitude)
        val lon1 = Math.toRadians(start.longitude)

        val lat2 = asin(
            sin(lat1) * cos(d) +
                    cos(lat1) * sin(d) * cos(brng)
        )

        val lon2 = lon1 + atan2(
            sin(brng) * sin(d) * cos(lat1),
            cos(d) - sin(lat1) * sin(lat2)
        )

        return GeoPoint(
            Math.toDegrees(lat2),
            Math.toDegrees(lon2)
        )
    }
}

