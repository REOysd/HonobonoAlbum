package com.example.honobonoalbum.sensor

import android.app.KeyguardManager
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import android.os.PowerManager
import android.os.VibratorManager
import android.provider.Settings
import android.view.WindowManager
import androidx.activity.ComponentActivity
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GetSystemService @Inject constructor(
    @ApplicationContext private val context: Context
) {
    val powerManager =
        context.getSystemService(Context.POWER_SERVICE) as PowerManager
    private val keyguardManager =
        context.getSystemService(Context.KEYGUARD_SERVICE) as KeyguardManager
    val vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        val vibratorManager =
            context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
        vibratorManager.defaultVibrator
    } else {
        @Suppress("DEPRECATION")
        context.getSystemService(Context.VIBRATOR_SERVICE) as android.os.Vibrator
    }
    val notificationManager =
        context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    fun turnOnScreen(activity: ComponentActivity) {
        activity.setShowWhenLocked(true)
        activity.setTurnScreenOn(true)

        activity.window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
    }

    fun turnOffScreen(activity: ComponentActivity) {
        activity.setShowWhenLocked(false)
        activity.setTurnScreenOn(false)
        activity.window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
    }
}