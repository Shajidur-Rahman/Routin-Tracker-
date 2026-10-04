package com.example

import android.app.Application
import com.example.data.local.AppDatabase
import com.example.data.remote.GoogleSheetService
import com.example.data.repository.TaskRepository
import com.example.util.NotificationHelper
import com.example.util.WeekColorHelper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.time.LocalDate

class RoutineApplication : Application() {

  val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

  val database by lazy { AppDatabase.getDatabase(this) }
  val sheetService by lazy { GoogleSheetService(this) }
  val repository by lazy { TaskRepository(database.taskDao(), sheetService, this) }

  override fun onCreate() {
    super.onCreate()
    NotificationHelper.createNotificationChannel(this)

    // Prepopulate initial tasks if database is brand new so the user sees a rich routine
    applicationScope.launch(Dispatchers.IO) {
      val existing = repository.getAllTasks().first()
      if (existing.isEmpty()) {
        val startDate = WeekColorHelper.START_DATE // 2026-10-05

        // Circle routine example
        repository.addTask(
          name = "Circle",
          subject = "Math 1st",
          startDate = startDate,
          daysToContinue = 4,
          reminderTime = "08:30"
        )
        // 11 Subjects routine examples
        repository.addTask(
          name = "Physics 1st",
          subject = "Physics 1st",
          startDate = startDate,
          daysToContinue = 3,
          reminderTime = "10:00"
        )
        repository.addTask(
          name = "Chemistry 1st",
          subject = "Chemistry 1st",
          startDate = startDate.plusDays(1),
          daysToContinue = 3,
          reminderTime = "14:00"
        )
        repository.addTask(
          name = "Bangla",
          subject = "Bangla",
          startDate = startDate,
          daysToContinue = 1,
          reminderTime = "19:00"
        )
        repository.addTask(
          name = "English",
          subject = "English",
          startDate = startDate.plusDays(1),
          daysToContinue = 1,
          reminderTime = "20:00"
        )

        // Background sync to fetch any existing sheet subjects
        repository.syncWithGoogleSheet()
      }
    }
  }
}
