package com.racebox.app.data.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.racebox.app.data.db.entity.Lap

@Dao
interface LapDao {

    @Insert
    suspend fun insert(lap: Lap): Long

    @Update
    suspend fun update(lap: Lap)

    @Query("SELECT * FROM laps WHERE id = :id LIMIT 1")
    suspend fun lapById(id: Long): Lap?

    @Query("SELECT * FROM laps WHERE raceId = :raceId ORDER BY lapNumber ASC")
    suspend fun lapsForRace(raceId: Long): List<Lap>

    @Query("SELECT COALESCE(MAX(lapNumber), 0) + 1 FROM laps WHERE raceId = :raceId")
    suspend fun nextLapNumber(raceId: Long): Int

    @Query("UPDATE laps SET isSynced = 1 WHERE id = :id")
    suspend fun markSynced(id: Long)

    @Query("UPDATE laps SET isSynced = 1 WHERE raceId = :raceId")
    suspend fun markSyncedForRace(raceId: Long)
}