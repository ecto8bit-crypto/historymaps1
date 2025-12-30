package com.example.historymaps.models

data class MilitaryEvent(
    val title: String,
    val description: String,
    val latitude: Double,
    val longitude: Double,
    val type: String // "battle", "landing", "capture", "surrender", "withdrawal"
)