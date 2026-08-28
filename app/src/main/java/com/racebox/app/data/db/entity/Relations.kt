package com.racebox.app.data.db.entity

import androidx.room.Embedded
import androidx.room.Relation

data class LapListEntry(
    @Embedded val lap: Lap,
    @Relation(parentColumn = "id", entityColumn = "lapId")
    val points: List<GpsPoint>
)

data class RaceDetail(
    @Embedded val race: Race,
    @Relation(parentColumn = "id", entityColumn = "raceId")
    val laps: List<LapListEntry>
)