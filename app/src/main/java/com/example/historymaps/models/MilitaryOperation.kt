package com.example.historymaps.models

data class MilitaryOperation(
    val id: Int,
    val title: String,
    val subtitle: String,
    val period: String,
    val description: String,
    val iconRes: Int
)