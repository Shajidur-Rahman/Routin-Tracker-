package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "tasks")
data class TaskEntity(
  @PrimaryKey(autoGenerate = true)
  val id: Long = 0,
  val title: String,
  val subject: String = "General",
  val dateIso: String,
  val isCompleted: Boolean = false,
  val completedAt: Long? = null,
  val routineGroup: String? = null,
  val routineDayIndex: Int? = null,
  val routineTotalDays: Int? = null,
  val reminderTime: String? = null, // "HH:mm" format e.g. "09:00"
  val syncedWithSheet: Boolean = false,
  val syncStatusMessage: String? = null,
  val createdAt: Long = System.currentTimeMillis()
)
