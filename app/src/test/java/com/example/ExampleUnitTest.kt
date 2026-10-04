package com.example

import com.example.util.BengaliDateHelper
import com.example.util.WeekColorHelper
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test
import java.time.LocalDate
import java.time.Month

class ExampleUnitTest {
  @Test
  fun addition_isCorrect() {
    assertEquals(4, 2 + 2)
  }

  @Test
  fun testBengaliNumerals() {
    assertEquals("৫", BengaliDateHelper.toBengaliNumber(5))
    assertEquals("১২", BengaliDateHelper.toBengaliNumber(12))
    assertEquals(5, BengaliDateHelper.fromBengaliNumber("৫"))
    assertEquals(12, BengaliDateHelper.fromBengaliNumber("১২"))
  }

  @Test
  fun testParseSpreadsheetDate() {
    val date = BengaliDateHelper.parseSpreadsheetDate("৫ - সোম - Oct", 2026)
    assertNotNull(date)
    assertEquals(5, date?.dayOfMonth)
    assertEquals(Month.OCTOBER, date?.month)
  }

  @Test
  fun testFormatSpreadsheetDate() {
    val date = LocalDate.of(2026, Month.OCTOBER, 5)
    val label = BengaliDateHelper.formatSpreadsheetDate(date)
    assertEquals("৫ - সোম - Oct", label)
  }

  @Test
  fun testFridayToThursdayWeeks() {
    // Week 1: Oct 5 (Mon) to Oct 8 (Thu)
    assertEquals(1, WeekColorHelper.getWeekNumber(LocalDate.of(2026, 10, 5)))
    assertEquals(1, WeekColorHelper.getWeekNumber(LocalDate.of(2026, 10, 8)))

    // Week 2: Oct 9 (Fri) to Oct 15 (Thu)
    assertEquals(2, WeekColorHelper.getWeekNumber(LocalDate.of(2026, 10, 9)))
    assertEquals(2, WeekColorHelper.getWeekNumber(LocalDate.of(2026, 10, 15)))

    // Week 3: Oct 16 (Fri)
    assertEquals(3, WeekColorHelper.getWeekNumber(LocalDate.of(2026, 10, 16)))

    // Week 13: Dec 25 (Fri) to Dec 30 (Wed)
    assertEquals(13, WeekColorHelper.getWeekNumber(LocalDate.of(2026, 12, 30)))
  }

  @Test
  fun testTotalDatesInRange() {
    val allDates = WeekColorHelper.getAllDatesInRange()
    assertEquals(87, allDates.size)
    assertEquals(LocalDate.of(2026, 10, 5), allDates.first())
    assertEquals(LocalDate.of(2026, 12, 30), allDates.last())
  }
}
