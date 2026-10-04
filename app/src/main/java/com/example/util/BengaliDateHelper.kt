package com.example.util

import java.time.DayOfWeek
import java.time.LocalDate
import java.time.Month
import java.time.format.DateTimeFormatter
import java.util.Locale

object BengaliDateHelper {

  private val bengaliDigits = charArrayOf('০', '১', '২', '৩', '৪', '৫', '৬', '৭', '৮', '৯')

  fun toBengaliNumber(number: Int): String {
    val s = number.toString()
    val sb = StringBuilder()
    for (ch in s) {
      if (ch in '0'..'9') {
        sb.append(bengaliDigits[ch - '0'])
      } else {
        sb.append(ch)
      }
    }
    return sb.toString()
  }

  fun fromBengaliNumber(bengaliStr: String): Int {
    val sb = StringBuilder()
    for (ch in bengaliStr.trim()) {
      val idx = bengaliDigits.indexOf(ch)
      if (idx >= 0) {
        sb.append(idx)
      } else if (ch in '0'..'9') {
        sb.append(ch)
      }
    }
    return sb.toString().toIntOrNull() ?: 1
  }

  fun getBengaliDayOfWeek(dayOfWeek: DayOfWeek): String {
    return when (dayOfWeek) {
      DayOfWeek.MONDAY -> "সোম"
      DayOfWeek.TUESDAY -> "মঙ্গল"
      DayOfWeek.WEDNESDAY -> "বুধ"
      DayOfWeek.THURSDAY -> "বৃহস্পতি"
      DayOfWeek.FRIDAY -> "শুক্র"
      DayOfWeek.SATURDAY -> "শনি"
      DayOfWeek.SUNDAY -> "রবি"
    }
  }

  fun getEnglishDayOfWeekShort(dayOfWeek: DayOfWeek): String {
    return when (dayOfWeek) {
      DayOfWeek.MONDAY -> "Mon"
      DayOfWeek.TUESDAY -> "Tue"
      DayOfWeek.WEDNESDAY -> "Wed"
      DayOfWeek.THURSDAY -> "Thu"
      DayOfWeek.FRIDAY -> "Fri"
      DayOfWeek.SATURDAY -> "Sat"
      DayOfWeek.SUNDAY -> "Sun"
    }
  }

  fun getMonthShort(month: Month): String {
    return when (month) {
      Month.JANUARY -> "Jan"
      Month.FEBRUARY -> "Feb"
      Month.MARCH -> "Mar"
      Month.APRIL -> "Apr"
      Month.MAY -> "May"
      Month.JUNE -> "Jun"
      Month.JULY -> "Jul"
      Month.AUGUST -> "Aug"
      Month.SEPTEMBER -> "Sep"
      Month.OCTOBER -> "Oct"
      Month.NOVEMBER -> "Nov"
      Month.DECEMBER -> "Dec"
    }
  }

  fun formatSpreadsheetDate(date: LocalDate): String {
    val bDay = toBengaliNumber(date.dayOfMonth)
    val bDayName = getBengaliDayOfWeek(date.dayOfWeek)
    val monthShort = getMonthShort(date.month)
    return "$bDay - $bDayName - $monthShort"
  }

  fun parseSpreadsheetDate(rawText: String, defaultYear: Int = 2026): LocalDate? {
    try {
      val trimmed = rawText.trim()
      if (trimmed.isEmpty()) return null
      val parts = trimmed.split("-").map { it.trim() }
      if (parts.size >= 3) {
        val dayPart = parts[0]
        val monthPart = parts[2].lowercase(Locale.ROOT)

        val day = fromBengaliNumber(dayPart)
        val month = when {
          monthPart.contains("oct") || monthPart.contains("অক্টো") -> Month.OCTOBER
          monthPart.contains("nov") || monthPart.contains("নভে") -> Month.NOVEMBER
          monthPart.contains("dec") || monthPart.contains("ডিসে") -> Month.DECEMBER
          monthPart.contains("jan") || monthPart.contains("জানু") -> Month.JANUARY
          monthPart.contains("feb") || monthPart.contains("ফেব্রু") -> Month.FEBRUARY
          monthPart.contains("mar") || monthPart.contains("মার্চ") -> Month.MARCH
          monthPart.contains("apr") || monthPart.contains("এপ্রিল") -> Month.APRIL
          monthPart.contains("may") || monthPart.contains("মে") -> Month.MAY
          monthPart.contains("jun") || monthPart.contains("জুন") -> Month.JUNE
          monthPart.contains("jul") || monthPart.contains("জুলাই") -> Month.JULY
          monthPart.contains("aug") || monthPart.contains("আগস্ট") -> Month.AUGUST
          monthPart.contains("sep") || monthPart.contains("সেপ্টে") -> Month.SEPTEMBER
          else -> Month.OCTOBER
        }
        return LocalDate.of(defaultYear, month, day.coerceIn(1, month.length(false)))
      }
    } catch (_: Exception) {
      // Fallback
    }
    return null
  }

  fun toIsoString(date: LocalDate): String {
    return date.format(DateTimeFormatter.ISO_LOCAL_DATE)
  }

  fun fromIsoString(iso: String): LocalDate {
    return try {
      LocalDate.parse(iso, DateTimeFormatter.ISO_LOCAL_DATE)
    } catch (_: Exception) {
      LocalDate.now()
    }
  }
}
