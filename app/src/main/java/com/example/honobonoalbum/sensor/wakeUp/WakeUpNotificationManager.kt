package com.example.honobonoalbum.sensor.wakeUp

import android.app.NotificationChannel
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import com.example.honobonoalbum.MainActivity
import com.example.honobonoalbum.sensor.GetSystemService
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class WakeUpNotificationManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val getSystemService: GetSystemService
) {
    private val notificationChannelId = "WAKE_UP_CHANNEL"
    private val notificationId = 1001

    init {
        createNotificationChannel()
    }

    private fun createNotificationChannel() {
        val name = "Wake Up Notifications"
        val descriptionText = "Notifications for waking up the device"
        val importance = android.app.NotificationManager.IMPORTANCE_HIGH
        val channel = NotificationChannel(notificationChannelId, name, importance).apply {
            description = descriptionText
            setBypassDnd(true)
            enableLights(true)
            enableVibration(true)
            setShowBadge(false)
            lockscreenVisibility = android.app.Notification.VISIBILITY_PUBLIC
        }

        getSystemService.notificationManager.createNotificationChannel(channel)
    }

    fun showWakeUpNotification() {
        val intent = Intent(context, MainActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
            addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY)
            addFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS)
            putExtra("WAKE_UP_SCREEN", true)
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or
                    PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, notificationChannelId)
            .setSmallIcon(android.R.drawable.ic_dialog_alert)
            .setContentTitle("Device Wake Up")
            .setContentText("Waking up device")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setAutoCancel(true)
            .setFullScreenIntent(pendingIntent, true)
            .setContentIntent(pendingIntent)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setOngoing(false)
            .build()

        getSystemService.notificationManager.notify(notificationId, notification)
    }

    fun clearWakeUpNotification() {
        getSystemService.notificationManager.cancel(notificationId)
    }
}