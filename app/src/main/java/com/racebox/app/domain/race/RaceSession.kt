package com.racebox.app.domain.race

import com.racebox.app.domain.geo.GeoUtils

data class GpsSample(
    val timestamp: Long,
    val latitude: Double,
    val longitude: Double,
    val speedKmh: Double
)

class LapSlice(
    val lapId: Long,
    val lapNumber: Int,
    val startTime: Long,
    var endTime: Long = 0L,
    val samples: MutableList<GpsSample> = mutableListOf()
)

class RaceSession {

    var userId: Long = 0L
        private set
    var raceId: Long = 0L
        private set
    var raceStartTime: Long = 0L
        private set

    var totalDistanceKm: Double = 0.0
        private set
    var maxSpeedKmh: Double = 0.0
        private set
    var lastSpeedKmh: Double = 0.0
        private set

    private val pendingSamples = mutableListOf<GpsSample>()
    private var lastSample: GpsSample? = null
    private var activeSlice: LapSlice? = null
    private var completedLaps = 0

    val activeLapId: Long?
        get() = activeSlice?.lapId

    val lapNumber: Int
        get() = activeSlice?.lapNumber ?: 0

    val elapsedSinceStart: Long
        get() = lastSample?.timestamp?.minus(raceStartTime)
            ?: (System.currentTimeMillis() - raceStartTime)

    val hasActiveLap: Boolean
        get() = activeSlice != null

    fun begin(userId: Long, raceId: Long) {
        this.userId = userId
        this.raceId = raceId
        this.raceStartTime = System.currentTimeMillis()
        pendingSamples.clear()
        lastSample = null
        activeSlice = null
        completedLaps = 0
        totalDistanceKm = 0.0
        maxSpeedKmh = 0.0
        lastSpeedKmh = 0.0
    }

    fun beginLap(lapId: Long, startTime: Long): LapSlice {
        val slice = LapSlice(
            lapId = lapId,
            lapNumber = completedLaps + 1,
            startTime = startTime
        )
        slice.samples.addAll(pendingSamples)
        pendingSamples.clear()
        activeSlice = slice
        return slice
    }

    fun addSample(sample: GpsSample) {
        lastSpeedKmh = sample.speedKmh
        if (sample.speedKmh > maxSpeedKmh) maxSpeedKmh = sample.speedKmh
        lastSample?.let { previous ->
            totalDistanceKm +=
                GeoUtils.distanceMeters(
                    previous.latitude,
                    previous.longitude,
                    sample.latitude,
                    sample.longitude
                ) / 1000.0
        }
        lastSample = sample
        activeSlice?.samples?.add(sample) ?: pendingSamples.add(sample)
    }

    fun endLap(): LapSlice? {
        val slice = activeSlice ?: return null
        slice.endTime = slice.samples.lastOrNull()?.timestamp ?: System.currentTimeMillis()
        activeSlice = null
        completedLaps++
        return slice
    }

    fun hasPendingSamples(): Boolean = pendingSamples.isNotEmpty()
}