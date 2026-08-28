package com.racebox.app

import android.app.Application
import com.racebox.app.di.AppContainer
import com.racebox.app.ui.notify.NotificationHelper

class RaceBoxApp : Application() {

    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        container = AppContainer(this)
        NotificationHelper.ensureChannel(this)
    }
}