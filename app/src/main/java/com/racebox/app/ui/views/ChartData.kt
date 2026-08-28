package com.racebox.app.ui.views

data class SpeedPoint(
    val offsetMillis: Long,
    val speedKmh: Double
)

data class HeatPoint(
    val latitude: Double,
    val longitude: Double,
    val speedKmh: Double
)