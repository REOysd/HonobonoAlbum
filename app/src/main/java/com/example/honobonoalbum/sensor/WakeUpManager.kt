package com.example.honobonoalbum.sensor

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.PowerManager
import android.os.VibrationEffect
import android.util.Log
import androidx.core.app.NotificationCompat
import com.example.honobonoalbum.MainActivity
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class WakeUpManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val getSystemService: GetSystemService
) {
    private var wakeLock: PowerManager.WakeLock? = null
    private val notificationChannelId = "WAKE_UP_CHANNEL"
    private val notificationId = 1001

    init {
        createNotificationChannel()
    }

    private fun acquireWakeLock() {
        releaseWakeLock()

        wakeLock = getSystemService.powerManager.newWakeLock(
            PowerManager.PARTIAL_WAKE_LOCK,
            "EntranceDetector::WakeLock"
        ).apply {
            acquire(10000) // 10秒間保持
        }
    }

    fun releaseWakeLock() {
        wakeLock?.let {
            if (it.isHeld) {
                it.release()
            }
        }
        wakeLock = null
    }

    private fun createNotificationChannel() {
        val name = "Wake Up Notifications"
        val descriptionText = "Notifications for waking up the device"
        val importance = NotificationManager.IMPORTANCE_HIGH
        val channel = NotificationChannel(notificationChannelId, name, importance).apply {
            description = descriptionText
            setBypassDnd(true)
            enableLights(true)
            enableVibration(true)
        }

        val notificationManager = getSystemService.notificationManager
        notificationManager.createNotificationChannel(channel)
    }

    private fun wakeUpScreen() {
        // Android 10以降では、フルスクリーンインテント通知を使用してバックグラウンド制限を回避
        val intent = Intent(context, MainActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
            addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            putExtra("WAKE_UP_SCREEN", true)
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notificationManager = getSystemService.notificationManager

        val notification = NotificationCompat.Builder(context, notificationChannelId)
            .setSmallIcon(android.R.drawable.ic_dialog_alert)
            .setContentTitle("Device Wake Up")
            .setContentText("Waking up device")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setAutoCancel(true)
            .setFullScreenIntent(pendingIntent, true) // これによりロック画面からでも起動可能
            .setContentIntent(pendingIntent)
            .build()

        notificationManager.notify(notificationId, notification)
    }

    private fun vibrateDevice() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val effect = VibrationEffect.createOneShot(
                200,
                VibrationEffect.DEFAULT_AMPLITUDE
            )
            getSystemService.vibrator.vibrate(effect)
        } else {
            @Suppress("DEPRECATION")
            getSystemService.vibrator.vibrate(100)
        }
    }

    fun clearWakeUpNotification() {
        val notificationManager = getSystemService.notificationManager
        notificationManager.cancel(notificationId)
    }

    fun forceWakeUpIgnoringLock(vibrate: Boolean = true): Boolean {
        return try {
            acquireWakeLock()

            // 直接Activityを起動（より積極的なアプローチ）
            val intent = Intent(context, MainActivity::class.java).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
                addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY)
                addFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS)
                putExtra("WAKE_UP_SCREEN", true)
                putExtra("FORCE_UNLOCK", true) // 強制解除フラグ
            }

            // 通知とActivity起動の両方を実行
            wakeUpScreen()
            context.startActivity(intent)

            if (vibrate) {
                vibrateDevice()
            }

            true
        } catch (e: Exception) {
            Log.e("WakeUpManager", "Failed to force wake up device", e)
            false
        }
    }
}