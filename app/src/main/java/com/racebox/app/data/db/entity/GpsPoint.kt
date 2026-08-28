package com.racebox.app.data.db.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "gps_points",
    indices = [Index(value = ["lapId"])],
    foreignKeys = [
        ForeignKey(
            entity = Lap::class,
            parentColumns = ["id"],
            childColumns = ["lapId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class GpsPoint(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val lapId: Long,
    val timestamp: Long,
    val latitudeEnc: String,
    val longitudeEnc: String,
    val speedKmhEnc: String
)