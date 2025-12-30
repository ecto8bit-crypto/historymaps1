package com.example.historymaps.models

import java.util.Date

data class FrontLine(
    val date: Date,
    val coordinates: List<com.example.historymaps.data.Coordinate>,
    val description: String = ""
)