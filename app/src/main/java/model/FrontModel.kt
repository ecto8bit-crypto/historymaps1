package com.example.historymaps.model

import com.example.historymaps.data.FrontScenario
import org.osmdroid.util.GeoPoint

class FrontModel(
    private val scenario: FrontScenario
) {

    val daysCount: Int
        get() = scenario.days.size

    fun frontForDay(day: Int): List<GeoPoint> {
        return scenario.days
            .getOrNull(day)
            ?: scenario.days.last()
    }
}



