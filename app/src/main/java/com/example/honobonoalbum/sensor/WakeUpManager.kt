package com.example.honobonoalbum.sensor

import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.PowerManager
import android.os.VibrationEffect
import android.util.Log
import com.example.honobonoalbum.MainActivity
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class WakeUpManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val getSystemService: GetSystemService,
    private val notificationManager: WakeUpNotificationManager
) {
    private var wakeLock: PowerManager.WakeLock? = null

    private fun acquireWakeLock() {
        releaseWakeLock()

        wakeLock = getSystemService.powerManager.newWakeLock(
            PowerManager.PARTIAL_WAKE_LOCK,
            "EntranceDetector::WakeLock"
        ).apply {
            acquire(10000)
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


    fun forceWakeUpIgnoringLock() {
        try {
            acquireWakeLock()

            val intent = Intent(context, MainActivity::class.java).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
                addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY)
                addFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS)
                putExtra("WAKE_UP_SCREEN", true)
            }

            notificationManager.showWakeUpNotification()
            context.startActivity(intent)
        } catch (e: Exception) {
            Log.e("WakeUpManager", "Failed to force wake up device", e)
        }
    }
}