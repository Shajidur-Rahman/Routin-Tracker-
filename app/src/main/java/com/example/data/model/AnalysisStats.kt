package com.example.data.model

import java.time.LocalDate

data class DailyStat(
  val date: LocalDate,
  val dateIso: String,
  val displayLabel: String,
  val dayOfWeek: String,
  val completedCount: Int,
  val totalCount: Int,
  val completionRate: Float
)

data class WeeklyStat(
  val weekNumber: Int,
  val weekLabel: String,
  val startDate: LocalDate,
  val endDate: LocalDate,
  val completedCount: Int,
  val totalCount: Int,
  val completionRate: Float
)

data class RoutineProgress(
  val groupName: String,
  val totalDays: Int,
  val completedDays: Int,
  val currentDayIndex: Int,
  val completionRate: Float
)

data class SubjectAnalysis(
  val subject: String,
  val totalTasks: Int,
  val completedTasks: Int,
  val pendingTasks: Int,
  val completionRate: Float,
  val isStrength: Boolean,
  val isWeakness: Boolean,
  val feedback: String
)

data class OverallStats(
  val currentStreak: Int = 0,
  val bestStreak: Int = 0,
  val totalTasks: Int = 0,
  val completedTasks: Int = 0,
  val overallRate: Float = 0f,
  val bestDayOfWeek: String = "Monday",
  val dailyStats: List<DailyStat> = emptyList(),
  val weeklyStats: List<WeeklyStat> = emptyList(),
  val activeRoutines: List<RoutineProgress> = emptyList(),
  val subjectRankings: List<SubjectAnalysis> = emptyList(),
  val strongSubjects: List<SubjectAnalysis> = emptyList(),
  val weakSubjects: List<SubjectAnalysis> = emptyList()
)
