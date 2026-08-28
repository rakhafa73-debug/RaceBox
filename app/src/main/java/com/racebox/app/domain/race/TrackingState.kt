package com.racebox.app.domain.race

data class TrackingState(
    val raceId: Long,
    val speedKmh: Double,
    val distanceKm: Double,
    val elapsedMillis: Long,
    val maxSpeedKmh: Double,
    val lapNumber: Int
)