package com.racebox.app.ui.notify

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.racebox.app.MainActivity
import com.racebox.app.R

object NotificationHelper {

    const val CHANNEL_ID = "racebox_progress"
    private const val NOTIF_ID_TRACKING = 1
    private const val NOTIF_ID_SAVED = 2
    private const val NOTIF_ID_SYNC = 3

    fun ensureChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            val channel = NotificationChannel(
                CHANNEL_ID,
                context.getString(R.string.notification_channel_name),
                NotificationManager.IMPORTANCE_LOW
            )
            manager.createNotificationChannel(channel)
        }
    }

    fun trackingNotification(context: Context, text: String): Notification {
        ensureChannel(context)
        return NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(context.getString(R.string.app_name))
            .setContentText(text)
            .setContentIntent(contentIntent(context))
            .setOngoing(true)
            .build()
    }

    fun raceSaved(context: Context) {
        notify(
            context,
            NOTIF_ID_SAVED,
            context.getString(R.string.notification_race_saved_title),
            context.getString(R.string.notification_race_saved_text)
        )
    }

    fun syncDone(context: Context) {
        notify(
            context,
            NOTIF_ID_SYNC,
            context.getString(R.string.notification_sync_title),
            context.getString(R.string.notification_sync_text)
        )
    }

    private fun notify(context: Context, id: Int, title: String, text: String) {
        ensureChannel(context)
        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(title)
            .setContentText(text)
            .setContentIntent(contentIntent(context))
            .setAutoCancel(true)
            .build()
        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.notify(id, notification)
    }

    private fun contentIntent(context: Context): PendingIntent {
        val intent = Intent(context, MainActivity::class.java)
        return PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }
}