package com.example.service

import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.media.AudioAttributes
import android.media.RingtoneManager
import android.os.Build
import android.os.IBinder
import android.os.PowerManager
import androidx.core.app.NotificationCompat
import com.example.R
import com.example.ui.call.StudyCallActivity
import com.example.util.StudyCallManager

class StudyCallService : Service() {

  private var wakeLock: PowerManager.WakeLock? = null

  companion object {
    const val ACTION_START_CALL = "com.example.service.ACTION_START_CALL"
    const val ACTION_STOP_CALL = "com.example.service.ACTION_STOP_CALL"
    const val NOTIFICATION_ID = 7777
  }

  override fun onBind(intent: Intent?): IBinder? = null

  override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
    if (intent?.action == ACTION_STOP_CALL) {
      stopCall()
      return START_NOT_STICKY
    }

    // 1. Force screen on with high priority WakeLock
    try {
      val powerManager = getSystemService(Context.POWER_SERVICE) as? PowerManager
      wakeLock?.release()
      @Suppress("DEPRECATION")
      wakeLock = powerManager?.newWakeLock(
        PowerManager.SCREEN_BRIGHT_WAKE_LOCK or
            PowerManager.ACQUIRE_CAUSES_WAKEUP or
            PowerManager.ON_AFTER_RELEASE,
        "RoutineTrack:StudyCallScreenWake"
      )
      wakeLock?.acquire(30000L) // 30 seconds
    } catch (e: Exception) {
      e.printStackTrace()
    }

    // 2. Prepare full screen activity intent
    val callIntent = Intent(this, StudyCallActivity::class.java).apply {
      this.flags = Intent.FLAG_ACTIVITY_NEW_TASK or
          Intent.FLAG_ACTIVITY_CLEAR_TOP or
          Intent.FLAG_ACTIVITY_SINGLE_TOP or
          Intent.FLAG_ACTIVITY_REORDER_TO_FRONT
      putExtra("FROM_FOREGROUND_SERVICE", true)
    }

    val fullScreenPendingIntent = PendingIntent.getActivity(
      this,
      8888,
      callIntent,
      PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
    )

    // 3. Build incoming call foreground notification
    val ringtoneUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE)
      ?: RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)

    StudyCallManager.createNotificationChannel(this)

    val notification = NotificationCompat.Builder(this, StudyCallManager.CALL_CHANNEL_ID)
      .setSmallIcon(R.mipmap.ic_launcher)
      .setContentTitle("📞 Incoming Study Call")
      .setContentText("Study Supervisor is calling • Tap to review pending works")
      .setPriority(NotificationCompat.PRIORITY_MAX)
      .setCategory(NotificationCompat.CATEGORY_CALL)
      .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
      .setAutoCancel(true)
      .setSound(ringtoneUri)
      .setVibrate(longArrayOf(0, 1000, 800, 1000, 800, 1000))
      .setFullScreenIntent(fullScreenPendingIntent, true)
      .setContentIntent(fullScreenPendingIntent)
      .setOngoing(true)
      .build()

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
      startForeground(
        NOTIFICATION_ID,
        notification,
        ServiceInfo.FOREGROUND_SERVICE_TYPE_PHONE_CALL
      )
    } else {
      startForeground(NOTIFICATION_ID, notification)
    }

    // 4. Force activity launch immediately
    try {
      startActivity(callIntent)
    } catch (e: Exception) {
      e.printStackTrace()
    }

    return START_NOT_STICKY
  }

  private fun stopCall() {
    try {
      wakeLock?.let {
        if (it.isHeld) it.release()
      }
    } catch (e: Exception) {
      e.printStackTrace()
    }
    stopForeground(STOP_FOREGROUND_REMOVE)
    stopSelf()
  }

  override fun onDestroy() {
    super.onDestroy()
    try {
      wakeLock?.let {
        if (it.isHeld) it.release()
      }
    } catch (e: Exception) {
      e.printStackTrace()
    }
  }
}
