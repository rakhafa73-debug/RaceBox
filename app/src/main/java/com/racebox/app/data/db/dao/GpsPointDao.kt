package com.racebox.app.data.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.racebox.app.data.db.entity.GpsPoint

@Dao
interface GpsPointDao {

    @Insert
    suspend fun insert(point: GpsPoint)

    @Insert
    suspend fun insertAll(points: List<GpsPoint>)

    @Query(
        "SELECT gp.* FROM gps_points gp " +
            "INNER JOIN laps l ON gp.lapId = l.id " +
            "WHERE l.raceId = :raceId ORDER BY gp.timestamp ASC"
    )
    suspend fun pointsForRace(raceId: Long): List<GpsPoint>

    @Query("SELECT COUNT(*) FROM gps_points WHERE lapId = :lapId")
    suspend fun countForLap(lapId: Long): Int
}