package com.racebox.app.domain.race

data class RaceFinalized(
    val raceId: Long,
    val totalDistanceKm: Double,
    val avgSpeedKmh: Double,
    val maxSpeedKmh: Double,
    val durationMillis: Long,
    val lapCount: Int
)