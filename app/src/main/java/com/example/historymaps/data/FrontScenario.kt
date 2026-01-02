package com.example.historymaps.data

import org.osmdroid.util.GeoPoint

data class FrontScenario(
    val days: List<List<GeoPoint>>
) {
    companion object {

        val narvikLanding = FrontScenario(
            days = listOf(

                // День 0 — высадка
                listOf(
                    GeoPoint(68.438, 17.427),
                    GeoPoint(68.42, 17.45),
                    GeoPoint(68.40, 17.42)
                ),

                // День 1
                listOf(
                    GeoPoint(68.45, 17.40),
                    GeoPoint(68.43, 17.48),
                    GeoPoint(68.38, 17.43)
                ),

                // День 2
                listOf(
                    GeoPoint(68.48, 17.38),
                    GeoPoint(68.45, 17.50),
                    GeoPoint(68.36, 17.45)
                )
            )
        )
    }
}
