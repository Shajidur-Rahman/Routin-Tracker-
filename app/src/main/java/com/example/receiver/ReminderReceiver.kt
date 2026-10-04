package com.example.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.example.util.NotificationHelper

class ReminderReceiver : BroadcastReceiver() {

  companion object {
    const val EXTRA_TASK_ID = "extra_task_id"
    const val EXTRA_TASK_TITLE = "extra_task_title"
    const val EXTRA_TASK_DATE = "extra_task_date"
  }

  override fun onReceive(context: Context, intent: Intent?) {
    if (intent == null) return
    val taskId = intent.getLongExtra(EXTRA_TASK_ID, -1L)
    val taskTitle = intent.getStringExtra(EXTRA_TASK_TITLE) ?: "Task Reminder"
    val taskDate = intent.getStringExtra(EXTRA_TASK_DATE) ?: ""

    if (taskId != -1L) {
      NotificationHelper.showNotification(context, taskId, taskTitle, taskDate)
    }
  }
}
