package com.racebox.app.data.sync

import com.racebox.app.data.db.AppDatabase
import com.racebox.app.data.db.entity.Race
import com.racebox.app.data.prefs.SecurePrefs
import com.racebox.app.data.security.CryptoUtils
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class SyncRepository(
    private val database: AppDatabase,
    private val client: SyncClient,
    private val crypto: CryptoUtils,
    private val securePrefs: SecurePrefs
) {

    private val _syncing = MutableStateFlow(false)
    val syncing: StateFlow<Boolean> = _syncing.asStateFlow()

    suspend fun syncNow(): SyncResult {
        if (_syncing.value) return SyncResult.Skipped
        _syncing.value = true
        return try {
            val races = database.raceDao().unsynced()
            if (races.isEmpty()) {
                SyncResult.NoData
            } else {
                val response = client.sync(buildPayload(races))
                if (response.isSuccessful && response.body()?.ok == true) {
                    races.forEach { race ->
                        database.raceDao().markSynced(race.id)
                        database.lapDao().markSyncedForRace(race.id)
                    }
                    SyncResult.Success(races.size)
                } else {
                    SyncResult.Error("Server merespons kode ${response.code()}")
                }
            }
        } catch (e: Exception) {
            SyncResult.Error(e.message ?: "Gagal sinkronisasi")
        } finally {
            _syncing.value = false
        }
    }

    private suspend fun buildPayload(races: List<Race>): SyncPayload {
        val username = securePrefs.userSession()?.username ?: "anonymous"
        val syncRaces = races.map { race ->
            val detail = database.raceDao().getDetail(race.id)
            val laps = detail?.laps.orEmpty().map { entry ->
                SyncLap(
                    lapNumber = entry.lap.lapNumber,
                    startTime = entry.lap.startTime,
                    endTime = entry.lap.endTime,
                    distanceKm = entry.lap.distanceKm,
                    avgSpeedKmh = entry.lap.avgSpeedKmh,
                    maxSpeedKmh = entry.lap.maxSpeedKmh,
                    points = entry.points.map { point ->
                        SyncPoint(
                            timestamp = point.timestamp,
                            latitude = decryptOrZero { crypto.decryptDouble(point.latitudeEnc) },
                            longitude = decryptOrZero { crypto.decryptDouble(point.longitudeEnc) },
                            speedKmh = decryptOrZero { crypto.decryptDouble(point.speedKmhEnc) }
                        )
                    }
                )
            }
            SyncRace(
                raceId = race.id,
                startTime = race.startTime,
                endTime = race.endTime,
                totalDistanceKm = race.totalDistanceKm,
                avgSpeedKmh = race.avgSpeedKmh,
                maxSpeedKmh = race.maxSpeedKmh,
                laps = laps
            )
        }
        return SyncPayload(user = username, races = syncRaces)
    }

    private fun decryptOrZero(block: () -> Double): Double =
        try {
            block()
        } catch (_: Exception) {
            0.0
        }
}

sealed interface SyncResult {
    data class Success(val raceCount: Int) : SyncResult
    data class Error(val message: String) : SyncResult
    object NoData : SyncResult
    object Skipped : SyncResult
}