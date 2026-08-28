package com.racebox.app.data.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.racebox.app.data.db.entity.Race
import com.racebox.app.data.db.entity.RaceDetail
import kotlinx.coroutines.flow.Flow

@Dao
interface RaceDao {

    @Insert
    suspend fun insert(race: Race): Long

    @Update
    suspend fun update(race: Race)

    @Query("SELECT * FROM races WHERE id = :id LIMIT 1")
    suspend fun getById(id: Long): Race?

    @Query("SELECT * FROM races WHERE id = :id LIMIT 1")
    suspend fun getDetail(id: Long): RaceDetail?

    @Query("SELECT * FROM races WHERE userId = :userId ORDER BY startTime DESC")
    fun racesForUser(userId: Long): Flow<List<Race>>

    @Query("SELECT * FROM races WHERE userId = :userId ORDER BY startTime DESC")
    suspend fun racesForUserOnce(userId: Long): List<Race>

    @Query("SELECT * FROM races WHERE isSynced = 0 ORDER BY startTime ASC")
    suspend fun unsynced(): List<Race>

    @Query("UPDATE races SET isSynced = 1 WHERE id = :id")
    suspend fun markSynced(id: Long)
}