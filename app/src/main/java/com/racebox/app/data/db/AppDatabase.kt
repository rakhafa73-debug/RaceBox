package com.racebox.app.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.racebox.app.data.db.dao.GpsPointDao
import com.racebox.app.data.db.dao.LapDao
import com.racebox.app.data.db.dao.RaceDao
import com.racebox.app.data.db.dao.UserDao
import com.racebox.app.data.db.entity.GpsPoint
import com.racebox.app.data.db.entity.Lap
import com.racebox.app.data.db.entity.Race
import com.racebox.app.data.db.entity.User

@Database(
    entities = [User::class, Race::class, Lap::class, GpsPoint::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun userDao(): UserDao
    abstract fun raceDao(): RaceDao
    abstract fun lapDao(): LapDao
    abstract fun gpsPointDao(): GpsPointDao

    companion object {
        @Volatile
        private var instance: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase =
            instance ?: synchronized(this) {
                instance ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "racebox.db"
                ).build().also { instance = it }
            }
    }
}