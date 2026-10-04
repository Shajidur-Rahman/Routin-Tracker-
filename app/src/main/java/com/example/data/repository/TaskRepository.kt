package com.example.data.repository

import android.content.Context
import com.example.data.local.TaskDao
import com.example.data.model.DailyStat
import com.example.data.model.OverallStats
import com.example.data.model.RoutineProgress
import com.example.data.model.SheetDateInfo
import com.example.data.model.TaskEntity
import com.example.data.model.WeeklyStat
import com.example.data.remote.GoogleSheetService
import com.example.data.remote.SyncResult
import com.example.util.BengaliDateHelper
import com.example.util.NotificationHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.temporal.TemporalAdjusters

class TaskRepository(
  private val taskDao: TaskDao,
  private val sheetService: GoogleSheetService,
  private val context: Context
) {

  fun getTasksForDate(dateIso: String): Flow<List<TaskEntity>> {
    return taskDao.getTasksForDate(dateIso)
  }

  fun getAllTasks(): Flow<List<TaskEntity>> {
    return taskDao.getAllTasks()
  }

  suspend fun addTask(
    name: String,
    subject: String,
    startDate: LocalDate,
    daysToContinue: Int,
    reminderTime: String? = null
  ) = withContext(Dispatchers.IO) {
    val cleanName = name.trim()
    val cleanSubject = subject.trim().ifEmpty { "General" }
    val tasksToInsert = mutableListOf<TaskEntity>()

    if (daysToContinue <= 1) {
      tasksToInsert.add(
        TaskEntity(
          title = cleanName,
          subject = cleanSubject,
          dateIso = BengaliDateHelper.toIsoString(startDate),
          reminderTime = reminderTime
        )
      )
    } else {
      for (i in 1..daysToContinue) {
        val targetDate = startDate.plusDays((i - 1).toLong())
        tasksToInsert.add(
          TaskEntity(
            title = "$cleanName-$i",
            subject = cleanSubject,
            dateIso = BengaliDateHelper.toIsoString(targetDate),
            routineGroup = cleanName,
            routineDayIndex = i,
            routineTotalDays = daysToContinue,
            reminderTime = reminderTime
          )
        )
      }
    }

    val insertedIds = taskDao.insertTasks(tasksToInsert)
    // Schedule reminders if time provided
    if (!reminderTime.isNullOrBlank()) {
      tasksToInsert.forEachIndexed { index, task ->
        val id = insertedIds.getOrNull(index) ?: return@forEachIndexed
        NotificationHelper.scheduleTaskReminder(
          context,
          id,
          task.title,
          task.dateIso,
          reminderTime
        )
      }
    }
  }

  suspend fun toggleTaskCompletion(task: TaskEntity): SyncResult = withContext(Dispatchers.IO) {
    val newCompleted = !task.isCompleted
    val now = if (newCompleted) System.currentTimeMillis() else null

    // Immediately update local DB so the UI turns blue instantly
    taskDao.setTaskCompletion(
      id = task.id,
      isCompleted = newCompleted,
      completedAt = now,
      synced = true,
      syncMessage = if (newCompleted) "Done" else null
    )

    // Cancel reminder if completed
    if (newCompleted) {
      NotificationHelper.cancelTaskReminder(context, task.id)
    }

    // Send to Google Spreadsheet
    val syncResult = sheetService.sendTaskCompletionToSheet(
      taskId = task.id,
      taskTitle = task.title,
      dateIso = task.dateIso,
      isCompleted = newCompleted,
      timestamp = now ?: System.currentTimeMillis()
    )

    // Update sync status in DB
    taskDao.updateSyncStatus(
      id = task.id,
      synced = syncResult.isSuccess,
      syncMessage = syncResult.message
    )

    syncResult
  }

  suspend fun deleteTask(task: TaskEntity) = withContext(Dispatchers.IO) {
    NotificationHelper.cancelTaskReminder(context, task.id)
    taskDao.deleteTask(task)
  }

  suspend fun syncWithGoogleSheet(): Result<Int> = withContext(Dispatchers.IO) {
    try {
      val result = sheetService.fetchSpreadsheetDatesAndTasks()
      if (result.isFailure) {
        return@withContext Result.failure(result.exceptionOrNull() ?: Exception("Unknown error"))
      }

      val rows = result.getOrNull() ?: emptyList()
      var newTasksCount = 0

      // Read any pre-existing subjects from the sheet rows and seed if missing
      val existingTasks = taskDao.getAllTasks().first()
      val existingTitlesByDate = existingTasks.groupBy { it.dateIso }
        .mapValues { entry -> entry.value.map { it.title.lowercase() }.toSet() }

      for (row in rows) {
        val rowDate = row.date ?: continue
        val dateIso = BengaliDateHelper.toIsoString(rowDate)
        val existingOnDate = existingTitlesByDate[dateIso] ?: emptySet()

        for (subject in row.subjects) {
          if (subject.isNotBlank() && !existingOnDate.contains(subject.lowercase())) {
            taskDao.insertTask(
              TaskEntity(
                title = subject,
                subject = "Sheet Import",
                dateIso = dateIso
              )
            )
            newTasksCount++
          }
        }
      }

      Result.success(newTasksCount)
    } catch (e: Exception) {
      Result.failure(e)
    }
  }

  suspend fun calculateAnalysisStats(): OverallStats = withContext(Dispatchers.IO) {
    val allTasks = taskDao.getAllTasks().first()
    if (allTasks.isEmpty()) {
      return@withContext OverallStats()
    }

    val tasksByDate = allTasks.groupBy { it.dateIso }
    val today = LocalDate.now()

    // 1. Daily stats for the last 14 days + next 7 days or all active dates
    val sortedDates = tasksByDate.keys.map { BengaliDateHelper.fromIsoString(it) }.sorted()
    val minDate = sortedDates.firstOrNull() ?: today.minusDays(7)
    val maxDate = sortedDates.lastOrNull() ?: today.plusDays(7)

    val dailyStatsList = mutableListOf<DailyStat>()
    var cur = minDate
    while (!cur.isAfter(maxDate)) {
      val iso = BengaliDateHelper.toIsoString(cur)
      val tasksForDay = tasksByDate[iso] ?: emptyList()
      val total = tasksForDay.size
      val completed = tasksForDay.count { it.isCompleted }
      val rate = if (total > 0) completed.toFloat() / total else 0f

      dailyStatsList.add(
        DailyStat(
          date = cur,
          dateIso = iso,
          displayLabel = "${cur.dayOfMonth} ${BengaliDateHelper.getMonthShort(cur.month)}",
          dayOfWeek = BengaliDateHelper.getEnglishDayOfWeekShort(cur.dayOfWeek),
          completedCount = completed,
          totalCount = total,
          completionRate = rate
        )
      )
      cur = cur.plusDays(1)
    }

    // 2. Weekly stats (Grouped by Friday - Thursday weeks starting Oct 5)
    val weeklyTasksMap = mutableMapOf<Int, MutableList<TaskEntity>>()
    for (task in allTasks) {
      val d = BengaliDateHelper.fromIsoString(task.dateIso)
      val wNum = com.example.util.WeekColorHelper.getWeekNumber(d)
      weeklyTasksMap.getOrPut(wNum) { mutableListOf() }.add(task)
    }

    val weeklyStatsList = (1..13).map { wNum ->
      val tasks = weeklyTasksMap[wNum] ?: emptyList()
      val total = tasks.size
      val completed = tasks.count { it.isCompleted }
      val rate = if (total > 0) completed.toFloat() / total else 0f
      
      val (startD, endD) = when (wNum) {
        1 -> LocalDate.of(2026, 10, 5) to LocalDate.of(2026, 10, 8)
        13 -> LocalDate.of(2026, 12, 25) to LocalDate.of(2026, 12, 30)
        else -> {
          val fri = LocalDate.of(2026, 10, 9).plusWeeks((wNum - 2).toLong())
          val thu = fri.plusDays(6)
          fri to thu
        }
      }

      WeeklyStat(
        weekNumber = wNum,
        weekLabel = "Week $wNum (${startD.dayOfMonth} ${BengaliDateHelper.getMonthShort(startD.month)} - ${endD.dayOfMonth} ${BengaliDateHelper.getMonthShort(endD.month)})",
        startDate = startD,
        endDate = endD,
        completedCount = completed,
        totalCount = total,
        completionRate = rate
      )
    }

    // 3. Subject Strength & Weakness Analysis (What you do most and what you don't)
    val standardSubjects = listOf(
      "Physics 1st", "Physics 2nd", "Math 1st", "Math 2nd",
      "Chemistry 1st", "Chemistry 2nd", "Biology 1st", "Biology 2nd",
      "Bangla", "English", "ICT"
    )
    val presentSubjects = (standardSubjects + allTasks.map { it.subject }).distinct()
    val tasksBySubject = allTasks.groupBy { it.subject }

    val subjectAnalysisList = presentSubjects.mapNotNull { subj ->
      val tasks = tasksBySubject[subj] ?: emptyList()
      if (tasks.isEmpty() && !standardSubjects.contains(subj)) return@mapNotNull null
      val total = tasks.size
      val completed = tasks.count { it.isCompleted }
      val pending = total - completed
      val rate = if (total > 0) completed.toFloat() / total else 0f

      val isStrong = total > 0 && rate >= 0.70f
      val isWeak = (total > 0 && rate < 0.50f) || (total == 0)

      val feedback = when {
        total == 0 -> "Not studied yet (Needs Attention)"
        rate >= 0.85f -> "Excellent • Strongest Subject"
        rate >= 0.65f -> "Good • Consistent Performance"
        rate >= 0.40f -> "Moderate • Needs Regular Practice"
        else -> "Weakness • High Pending Tasks ($pending left)"
      }

      com.example.data.model.SubjectAnalysis(
        subject = subj,
        totalTasks = total,
        completedTasks = completed,
        pendingTasks = pending,
        completionRate = rate,
        isStrength = isStrong,
        isWeakness = isWeak,
        feedback = feedback
      )
    }.sortedWith(compareByDescending<com.example.data.model.SubjectAnalysis> { it.completedTasks }
      .thenByDescending { it.completionRate })

    val strongSubjects = subjectAnalysisList.filter { it.isStrength }
    val weakSubjects = subjectAnalysisList.filter { it.isWeakness }

    // 4. Routine groups progress
    val routinesByGroup = allTasks.filter { !it.routineGroup.isNullOrBlank() }
      .groupBy { it.routineGroup!! }

    val routineProgressList = routinesByGroup.map { (groupName, list) ->
      val total = list.firstOrNull()?.routineTotalDays ?: list.size
      val completed = list.count { it.isCompleted }
      val maxDayIndex = list.maxOfOrNull { it.routineDayIndex ?: 1 } ?: 1
      RoutineProgress(
        groupName = groupName,
        totalDays = total,
        completedDays = completed,
        currentDayIndex = maxDayIndex,
        completionRate = if (total > 0) completed.toFloat() / total else 0f
      )
    }

    // 4. Streaks
    var currentStreak = 0
    var bestStreak = 0
    var tempStreak = 0

    // Check consecutive days leading up to today
    var checkDate = today
    while (true) {
      val iso = BengaliDateHelper.toIsoString(checkDate)
      val tasks = tasksByDate[iso] ?: emptyList()
      if (tasks.isNotEmpty() && tasks.all { it.isCompleted }) {
        currentStreak++
        checkDate = checkDate.minusDays(1)
      } else if (checkDate == today && (tasks.isEmpty() || !tasks.all { it.isCompleted })) {
        // Check if yesterday had streak
        checkDate = checkDate.minusDays(1)
      } else {
        break
      }
    }

    for (stat in dailyStatsList) {
      if (stat.totalCount > 0 && stat.completionRate >= 1.0f) {
        tempStreak++
        if (tempStreak > bestStreak) bestStreak = tempStreak
      } else {
        tempStreak = 0
      }
    }

    val totalCount = allTasks.size
    val totalCompleted = allTasks.count { it.isCompleted }
    val overallRate = if (totalCount > 0) totalCompleted.toFloat() / totalCount else 0f

    // Best day of week
    val daySuccessMap = allTasks.groupBy { BengaliDateHelper.fromIsoString(it.dateIso).dayOfWeek }
      .mapValues { (_, list) ->
        val c = list.count { it.isCompleted }
        if (list.isNotEmpty()) c.toFloat() / list.size else 0f
      }
    val bestDay = daySuccessMap.maxByOrNull { it.value }?.key?.name?.lowercase()
      ?.replaceFirstChar { it.uppercase() } ?: "Monday"

    OverallStats(
      currentStreak = currentStreak,
      bestStreak = maxOf(bestStreak, currentStreak),
      totalTasks = totalCount,
      completedTasks = totalCompleted,
      overallRate = overallRate,
      bestDayOfWeek = bestDay,
      dailyStats = dailyStatsList,
      weeklyStats = weeklyStatsList,
      activeRoutines = routineProgressList,
      subjectRankings = subjectAnalysisList,
      strongSubjects = strongSubjects,
      weakSubjects = weakSubjects
    )
  }
}
