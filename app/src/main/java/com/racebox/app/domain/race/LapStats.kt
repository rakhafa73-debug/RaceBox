package com.racebox.app.domain.race

import com.racebox.app.domain.geo.GeoUtils

object LapStats {

    fun distanceKm(samples: List<GpsSample>): Double {
        if (samples.size < 2) return 0.0
        return samples.zipWithNext()
            .sumOf { (a, b) ->
                GeoUtils.distanceMeters(a.latitude, a.longitude, b.latitude, b.longitude)
            } / 1000.0
    }

    fun averageKmh(distanceKm: Double, startTime: Long, endTime: Long): Double {
        val hours = (endTime - startTime) / 3_600_000.0
        return if (hours <= 0) 0.0 else distanceKm / hours
    }

    fun maxSpeedKmh(samples: List<GpsSample>): Double =
        samples.maxOfOrNull { it.speedKmh } ?: 0.0
}