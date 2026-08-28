package com.racebox.app.ui.summary

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.racebox.app.data.security.CryptoUtils
import com.racebox.app.repository.RaceRepository
import com.racebox.app.ui.views.HeatPoint
import com.racebox.app.ui.views.SpeedPoint
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class SummaryViewModel(
    private val repository: RaceRepository,
    private val crypto: CryptoUtils
) : ViewModel() {

    private val _ui = MutableStateFlow<SummaryUiState?>(null)
    val ui: StateFlow<SummaryUiState?> = _ui.asStateFlow()

    fun load(raceId: Long) {
        viewModelScope.launch {
            val detail = repository.getRaceDetail(raceId) ?: return@launch
            val speedSeries = mutableListOf<SpeedPoint>()
            val heatPoints = mutableListOf<HeatPoint>()
            val laps = mutableListOf<LapSummary>()

            detail.laps.forEach { entry ->
                entry.points.forEach { point ->
                    val latitude = decryptOrZero { crypto.decryptDouble(point.latitudeEnc) }
                    val longitude = decryptOrZero { crypto.decryptDouble(point.longitudeEnc) }
                    val speed = decryptOrZero { crypto.decryptDouble(point.speedKmhEnc) }
                    speedSeries.add(SpeedPoint(point.timestamp - detail.race.startTime, speed))
                    heatPoints.add(HeatPoint(latitude, longitude, speed))
                }
                laps.add(
                    LapSummary(
                        number = entry.lap.lapNumber,
                        distanceKm = entry.lap.distanceKm,
                        avgSpeedKmh = entry.lap.avgSpeedKmh,
                        maxSpeedKmh = entry.lap.maxSpeedKmh,
                        durationMillis = entry.lap.endTime - entry.lap.startTime
                    )
                )
            }

            _ui.value = SummaryUiState(
                raceId = detail.race.id,
                startTime = detail.race.startTime,
                distanceKm = detail.race.totalDistanceKm,
                avgSpeedKmh = detail.race.avgSpeedKmh,
                maxSpeedKmh = detail.race.maxSpeedKmh,
                durationMillis = (detail.race.endTime ?: detail.race.startTime) - detail.race.startTime,
                lapCount = detail.laps.size,
                isSynced = detail.race.isSynced,
                speedSeries = speedSeries,
                heatPoints = heatPoints,
                laps = laps
            )
        }
    }

    fun reload(raceId: Long) {
        load(raceId)
    }

    private fun decryptOrZero(block: () -> Double): Double =
        try {
            block()
        } catch (_: Exception) {
            0.0
        }
}

data class SummaryUiState(
    val raceId: Long,
    val startTime: Long,
    val distanceKm: Double,
    val avgSpeedKmh: Double,
    val maxSpeedKmh: Double,
    val durationMillis: Long,
    val lapCount: Int,
    val isSynced: Boolean,
    val speedSeries: List<SpeedPoint>,
    val heatPoints: List<HeatPoint>,
    val laps: List<LapSummary>
)

data class LapSummary(
    val number: Int,
    val distanceKm: Double,
    val avgSpeedKmh: Double,
    val maxSpeedKmh: Double,
    val durationMillis: Long
)