package com.racebox.app.data.db.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "laps",
    indices = [Index(value = ["raceId"])],
    foreignKeys = [
        ForeignKey(
            entity = Race::class,
            parentColumns = ["id"],
            childColumns = ["raceId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class Lap(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val raceId: Long,
    val lapNumber: Int,
    val startTime: Long,
    val endTime: Long,
    val distanceKm: Double = 0.0,
    val avgSpeedKmh: Double = 0.0,
    val maxSpeedKmh: Double = 0.0,
    val isSynced: Boolean = false
)