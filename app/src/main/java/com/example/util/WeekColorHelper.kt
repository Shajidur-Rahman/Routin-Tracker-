package com.example.util

import androidx.compose.ui.graphics.Color
import java.time.DayOfWeek
import java.time.LocalDate

data class WeekColorPalette(
  val weekNumber: Int,
  val name: String,
  val backgroundLight: Color,
  val borderLight: Color,
  val backgroundDark: Color,
  val borderDark: Color,
  val accentText: Color
)

object WeekColorHelper {

  val START_DATE: LocalDate = LocalDate.of(2026, 10, 5)
  val END_DATE: LocalDate = LocalDate.of(2026, 12, 30)

  // Soft, pleasant non-deep pastel color palettes for each week
  private val weekPalettes = listOf(
    // Week 1: Sky Blue (as requested: "like first week will have sky blue type color")
    WeekColorPalette(
      weekNumber = 1,
      name = "Sky Blue",
      backgroundLight = Color(0xFFF0F9FF),
      borderLight = Color(0xFFBAE6FD),
      backgroundDark = Color(0xFF0C243B),
      borderDark = Color(0xFF0369A1),
      accentText = Color(0xFF0284C7)
    ),
    // Week 2: Mint / Sage
    WeekColorPalette(
      weekNumber = 2,
      name = "Mint",
      backgroundLight = Color(0xFFF0FDF4),
      borderLight = Color(0xFFBBF7D0),
      backgroundDark = Color(0xFF063319),
      borderDark = Color(0xFF15803D),
      accentText = Color(0xFF16A34A)
    ),
    // Week 3: Lavender
    WeekColorPalette(
      weekNumber = 3,
      name = "Lavender",
      backgroundLight = Color(0xFFF5F3FF),
      borderLight = Color(0xFFDDD6FE),
      backgroundDark = Color(0xFF221645),
      borderDark = Color(0xFF6D28D9),
      accentText = Color(0xFF7C3AED)
    ),
    // Week 4: Soft Peach
    WeekColorPalette(
      weekNumber = 4,
      name = "Peach",
      backgroundLight = Color(0xFFFFF7ED),
      borderLight = Color(0xFFFED7AA),
      backgroundDark = Color(0xFF3B1E08),
      borderDark = Color(0xFFC2410C),
      accentText = Color(0xFFEA580C)
    ),
    // Week 5: Soft Rose
    WeekColorPalette(
      weekNumber = 5,
      name = "Rose",
      backgroundLight = Color(0xFFFFF1F2),
      borderLight = Color(0xFFFECDD3),
      backgroundDark = Color(0xFF3B1017),
      borderDark = Color(0xFFBE123C),
      accentText = Color(0xFFE11D48)
    ),
    // Week 6: Soft Butter / Primrose
    WeekColorPalette(
      weekNumber = 6,
      name = "Primrose",
      backgroundLight = Color(0xFFFEFCE8),
      borderLight = Color(0xFFFEF08A),
      backgroundDark = Color(0xFF332F06),
      borderDark = Color(0xFFA16207),
      accentText = Color(0xFFCA8A04)
    ),
    // Week 7: Soft Aqua / Teal
    WeekColorPalette(
      weekNumber = 7,
      name = "Teal",
      backgroundLight = Color(0xFFF0FDFA),
      borderLight = Color(0xFF99F6E4),
      backgroundDark = Color(0xFF042B28),
      borderDark = Color(0xFF0F766E),
      accentText = Color(0xFF0D9488)
    ),
    // Week 8: Periwinkle
    WeekColorPalette(
      weekNumber = 8,
      name = "Periwinkle",
      backgroundLight = Color(0xFFEEF2FF),
      borderLight = Color(0xFFC7D2FE),
      backgroundDark = Color(0xFF14193F),
      borderDark = Color(0xFF4338CA),
      accentText = Color(0xFF4F46E5)
    ),
    // Week 9: Coral
    WeekColorPalette(
      weekNumber = 9,
      name = "Coral",
      backgroundLight = Color(0xFFFFF5F5),
      borderLight = Color(0xFFFED7D7),
      backgroundDark = Color(0xFF3D1515),
      borderDark = Color(0xFFC53030),
      accentText = Color(0xFFE53E3E)
    ),
    // Week 10: Cyan
    WeekColorPalette(
      weekNumber = 10,
      name = "Cyan",
      backgroundLight = Color(0xFFECFEFF),
      borderLight = Color(0xFFA5F3FC),
      backgroundDark = Color(0xFF082B33),
      borderDark = Color(0xFF0E7490),
      accentText = Color(0xFF0891B2)
    ),
    // Week 11: Pistachio
    WeekColorPalette(
      weekNumber = 11,
      name = "Pistachio",
      backgroundLight = Color(0xFFF7FEE7),
      borderLight = Color(0xFFD9F99D),
      backgroundDark = Color(0xFF1E3106),
      borderDark = Color(0xFF4D7C0F),
      accentText = Color(0xFF65A30D)
    ),
    // Week 12: Violet
    WeekColorPalette(
      weekNumber = 12,
      name = "Violet",
      backgroundLight = Color(0xFFFAF5FF),
      borderLight = Color(0xFFE9D5FF),
      backgroundDark = Color(0xFF28103F),
      borderDark = Color(0xFF7E22CE),
      accentText = Color(0xFF9333EA)
    ),
    // Week 13: Honey
    WeekColorPalette(
      weekNumber = 13,
      name = "Honey",
      backgroundLight = Color(0xFFFFFBEB),
      borderLight = Color(0xFFFDE68A),
      backgroundDark = Color(0xFF382A07),
      borderDark = Color(0xFFB45309),
      accentText = Color(0xFFD97706)
    )
  )

  /**
   * Calculates week number starting from Oct 5, 2026.
   * Weeks start on Friday and end on Thursday:
   * Week 1: Oct 5 (Mon) - Oct 8 (Thu)
   * Week 2: Oct 9 (Fri) - Oct 15 (Thu)
   * Week 3: Oct 16 (Fri) - Oct 22 (Thu)
   * ... up to Dec 30 (Wed).
   */
  fun getWeekNumber(date: LocalDate): Int {
    if (date.isBefore(START_DATE)) return 1
    if (date.isAfter(END_DATE)) return weekPalettes.size

    val firstFriday = LocalDate.of(2026, 10, 9)
    if (date.isBefore(firstFriday)) {
      return 1
    }

    // Days from Oct 9
    val daysAfterFirstFriday = java.time.temporal.ChronoUnit.DAYS.between(firstFriday, date)
    val weekOffset = (daysAfterFirstFriday / 7).toInt()
    return minOf(2 + weekOffset, weekPalettes.size)
  }

  fun getPaletteForDate(date: LocalDate): WeekColorPalette {
    val weekNum = getWeekNumber(date)
    return weekPalettes[(weekNum - 1).coerceIn(0, weekPalettes.size - 1)]
  }

  fun getAllDatesInRange(): List<LocalDate> {
    val list = mutableListOf<LocalDate>()
    var cur = START_DATE
    while (!cur.isAfter(END_DATE)) {
      list.add(cur)
      cur = cur.plusDays(1)
    }
    return list
  }
}
