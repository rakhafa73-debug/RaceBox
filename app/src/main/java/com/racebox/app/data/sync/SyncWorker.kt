package com.racebox.app.data.sync

import android.content.Context
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.racebox.app.RaceBoxApp
import com.racebox.app.ui.notify.NotificationHelper
import java.util.concurrent.TimeUnit

class SyncWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val container = (applicationContext as RaceBoxApp).container
        return when (val result = container.syncRepository.syncNow()) {
            is SyncResult.Success -> {
                NotificationHelper.syncDone(applicationContext)
                Result.success()
            }
            SyncResult.NoData, SyncResult.Skipped -> Result.success()
            is SyncResult.Error -> Result.retry()
        }
    }

    companion object {
        private const val UNIQUE_NAME = "racebox_sync"

        fun schedule(context: Context) {
            val request = PeriodicWorkRequestBuilder<SyncWorker>(PERIOD_MINUTES, TimeUnit.MINUTES)
                .setConstraints(
                    Constraints.Builder()
                        .setRequiredNetworkType(NetworkType.CONNECTED)
                        .build()
                )
                .build()
            WorkManager.getInstance(context)
                .enqueueUniquePeriodicWork(UNIQUE_NAME, ExistingPeriodicWorkPolicy.KEEP, request)
        }

        private const val PERIOD_MINUTES = 15L
    }
}