package com.example.historymaps.model

import com.example.historymaps.data.FrontScenario
import org.osmdroid.util.GeoPoint

class FrontModel(private val scenario: List<List<GeoPoint>>) {

    // Количество дней в сценарии
    val daysCount: Int
        get() = scenario.size

    // Даты для каждого дня
    val dates = listOf(
        "9 апреля 1940 - Начало вторжения",
        "10-14 апреля - Высадка союзников",
        "15-30 апреля - Бои за центральную Норвегию",
        "1-7 мая - Продвижение на север",
        "8-28 мая - Битва за Нарвик",
        "29 мая - 10 июня - Эвакуация",
        "10 июня - Капитуляция Норвегии"
    )

    // Получить линию фронта для дня
    fun frontForDay(day: Int): List<GeoPoint> {
        return if (day in 0 until scenario.size) {
            scenario[day]
        } else {
            emptyList() // или scenario.last()
        }
    }

    // Получить описание для дня
    fun dateForDay(day: Int): String {
        return if (day in 0 until dates.size) {
            dates[day]
        } else {
            "День $day"
        }
    }

    // Получить границы для автоматического зума
    fun getBoundsForDay(day: Int): Pair<GeoPoint, GeoPoint>? {
        val points = frontForDay(day)
        if (points.isEmpty()) return null

        var minLat = 90.0
        var maxLat = -90.0
        var minLon = 180.0
        var maxLon = -180.0

        for (point in points) {
            minLat = minOf(minLat, point.latitude)
            maxLat = maxOf(maxLat, point.latitude)
            minLon = minOf(minLon, point.longitude)
            maxLon = maxOf(maxLon, point.longitude)
        }

        return Pair(GeoPoint(minLat, minLon), GeoPoint(maxLat, maxLon))
    }
}