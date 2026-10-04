package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.RoutineApplication
import com.example.data.model.OverallStats
import com.example.data.model.SheetDateInfo
import com.example.data.model.TaskEntity
import com.example.data.remote.ParsedSheetRow
import com.example.util.BengaliDateHelper
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate

class MainViewModel(application: Application) : AndroidViewModel(application) {

  private val app = application as RoutineApplication
  private val repository = app.repository
  private val sheetService = app.sheetService

  private val _selectedDate = MutableStateFlow(LocalDate.now())
  val selectedDate: StateFlow<LocalDate> = _selectedDate.asStateFlow()

  private val _sheetDatesList = MutableStateFlow<List<LocalDate>>(emptyList())

  private val _isSyncing = MutableStateFlow(false)
  val isSyncing: StateFlow<Boolean> = _isSyncing.asStateFlow()

  private val _syncMessage = MutableStateFlow<String?>(null)
  val syncMessage: StateFlow<String?> = _syncMessage.asStateFlow()

  private val _analysisStats = MutableStateFlow(OverallStats())
  val analysisStats: StateFlow<OverallStats> = _analysisStats.asStateFlow()

  init {
    initSheetDates()
    refreshAnalysis()
  }

  private fun initSheetDates() {
    val fixedDates = com.example.util.WeekColorHelper.getAllDatesInRange()
    _sheetDatesList.value = fixedDates

    val today = LocalDate.now()
    if (today in com.example.util.WeekColorHelper.START_DATE..com.example.util.WeekColorHelper.END_DATE) {
      _selectedDate.value = today
    } else {
      _selectedDate.value = com.example.util.WeekColorHelper.START_DATE
    }

    // Also fetch the real sheet in the background to ensure any special dates are merged
    viewModelScope.launch {
      val res = sheetService.fetchSpreadsheetDatesAndTasks()
      if (res.isSuccess) {
        val parsedRows = res.getOrNull() ?: emptyList()
        val sheetDates = parsedRows.mapNotNull { it.date }
        if (sheetDates.isNotEmpty()) {
          val merged = (fixedDates + sheetDates).distinct().sorted()
          _sheetDatesList.value = merged
        }
      }
    }
  }

  val allTasks: StateFlow<List<TaskEntity>> = repository.getAllTasks()
    .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

  // Date cards with reactive task counts and completion rates
  val dateCards: StateFlow<List<SheetDateInfo>> = combine(_sheetDatesList, allTasks) { dates, tasks ->
    val today = LocalDate.now()
    val taskMap = tasks.groupBy { it.dateIso }

    dates.map { date ->
      val iso = BengaliDateHelper.toIsoString(date)
      val tasksOnDate = taskMap[iso] ?: emptyList()
      val total = tasksOnDate.size
      val completed = tasksOnDate.count { it.isCompleted }

      SheetDateInfo(
        date = date,
        dateIso = iso,
        bengaliDayNum = BengaliDateHelper.toBengaliNumber(date.dayOfMonth),
        englishDayNum = String.format("%02d", date.dayOfMonth),
        bengaliDayName = BengaliDateHelper.getBengaliDayOfWeek(date.dayOfWeek),
        englishDayName = BengaliDateHelper.getEnglishDayOfWeekShort(date.dayOfWeek),
        monthShort = BengaliDateHelper.getMonthShort(date.month),
        formattedLabel = BengaliDateHelper.formatSpreadsheetDate(date),
        isToday = date == today,
        totalTasks = total,
        completedTasks = completed
      )
    }
  }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

  @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
  val tasksForSelectedDate: StateFlow<List<TaskEntity>> = _selectedDate
    .flatMapLatest { date ->
      val iso = BengaliDateHelper.toIsoString(date)
      repository.getTasksForDate(iso)
    }
    .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

  fun selectDate(date: LocalDate) {
    _selectedDate.value = date
  }

  fun jumpToToday() {
    _selectedDate.value = LocalDate.now()
  }

  private val _isSheetWriteConfigured = MutableStateFlow(sheetService.isWriteEnabled)
  val isSheetWriteConfigured: StateFlow<Boolean> = _isSheetWriteConfigured.asStateFlow()

  fun toggleTaskCompletion(task: TaskEntity) {
    viewModelScope.launch {
      val result = repository.toggleTaskCompletion(task)
      if (result.isSuccess) {
        _syncMessage.value = "✓ " + result.message
      } else if (!sheetService.isWriteEnabled) {
        _syncMessage.value = "Turned Blue locally. To save inside the Google Sheet file, add Webhook or OAuth Token in Settings."
      } else {
        _syncMessage.value = result.message
      }
      refreshAnalysis()
    }
  }

  fun addTaskOrRoutine(
    name: String,
    subject: String,
    startDate: LocalDate,
    daysToContinue: Int,
    reminderTime: String?
  ) {
    viewModelScope.launch {
      repository.addTask(
        name = name,
        subject = subject,
        startDate = startDate,
        daysToContinue = daysToContinue,
        reminderTime = reminderTime
      )
      refreshAnalysis()
      _syncMessage.value = if (daysToContinue > 1) {
        "Added $name routine for $daysToContinue consecutive days!"
      } else {
        "Task added for ${BengaliDateHelper.formatSpreadsheetDate(startDate)}"
      }
    }
  }

  fun deleteTask(task: TaskEntity) {
    viewModelScope.launch {
      repository.deleteTask(task)
      refreshAnalysis()
      _syncMessage.value = "Task deleted"
    }
  }

  fun manualSyncWithSheet() {
    viewModelScope.launch {
      _isSyncing.value = true
      _syncMessage.value = "Connecting to Google Sheet..."
      val result = repository.syncWithGoogleSheet()
      _isSyncing.value = false
      if (result.isSuccess) {
        val imported = result.getOrNull() ?: 0
        _syncMessage.value = if (imported > 0) {
          "Successfully synced! Imported $imported new items."
        } else {
          "Google Sheet is up-to-date."
        }
      } else {
        _syncMessage.value = "Sync error: ${result.exceptionOrNull()?.localizedMessage ?: "Unknown"}"
      }
      refreshAnalysis()
    }
  }

  fun refreshAnalysis() {
    viewModelScope.launch {
      _analysisStats.value = repository.calculateAnalysisStats()
    }
  }

  fun clearSyncMessage() {
    _syncMessage.value = null
  }

  fun getSpreadsheetId(): String = sheetService.spreadsheetId
  fun setSpreadsheetId(id: String) {
    sheetService.spreadsheetId = id
  }

  fun getWebhookUrl(): String = sheetService.webhookUrl
  fun setWebhookUrl(url: String) {
    sheetService.webhookUrl = url
    _isSheetWriteConfigured.value = sheetService.isWriteEnabled
  }

  fun getOAuthToken(): String = sheetService.oauthAccessToken
  fun setOAuthToken(token: String) {
    sheetService.oauthAccessToken = token
    _isSheetWriteConfigured.value = sheetService.isWriteEnabled
  }

  fun testSheetWrite(onResult: (Boolean, String) -> Unit) {
    viewModelScope.launch {
      val res = sheetService.testSheetWrite()
      if (res.isSuccess) {
        val msg = res.getOrNull() ?: "Success"
        _syncMessage.value = "✓ Test row written to your Google Sheet file!"
        onResult(true, msg)
      } else {
        val err = res.exceptionOrNull()?.localizedMessage ?: "Failed"
        _syncMessage.value = "Sheet write test error: $err"
        onResult(false, err)
      }
    }
  }
}
