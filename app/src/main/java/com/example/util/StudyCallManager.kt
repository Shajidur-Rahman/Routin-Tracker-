package com.example.util

import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.media.RingtoneManager
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.util.Log
import com.example.receiver.StudyCallReceiver
import java.util.Calendar

/**
 * Study Call Frequency schedules strictly between 9:00 AM and 11:59 PM.
 * High: 10 calls/day
 * Middle: 5 calls/day
 * Low: 3 calls/day
 */
enum class StudyCallIntensity(
  val title: String,
  val callsPerDay: Int,
  val scheduleTimes: List<Pair<Int, Int>> // Hour (24h), Minute
) {
  LOW(
    title = "Low Study (3x/day)",
    callsPerDay = 3,
    scheduleTimes = listOf(
      10 to 0,   // 10:00 AM
      16 to 0,   // 04:00 PM
      21 to 30   // 09:30 PM
    )
  ),
  MIDDLE(
    title = "Middle Study (5x/day)",
    callsPerDay = 5,
    scheduleTimes = listOf(
      9 to 0,    // 09:00 AM
      12 to 0,   // 12:00 PM
      15 to 0,   // 03:00 PM
      18 to 0,   // 06:00 PM
      21 to 0    // 09:00 PM
    )
  ),
  HIGH(
    title = "High Study (10x/day)",
    callsPerDay = 10,
    scheduleTimes = listOf(
      9 to 0,    // 09:00 AM
      10 to 30,  // 10:30 AM
      12 to 0,   // 12:00 PM
      13 to 30,  // 01:30 PM
      15 to 0,   // 03:00 PM
      16 to 30,  // 04:30 PM
      18 to 0,   // 06:00 PM
      19 to 30,  // 07:30 PM
      21 to 0,   // 09:00 PM
      22 to 30   // 10:30 PM
    )
  )
}

object StudyCallManager {

  private const val PREFS_NAME = "study_call_prefs"
  private const val KEY_ENABLED = "study_call_enabled"
  private const val KEY_INTENSITY = "study_call_intensity"
  const val CALL_CHANNEL_ID = "study_call_incoming_channel"
  private const val BASE_REQUEST_CODE = 8000

  fun isEnabled(context: Context): Boolean {
    val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    return prefs.getBoolean(KEY_ENABLED, false)
  }

  fun setEnabled(context: Context, enabled: Boolean) {
    val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    prefs.edit().putBoolean(KEY_ENABLED, enabled).apply()
    if (enabled) {
      scheduleAllCalls(context)
    } else {
      cancelAllCalls(context)
    }
  }

  fun getIntensity(context: Context): StudyCallIntensity {
    val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    val name = prefs.getString(KEY_INTENSITY, StudyCallIntensity.MIDDLE.name) ?: StudyCallIntensity.MIDDLE.name
    return try {
      StudyCallIntensity.valueOf(name)
    } catch (e: Exception) {
      StudyCallIntensity.MIDDLE
    }
  }

  fun setIntensity(context: Context, intensity: StudyCallIntensity) {
    val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    prefs.edit().putString(KEY_INTENSITY, intensity.name).apply()
    if (isEnabled(context)) {
      cancelAllCalls(context)
      scheduleAllCalls(context)
    }
  }

  fun createNotificationChannel(context: Context) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
      val ringtoneUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE)
        ?: RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)

      val audioAttributes = AudioAttributes.Builder()
        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
        .setUsage(AudioAttributes.USAGE_NOTIFICATION_RINGTONE)
        .build()

      val channel = NotificationChannel(
        CALL_CHANNEL_ID,
        "Incoming Study Call",
        NotificationManager.IMPORTANCE_HIGH
      ).apply {
        description = "Wakes up device with incoming study reminder call"
        enableLights(true)
        enableVibration(true)
        vibrationPattern = longArrayOf(0, 1000, 800, 1000, 800, 1000)
        setSound(ringtoneUri, audioAttributes)
        lockscreenVisibility = android.app.Notification.VISIBILITY_PUBLIC
      }

      val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
      manager.createNotificationChannel(channel)
    }
  }

  fun scheduleAllCalls(context: Context) {
    createNotificationChannel(context)
    val intensity = getIntensity(context)
    val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    intensity.scheduleTimes.forEachIndexed { index, (hour, minute) ->
      val calendar = Calendar.getInstance().apply {
        set(Calendar.HOUR_OF_DAY, hour)
        set(Calendar.MINUTE, minute)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
        if (timeInMillis <= System.currentTimeMillis()) {
          add(Calendar.DAY_OF_YEAR, 1)
        }
      }

      val intent = Intent(context, StudyCallReceiver::class.java).apply {
        action = "com.example.ACTION_STUDY_CALL"
        putExtra("CALL_INDEX", index)
        putExtra("CALL_HOUR", hour)
        putExtra("CALL_MINUTE", minute)
      }

      val pendingIntent = PendingIntent.getBroadcast(
        context,
        BASE_REQUEST_CODE + index,
        intent,
        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
      )

      try {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
          alarmManager.setExactAndAllowWhileIdle(
            AlarmManager.RTC_WAKEUP,
            calendar.timeInMillis,
            pendingIntent
          )
        } else {
          alarmManager.setExact(
            AlarmManager.RTC_WAKEUP,
            calendar.timeInMillis,
            pendingIntent
          )
        }
      } catch (e: Exception) {
        Log.e("StudyCallManager", "Failed to schedule exact alarm for $hour:$minute", e)
      }
    }
  }

  fun cancelAllCalls(context: Context) {
    val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
    for (i in 0..15) {
      val intent = Intent(context, StudyCallReceiver::class.java)
      val pendingIntent = PendingIntent.getBroadcast(
        context,
        BASE_REQUEST_CODE + i,
        intent,
        PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
      )
      if (pendingIntent != null) {
        alarmManager.cancel(pendingIntent)
        pendingIntent.cancel()
      }
    }
  }

  /**
   * Test mode: Guarantees a strict 5-second delay before firing the study call.
   * Nothing rings or pops up immediately upon clicking.
   */
  fun triggerTestCall(context: Context, delaySeconds: Int = 5) {
    createNotificationChannel(context)
    val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    val intent = Intent(context, StudyCallReceiver::class.java).apply {
      action = "com.example.ACTION_STUDY_CALL"
      putExtra("IS_TEST", true)
    }

    val pendingIntent = PendingIntent.getBroadcast(
      context,
      9999,
      intent,
      PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
    )

    val delayMillis = delaySeconds * 1000L
    val triggerAtMillis = System.currentTimeMillis() + delayMillis

    // 1. Schedule via AlarmManager for deep sleep / lock screen wakeup
    try {
      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
        alarmManager.setExactAndAllowWhileIdle(
          AlarmManager.RTC_WAKEUP,
          triggerAtMillis,
          pendingIntent
        )
      } else {
        alarmManager.setExact(
          AlarmManager.RTC_WAKEUP,
          triggerAtMillis,
          pendingIntent
        )
      }
    } catch (e: Exception) {
      Log.w("StudyCallManager", "Exact alarm permission restricted, using handler fallback", e)
    }

    // 2. Also register a Handler timer for exact 5000ms delay to ensure it triggers after 5 seconds even without exact alarm permission
    Handler(Looper.getMainLooper()).postDelayed({
      try {
        context.sendBroadcast(intent)
      } catch (e: Exception) {
        e.printStackTrace()
      }
    }, delayMillis)
  }
}
