package com.example.honobonoalbum

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.material3.Text
import com.example.honobonoalbum.sensor.GetSystemService
import com.example.honobonoalbum.sensor.WakeUpManager
import com.example.honobonoalbum.sensor.WakeUpNotificationManager
import com.example.honobonoalbum.ui.theme.HonobonoAlbumTheme
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    @Inject lateinit var wakeUpManager: WakeUpManager
    @Inject lateinit var wakeUpNotificationManager: WakeUpNotificationManager
    @Inject lateinit var getSystemService: GetSystemService

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (intent.getBooleanExtra("WAKE_UP_SCREEN", false)) {
            getSystemService.turnOnScreen(this)
            wakeUpNotificationManager.clearWakeUpNotification()
            intent.removeExtra("WAKE_UP_SCREEN")
        }

        testWakeUpDevice()

        enableEdgeToEdge()
        setContent {
            HonobonoAlbumTheme {
                Text("fsdfsdfsdf")
            }
        }
    }

    override fun onResume() {
        super.onResume()
        wakeUpManager.releaseWakeLock()
    }

    override fun onDestroy() {
        super.onDestroy()
        getSystemService.turnOffScreen(this)
    }

    // TODO: テスト用のメソッド
    private fun testWakeUpDevice() {
        Handler(Looper.getMainLooper()).postDelayed({
            wakeUpManager.forceWakeUpIgnoringLock()
        }, 10000)
    }
}