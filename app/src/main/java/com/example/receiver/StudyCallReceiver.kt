package com.example.receiver

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.media.RingtoneManager
import android.os.Build
import android.os.PowerManager
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.example.R
import com.example.service.StudyCallService
import com.example.ui.call.StudyCallActivity
import com.example.util.StudyCallManager

class StudyCallReceiver : BroadcastReceiver() {

  override fun onReceive(context: Context, intent: Intent) {
    // 1. Force screen on with wake lock
    val powerManager = context.getSystemService(Context.POWER_SERVICE) as? PowerManager
    @Suppress("DEPRECATION")
    val wakeLock = powerManager?.newWakeLock(
      PowerManager.SCREEN_BRIGHT_WAKE_LOCK or
          PowerManager.ACQUIRE_CAUSES_WAKEUP or
          PowerManager.ON_AFTER_RELEASE,
      "RoutineTrack:StudyCallWakeLock"
    )
    wakeLock?.acquire(30000L) // 30 seconds

    // 2. Start Foreground Service so Android allows calling screen popup from background
    try {
      val serviceIntent = Intent(context, StudyCallService::class.java).apply {
        action = StudyCallService.ACTION_START_CALL
      }
      ContextCompat.startForegroundService(context, serviceIntent)
    } catch (e: Exception) {
      e.printStackTrace()
    }

    // 3. Prepare full screen activity intent
    val callIntent = Intent(context, StudyCallActivity::class.java).apply {
      flags = Intent.FLAG_ACTIVITY_NEW_TASK or
          Intent.FLAG_ACTIVITY_CLEAR_TOP or
          Intent.FLAG_ACTIVITY_SINGLE_TOP or
          Intent.FLAG_ACTIVITY_REORDER_TO_FRONT
      putExtra("FROM_CALL_RECEIVER", true)
    }

    val fullScreenPendingIntent = PendingIntent.getActivity(
      context,
      8888,
      callIntent,
      PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
    )

    // 4. Post high-priority full-screen call notification
    StudyCallManager.createNotificationChannel(context)
    val ringtoneUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE)
      ?: RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)

    val notification = NotificationCompat.Builder(context, StudyCallManager.CALL_CHANNEL_ID)
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
      .build()

    val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    notificationManager.notify(StudyCallService.NOTIFICATION_ID, notification)

    // 5. Direct launch of activity
    try {
      context.startActivity(callIntent)
    } catch (e: Exception) {
      e.printStackTrace()
    }

    // 6. Reschedule if recurring
    val isTest = intent.getBooleanExtra("IS_TEST", false)
    if (!isTest && StudyCallManager.isEnabled(context)) {
      StudyCallManager.scheduleAllCalls(context)
    }
  }
}
