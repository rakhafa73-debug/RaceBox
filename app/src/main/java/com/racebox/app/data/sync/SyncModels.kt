package com.racebox.app.data.sync

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

data class SyncPayload(
    val user: String,
    val races: List<SyncRace>
)

data class SyncRace(
    val raceId: Long,
    val startTime: Long,
    val endTime: Long?,
    val totalDistanceKm: Double,
    val avgSpeedKmh: Double,
    val maxSpeedKmh: Double,
    val laps: List<SyncLap>
)

data class SyncLap(
    val lapNumber: Int,
    val startTime: Long,
    val endTime: Long,
    val distanceKm: Double,
    val avgSpeedKmh: Double,
    val maxSpeedKmh: Double,
    val points: List<SyncPoint>
)

data class SyncPoint(
    val timestamp: Long,
    val latitude: Double,
    val longitude: Double,
    val speedKmh: Double
)

data class SyncResponse(
    val ok: Boolean,
    val message: String? = null
)

interface RaceBoxApi {

    @POST("sync")
    suspend fun sync(@Body payload: SyncPayload): Response<SyncResponse>
}