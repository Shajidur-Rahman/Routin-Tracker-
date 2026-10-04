package com.example.data.model

import com.example.util.WeekColorHelper
import com.example.util.WeekColorPalette
import java.time.LocalDate

data class SheetDateInfo(
  val date: LocalDate,
  val dateIso: String,
  val bengaliDayNum: String,
  val englishDayNum: String,
  val bengaliDayName: String,
  val englishDayName: String,
  val monthShort: String,
  val formattedLabel: String,
  val isToday: Boolean,
  val totalTasks: Int = 0,
  val completedTasks: Int = 0,
  val weekNumber: Int = WeekColorHelper.getWeekNumber(date)
) {
  val weekPalette: WeekColorPalette
    get() = WeekColorHelper.getPaletteForDate(date)

  val weekLabel: String
    get() = "Week $weekNumber"

  val completionRate: Float
    get() = if (totalTasks > 0) completedTasks.toFloat() / totalTasks else 0f
}
