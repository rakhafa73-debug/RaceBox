package com.racebox.app.di

import android.content.Context
import com.racebox.app.R
import com.racebox.app.data.db.AppDatabase
import com.racebox.app.data.export.RaceExporter
import com.racebox.app.data.prefs.SecurePrefs
import com.racebox.app.data.security.CryptoUtils
import com.racebox.app.data.sync.SyncClient
import com.racebox.app.data.sync.SyncRepository
import com.racebox.app.domain.track.GpsTracker
import com.racebox.app.repository.AuthRepository
import com.racebox.app.repository.RaceRepository

class AppContainer(private val appContext: Context) {

    val gpsTracker by lazy { GpsTracker(appContext) }
    val crypto by lazy { CryptoUtils() }
    val securePrefs by lazy { SecurePrefs(appContext, crypto) }

    val database by lazy { AppDatabase.getInstance(appContext) }

    val authRepository by lazy { AuthRepository(database.userDao(), securePrefs) }
    val raceRepository by lazy { RaceRepository(database, crypto, appContext) }

    val syncClient by lazy {
        SyncClient(
            baseUrl = appContext.getString(R.string.backend_base_url),
            usernameProvider = { securePrefs.userSession()?.username }
        )
    }
    val syncRepository by lazy {
        SyncRepository(database, syncClient, crypto, securePrefs)
    }

    val raceExporter by lazy { RaceExporter(appContext, database, crypto) }
}