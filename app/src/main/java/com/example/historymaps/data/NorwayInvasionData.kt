package com.example.historymaps.data

import com.example.historymaps.models.FrontLine
import com.example.historymaps.models.MilitaryEvent
import java.text.SimpleDateFormat
import java.util.*

object NorwayInvasionData {

    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

    // Данные линий фронта по датам
    fun getFrontLines(): List<FrontLine> {
        return listOf(
            FrontLine(
                date = dateFormat.parse("1940-04-09")!!,
                coordinates = listOf(
                    Coordinate(58.0, 6.0),   // Ставангер
                    Coordinate(60.0, 5.0),   // Берген
                    Coordinate(63.0, 10.0),  // Тронхейм
                    Coordinate(69.0, 18.0)   // Нарвик
                ),
                description = "Начало вторжения. Немецкие войска высаживаются в ключевых портах."
            ),
            FrontLine(
                date = dateFormat.parse("1940-04-14")!!,
                coordinates = listOf(
                    Coordinate(59.0, 6.5),
                    Coordinate(60.5, 5.5),
                    Coordinate(63.5, 10.5),
                    Coordinate(68.5, 17.0)
                ),
                description = "Продвижение немецких войск вглубь страны."
            ),
            FrontLine(
                date = dateFormat.parse("1940-04-30")!!,
                coordinates = listOf(
                    Coordinate(60.0, 7.0),
                    Coordinate(61.0, 6.0),
                    Coordinate(64.0, 11.0),
                    Coordinate(67.0, 15.0)
                ),
                description = "Установление контроля над южной Норвегией."
            ),
            FrontLine(
                date = dateFormat.parse("1940-06-10")!!,
                coordinates = listOf(
                    Coordinate(62.0, 8.0),
                    Coordinate(63.0, 7.0),
                    Coordinate(65.0, 12.0),
                    Coordinate(66.0, 13.0)
                ),
                description = "Завершение операции. Полный контроль Германии над Норвегией."
            )
        )
    }

    // События по датам
    fun getEventsForDate(dateIndex: Int): List<MilitaryEvent> {
        return when (dateIndex) {
            0 -> listOf(
                MilitaryEvent(
                    title = "Высадка в Осло",
                    description = "Захват столицы немецкими войсками",
                    latitude = 59.9139,
                    longitude = 10.7522,
                    type = "landing"
                ),
                MilitaryEvent(
                    title = "Захват Бергена",
                    description = "Порт захвачен немецким десантом",
                    latitude = 60.3913,
                    longitude = 5.3221,
                    type = "capture"
                ),
                MilitaryEvent(
                    title = "Захват Тронхейма",
                    description = "Важный порт на севере",
                    latitude = 63.4305,
                    longitude = 10.3951,
                    type = "capture"
                )
            )
            1 -> listOf(
                MilitaryEvent(
                    title = "Битва за Нарвик",
                    description = "Первое морское сражение",
                    latitude = 68.4384,
                    longitude = 17.4273,
                    type = "battle"
                ),
                MilitaryEvent(
                    title = "Высадка союзников",
                    description = "Британские войска в Намсусе",
                    latitude = 64.4667,
                    longitude = 11.5000,
                    type = "landing"
                )
            )
            2 -> listOf(
                MilitaryEvent(
                    title = "Эвакуация союзников",
                    description = "Британские войска покидают Норвегию",
                    latitude = 63.1118,
                    longitude = 7.7380,
                    type = "withdrawal"
                )
            )
            3 -> listOf(
                MilitaryEvent(
                    title = "Капитуляция Норвегии",
                    description = "Официальная капитуляция норвежской армии",
                    latitude = 59.9139,
                    longitude = 10.7522,
                    type = "surrender"
                )
            )
            else -> emptyList()
        }
    }
}

// Вспомогательные классы
data class Coordinate(val latitude: Double, val longitude: Double)