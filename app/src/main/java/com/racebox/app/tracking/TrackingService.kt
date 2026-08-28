package com.racebox.app.tracking

import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.IBinder
import androidx.core.app.ServiceCompat
import com.racebox.app.R
import com.racebox.app.RaceBoxApp
import com.racebox.app.domain.race.GpsSample
import com.racebox.app.domain.track.GpsTracker
import com.racebox.app.repository.RaceRepository
import com.racebox.app.ui.notify.NotificationHelper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch

class TrackingService : Service() {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private lateinit var gpsTracker: GpsTracker
    private lateinit var raceRepository: RaceRepository
    private var collectJob: Job? = null

    override fun onCreate() {
        super.onCreate()
        val container = (application as RaceBoxApp).container
        gpsTracker = container.gpsTracker
        raceRepository = container.raceRepository
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_START -> {
                ServiceCompat.startForeground(
                    this,
                    NOTIFICATION_ID,
                    NotificationHelper.trackingNotification(this, getString(R.string.notification_tracking_text)),
                    ServiceInfo.FOREGROUND_SERVICE_TYPE_LOCATION
                )
                startCollecting()
            }
            else -> stopSelf()
        }
        return START_STICKY
    }

    private fun startCollecting() {
        if (collectJob?.isActive == true) return
        collectJob = scope.launch {
            gpsTracker.start()
            gpsTracker.locations.collect { location ->
                raceRepository.recordSample(
                    GpsSample(
                        timestamp = location.time,
                        latitude = location.latitude,
                        longitude = location.longitude,
                        speedKmh = location.speed * 3.6
                    )
                )
            }
        }
    }

    override fun onDestroy() {
        collectJob?.cancel()
        gpsTracker.stop()
        scope.cancel()
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    companion object {
        const val ACTION_START = "com.racebox.app.action.START_TRACKING"
        private const val NOTIFICATION_ID = 1001

        fun start(context: Context) {
            val intent = Intent(context, TrackingService::class.java).setAction(ACTION_START)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        }

        fun stop(context: Context) {
            context.stopService(Intent(context, TrackingService::class.java))
        }
    }
}