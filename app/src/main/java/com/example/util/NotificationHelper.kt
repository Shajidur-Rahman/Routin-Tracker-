package com.example.util

import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.MainActivity
import com.example.receiver.ReminderReceiver
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId

object NotificationHelper {

  const val CHANNEL_ID = "task_routine_reminders"
  const val CHANNEL_NAME = "Task & Routine Reminders"

  fun createNotificationChannel(context: Context) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
      val channel = NotificationChannel(
        CHANNEL_ID,
        CHANNEL_NAME,
        NotificationManager.IMPORTANCE_HIGH
      ).apply {
        description = "Alerts and reminders for scheduled routines and daily tasks"
        enableVibration(true)
      }
      val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager
      manager?.createNotificationChannel(channel)
    }
  }

  fun scheduleTaskReminder(
    context: Context,
    taskId: Long,
    taskTitle: String,
    dateIso: String,
    timeString: String // "HH:mm"
  ) {
    try {
      val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return
      val parts = timeString.split(":")
      if (parts.size != 2) return
      val hour = parts[0].toIntOrNull() ?: return
      val minute = parts[1].toIntOrNull() ?: return

      val taskDate = BengaliDateHelper.fromIsoString(dateIso)
      val targetDateTime = taskDate.atTime(LocalTime.of(hour, minute))
      val triggerMillis = targetDateTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()

      // If the reminder is in the past, don't schedule
      if (triggerMillis <= System.currentTimeMillis()) return

      val intent = Intent(context, ReminderReceiver::class.java).apply {
        putExtra(ReminderReceiver.EXTRA_TASK_ID, taskId)
        putExtra(ReminderReceiver.EXTRA_TASK_TITLE, taskTitle)
        putExtra(ReminderReceiver.EXTRA_TASK_DATE, dateIso)
      }

      val flags = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
      } else {
        PendingIntent.FLAG_UPDATE_CURRENT
      }

      val pendingIntent = PendingIntent.getBroadcast(
        context,
        taskId.toInt(),
        intent,
        flags
      )

      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
        alarmManager.setExactAndAllowWhileIdle(
          AlarmManager.RTC_WAKEUP,
          triggerMillis,
          pendingIntent
        )
      } else {
        alarmManager.set(
          AlarmManager.RTC_WAKEUP,
          triggerMillis,
          pendingIntent
        )
      }
    } catch (_: Exception) {
      // Handle security exception if exact alarm permission not allowed
    }
  }

  fun cancelTaskReminder(context: Context, taskId: Long) {
    try {
      val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return
      val intent = Intent(context, ReminderReceiver::class.java)
      val flags = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
      } else {
        PendingIntent.FLAG_UPDATE_CURRENT
      }
      val pendingIntent = PendingIntent.getBroadcast(
        context,
        taskId.toInt(),
        intent,
        flags
      )
      alarmManager.cancel(pendingIntent)
    } catch (_: Exception) {
    }
  }

  fun showNotification(context: Context, taskId: Long, taskTitle: String, dateIso: String) {
    val openIntent = Intent(context, MainActivity::class.java).apply {
      flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
      putExtra("SELECTED_DATE_ISO", dateIso)
    }
    val flags = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
      PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
    } else {
      PendingIntent.FLAG_UPDATE_CURRENT
    }
    val pendingIntent = PendingIntent.getActivity(context, taskId.toInt(), openIntent, flags)

    val notification = NotificationCompat.Builder(context, CHANNEL_ID)
      .setSmallIcon(android.R.drawable.ic_popup_reminder)
      .setContentTitle("Task Reminder")
      .setContentText(taskTitle)
      .setPriority(NotificationCompat.PRIORITY_HIGH)
      .setAutoCancel(true)
      .setContentIntent(pendingIntent)
      .build()

    try {
      NotificationManagerCompat.from(context).notify(taskId.toInt(), notification)
    } catch (_: SecurityException) {
      // Permission might not be granted
    }
  }
}
