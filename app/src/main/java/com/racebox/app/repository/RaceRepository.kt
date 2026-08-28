package com.racebox.app.repository

import android.content.Context
import com.racebox.app.data.db.AppDatabase
import com.racebox.app.data.db.entity.GpsPoint
import com.racebox.app.data.db.entity.Lap
import com.racebox.app.data.db.entity.Race
import com.racebox.app.data.db.entity.RaceDetail
import com.racebox.app.data.security.CryptoUtils
import com.racebox.app.domain.race.GpsSample
import com.racebox.app.domain.race.LapStats
import com.racebox.app.domain.race.RaceFinalized
import com.racebox.app.domain.race.RaceSession
import com.racebox.app.domain.race.TrackingState
import com.racebox.app.ui.notify.NotificationHelper
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class RaceRepository(
    private val database: AppDatabase,
    private val crypto: CryptoUtils,
    private val appContext: Context
) {

    private var session: RaceSession? = null

    private val _trackingState = MutableStateFlow<TrackingState?>(null)
    val trackingState: StateFlow<TrackingState?> = _trackingState.asStateFlow()

    fun isTracking(): Boolean = session != null

    suspend fun startRace(userId: Long): Boolean {
        if (session != null) return false
        val raceId = database.raceDao().insert(Race(userId = userId, startTime = System.currentTimeMillis()))
        val newSession = RaceSession()
        newSession.begin(userId, raceId)
        session = newSession
        updateState()
        return true
    }

    suspend fun recordSample(sample: GpsSample) {
        val current = session ?: return
        current.addSample(sample)
        current.activeLapId?.let { lapId ->
            database.gpsPointDao().insert(toPoint(lapId, sample))
        }
        updateState()
    }

    suspend fun beginLap(): Boolean {
        val current = session ?: return false
        if (current.hasActiveLap) return false
        openLap(current)
        updateState()
        return true
    }

    suspend fun endLap(): Boolean {
        val current = session ?: return false
        if (!current.hasActiveLap) return false
        finalizeActiveLap(current)
        updateState()
        return true
    }

    suspend fun stopRace(): RaceFinalized? {
        val current = session ?: return null
        session = null
        if (current.hasActiveLap) {
            finalizeActiveLap(current)
        } else if (current.hasPendingSamples()) {
            openLap(current)
            finalizeActiveLap(current)
        }
        val endTime = System.currentTimeMillis()
        val durationMillis = endTime - current.raceStartTime
        val avgSpeedKmh = if (durationMillis > 0) {
            current.totalDistanceKm / (durationMillis / 3_600_000.0)
        } else {
            0.0
        }
        database.raceDao().getById(current.raceId)?.let { stored ->
            database.raceDao().update(
                stored.copy(
                    endTime = endTime,
                    totalDistanceKm = current.totalDistanceKm,
                    avgSpeedKmh = avgSpeedKmh,
                    maxSpeedKmh = current.maxSpeedKmh
                )
            )
        }
        updateState()
        NotificationHelper.raceSaved(appContext)
        return RaceFinalized(
            raceId = current.raceId,
            totalDistanceKm = current.totalDistanceKm,
            avgSpeedKmh = avgSpeedKmh,
            maxSpeedKmh = current.maxSpeedKmh,
            durationMillis = durationMillis,
            lapCount = database.lapDao().lapsForRace(current.raceId).size
        )
    }

    private suspend fun openLap(current: RaceSession) {
        val startedAt = System.currentTimeMillis()
        val lapId = database.lapDao().insert(
            Lap(
                raceId = current.raceId,
                lapNumber = database.lapDao().nextLapNumber(current.raceId),
                startTime = startedAt,
                endTime = startedAt
            )
        )
        val slice = current.beginLap(lapId, startedAt)
        if (slice.samples.isNotEmpty()) {
            database.gpsPointDao().insertAll(slice.samples.map { toPoint(lapId, it) })
        }
    }

    private suspend fun finalizeActiveLap(current: RaceSession) {
        val slice = current.endLap() ?: return
        val stored = database.lapDao().lapById(slice.lapId) ?: return
        val distanceKm = LapStats.distanceKm(slice.samples)
        val avgKmh = LapStats.averageKmh(distanceKm, slice.startTime, slice.endTime)
        val maxKmh = LapStats.maxSpeedKmh(slice.samples)
        database.lapDao().update(
            stored.copy(
                endTime = slice.endTime,
                distanceKm = distanceKm,
                avgSpeedKmh = avgKmh,
                maxSpeedKmh = maxKmh
            )
        )
    }

    fun racesForUser(userId: Long): Flow<List<Race>> = database.raceDao().racesForUser(userId)

    suspend fun getRaceDetail(id: Long): RaceDetail? = database.raceDao().getDetail(id)

    suspend fun markRaceSynced(id: Long) {
        database.raceDao().markSynced(id)
        database.lapDao().markSyncedForRace(id)
    }

    private fun toPoint(lapId: Long, sample: GpsSample): GpsPoint =
        GpsPoint(
            lapId = lapId,
            timestamp = sample.timestamp,
            latitudeEnc = crypto.encryptDouble(sample.latitude),
            longitudeEnc = crypto.encryptDouble(sample.longitude),
            speedKmhEnc = crypto.encryptDouble(sample.speedKmh)
        )

    private fun updateState() {
        val current = session ?: run {
            _trackingState.value = null
            return
        }
        _trackingState.value = TrackingState(
            raceId = current.raceId,
            speedKmh = current.lastSpeedKmh,
            distanceKm = current.totalDistanceKm,
            elapsedMillis = current.elapsedSinceStart,
            maxSpeedKmh = current.maxSpeedKmh,
            lapNumber = current.lapNumber
        )
    }
}