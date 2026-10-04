package com.example.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material.icons.filled.ThumbDown
import androidx.compose.material.icons.filled.ThumbUp
import androidx.compose.material.icons.filled.TrendingDown
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.DailyStat
import com.example.data.model.OverallStats
import com.example.data.model.RoutineProgress
import com.example.data.model.SubjectAnalysis
import com.example.data.model.WeeklyStat
import com.example.ui.theme.BluePrimary
import com.example.ui.theme.Slate400
import com.example.ui.theme.Slate500
import com.example.ui.viewmodel.MainViewModel
import com.example.util.WeekColorHelper

@Composable
fun AnalysisScreen(
  viewModel: MainViewModel,
  modifier: Modifier = Modifier
) {
  val stats by viewModel.analysisStats.collectAsState()
  var selectedTab by remember { mutableIntStateOf(0) }
  val tabs = listOf("Subjects & Weakness", "Day to Day", "Week to Week", "Routines")

  LazyColumn(
    modifier = modifier
      .fillMaxSize()
      .background(MaterialTheme.colorScheme.background)
      .padding(horizontal = 16.dp),
    contentPadding = PaddingValues(top = 16.dp, bottom = 80.dp),
    verticalArrangement = Arrangement.spacedBy(14.dp)
  ) {
    // Header
    item {
      Column {
        Text(
          text = "Progress & Weakness Analysis",
          style = MaterialTheme.typography.headlineMedium.copy(
            fontWeight = FontWeight.ExtraBold,
            letterSpacing = (-0.5).sp
          ),
          color = MaterialTheme.colorScheme.onBackground
        )
        Text(
          text = "Analyze strengths, weaknesses, and routine performance",
          style = MaterialTheme.typography.bodyMedium,
          color = Slate500
        )
      }
    }

    // Top Overview Metric Cards
    item {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
      ) {
        MetricTile(
          icon = Icons.Default.LocalFireDepartment,
          iconTint = Color(0xFFF97316),
          title = "Active Streak",
          value = "${stats.currentStreak} Days",
          subtitle = "Best: ${stats.bestStreak}d",
          modifier = Modifier.weight(1f)
        )
        MetricTile(
          icon = Icons.Default.TrendingUp,
          iconTint = BluePrimary,
          title = "Total Done",
          value = "${stats.completedTasks}/${stats.totalTasks}",
          subtitle = "${(stats.overallRate * 100).toInt()}% Rate",
          modifier = Modifier.weight(1f)
        )
      }
    }

    // Navigation Tabs
    item {
      Surface(
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surfaceVariant,
        modifier = Modifier.fillMaxWidth()
      ) {
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .padding(3.dp),
          horizontalArrangement = Arrangement.SpaceBetween
        ) {
          tabs.forEachIndexed { index, title ->
            val isSelected = selectedTab == index
            Box(
              modifier = Modifier
                .weight(1f)
                .clip(RoundedCornerShape(9.dp))
                .background(if (isSelected) MaterialTheme.colorScheme.surface else Color.Transparent)
                .clickable { selectedTab = index }
                .padding(vertical = 8.dp)
                .testTag("tab_analysis_$index"),
              contentAlignment = Alignment.Center
            ) {
              Text(
                text = title,
                style = MaterialTheme.typography.labelSmall.copy(
                  fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                  fontSize = 11.sp
                ),
                color = if (isSelected) BluePrimary else MaterialTheme.colorScheme.onSurfaceVariant
              )
            }
          }
        }
      }
    }

    // Tab content
    when (selectedTab) {
      0 -> {
        // SUBJECTS & WEAKNESS TAB
        item {
          // Top highlight cards: Strongest vs Weakness
          val mostDone = stats.strongSubjects.firstOrNull() ?: stats.subjectRankings.firstOrNull()
          val leastDone = stats.weakSubjects.lastOrNull() ?: stats.subjectRankings.lastOrNull()

          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
          ) {
            Surface(
              modifier = Modifier.weight(1f),
              shape = RoundedCornerShape(14.dp),
              color = Color(0xFFF0FDF4),
              border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFBBF7D0))
            ) {
              Column(modifier = Modifier.padding(12.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                  Icon(Icons.Default.ThumbUp, contentDescription = null, tint = Color(0xFF16A34A), modifier = Modifier.size(16.dp))
                  Spacer(modifier = Modifier.width(6.dp))
                  Text("STUDIED MOST", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold), color = Color(0xFF16A34A))
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                  text = mostDone?.subject ?: "None",
                  style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                  color = Color(0xFF14532D)
                )
                Text(
                  text = "${mostDone?.completedTasks ?: 0} tasks done (${((mostDone?.completionRate ?: 0f) * 100).toInt()}%)",
                  style = MaterialTheme.typography.labelSmall,
                  color = Color(0xFF15803D)
                )
              }
            }

            Surface(
              modifier = Modifier.weight(1f),
              shape = RoundedCornerShape(14.dp),
              color = Color(0xFFFFF1F2),
              border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFECDD3))
            ) {
              Column(modifier = Modifier.padding(12.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                  Icon(Icons.Default.Warning, contentDescription = null, tint = Color(0xFFE11D48), modifier = Modifier.size(16.dp))
                  Spacer(modifier = Modifier.width(6.dp))
                  Text("WEAKNESS", style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold), color = Color(0xFFE11D48))
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                  text = leastDone?.subject ?: "None",
                  style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                  color = Color(0xFF881337)
                )
                Text(
                  text = "${leastDone?.pendingTasks ?: 0} tasks pending (${((leastDone?.completionRate ?: 0f) * 100).toInt()}%)",
                  style = MaterialTheme.typography.labelSmall,
                  color = Color(0xFFBE123C)
                )
              }
            }
          }
        }

        item {
          Text(
            text = "Subject Breakdown (11 Academic Subjects)",
            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.onBackground
          )
        }

        items(stats.subjectRankings) { subjectStat ->
          SubjectStatRow(stat = subjectStat)
        }
      }

      1 -> {
        // Day to Day Progress
        item {
          DailyChartCard(dailyStats = stats.dailyStats)
        }

        item {
          Text(
            text = "Daily Breakdown",
            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.onBackground
          )
        }

        items(stats.dailyStats.filter { it.totalCount > 0 }) { day ->
          DailyStatRow(day = day)
        }
      }

      2 -> {
        // Week to Week Progress (Friday to Thursday weeks)
        item {
          WeeklySummaryCard(weeklyStats = stats.weeklyStats, bestDay = stats.bestDayOfWeek)
        }

        item {
          Text(
            text = "13 Weeks Overview (Fri - Thu)",
            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.onBackground
          )
        }

        items(stats.weeklyStats) { week ->
          WeeklyStatRow(week = week)
        }
      }

      3 -> {
        // Routine Progress (e.g. Circle-1..Circle-4)
        item {
          Text(
            text = "Active Multi-Day Routines",
            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.onBackground
          )
        }

        if (stats.activeRoutines.isEmpty()) {
          item {
            Surface(
              modifier = Modifier.fillMaxWidth(),
              shape = RoundedCornerShape(16.dp),
              color = MaterialTheme.colorScheme.surface
            ) {
              Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
              ) {
                Text(
                  text = "No active routines yet",
                  style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                  text = "Add a routine on the home screen (e.g. Physics 1st for 4 days) to track multi-day progress.",
                  style = MaterialTheme.typography.bodySmall,
                  color = Slate500,
                  textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
              }
            }
          }
        } else {
          items(stats.activeRoutines) { routine ->
            RoutineProgressCard(routine = routine)
          }
        }
      }
    }
  }
}

@Composable
fun SubjectStatRow(stat: SubjectAnalysis) {
  val isWeak = stat.isWeakness
  val isStrong = stat.isStrength

  Surface(
    modifier = Modifier.fillMaxWidth(),
    shape = RoundedCornerShape(14.dp),
    color = MaterialTheme.colorScheme.surface,
    border = androidx.compose.foundation.BorderStroke(
      1.dp,
      if (isWeak) Color(0xFFFECDD3) else if (isStrong) Color(0xFFBBF7D0) else MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)
    )
  ) {
    Column(modifier = Modifier.padding(14.dp)) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Column(modifier = Modifier.weight(1f)) {
          Text(
            text = stat.subject,
            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.onSurface
          )
          Text(
            text = stat.feedback,
            style = MaterialTheme.typography.labelSmall.copy(
              color = if (isWeak) Color(0xFFE11D48) else if (isStrong) Color(0xFF16A34A) else Slate500,
              fontWeight = if (isWeak || isStrong) FontWeight.SemiBold else FontWeight.Normal
            )
          )
        }

        Column(horizontalAlignment = Alignment.End) {
          Text(
            text = "${(stat.completionRate * 100).toInt()}%",
            style = MaterialTheme.typography.titleMedium.copy(
              fontWeight = FontWeight.ExtraBold,
              color = if (isStrong) Color(0xFF16A34A) else if (isWeak) Color(0xFFE11D48) else BluePrimary
            )
          )
          Text(
            text = "${stat.completedTasks}/${stat.totalTasks} Done",
            style = MaterialTheme.typography.labelSmall,
            color = Slate400
          )
        }
      }

      Spacer(modifier = Modifier.height(8.dp))

      LinearProgressIndicator(
        progress = { stat.completionRate },
        modifier = Modifier
          .fillMaxWidth()
          .height(6.dp)
          .clip(RoundedCornerShape(3.dp)),
        color = if (isStrong) Color(0xFF16A34A) else if (isWeak) Color(0xFFE11D48) else BluePrimary,
        trackColor = MaterialTheme.colorScheme.surfaceVariant
      )
    }
  }
}

@Composable
fun MetricTile(
  icon: androidx.compose.ui.graphics.vector.ImageVector,
  iconTint: Color,
  title: String,
  value: String,
  subtitle: String,
  modifier: Modifier = Modifier
) {
  Surface(
    modifier = modifier,
    shape = RoundedCornerShape(14.dp),
    color = MaterialTheme.colorScheme.surface,
    border = androidx.compose.foundation.BorderStroke(
      1.dp,
      MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
    )
  ) {
    Column(modifier = Modifier.padding(12.dp)) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Text(
          text = title,
          style = MaterialTheme.typography.labelSmall,
          color = Slate500
        )
        Box(
          modifier = Modifier
            .size(26.dp)
            .clip(CircleShape)
            .background(iconTint.copy(alpha = 0.12f)),
          contentAlignment = Alignment.Center
        ) {
          Icon(
            imageVector = icon,
            contentDescription = null,
            tint = iconTint,
            modifier = Modifier.size(15.dp)
          )
        }
      }
      Spacer(modifier = Modifier.height(4.dp))
      Text(
        text = value,
        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
        color = MaterialTheme.colorScheme.onSurface
      )
      Text(
        text = subtitle,
        style = MaterialTheme.typography.labelSmall,
        color = Slate400
      )
    }
  }
}

@Composable
fun DailyChartCard(dailyStats: List<DailyStat>) {
  val recentStats = dailyStats.takeLast(10)
  Surface(
    modifier = Modifier.fillMaxWidth(),
    shape = RoundedCornerShape(16.dp),
    color = MaterialTheme.colorScheme.surface,
    border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f))
  ) {
    Column(modifier = Modifier.padding(14.dp)) {
      Text(
        text = "Daily Completion Trend (Recent)",
        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
        color = MaterialTheme.colorScheme.onSurface
      )
      Spacer(modifier = Modifier.height(12.dp))

      Box(
        modifier = Modifier
          .fillMaxWidth()
          .height(110.dp)
      ) {
        val primaryColor = BluePrimary
        val trackColor = MaterialTheme.colorScheme.surfaceVariant

        Canvas(modifier = Modifier.fillMaxSize()) {
          val count = recentStats.size
          if (count == 0) return@Canvas
          val spacing = 8.dp.toPx()
          val totalSpacing = spacing * (count - 1)
          val barWidth = (size.width - totalSpacing) / count
          val maxBarHeight = size.height - 20.dp.toPx()

          recentStats.forEachIndexed { i, stat ->
            val left = i * (barWidth + spacing)
            drawRoundRect(
              color = trackColor,
              topLeft = Offset(left, 0f),
              size = Size(barWidth, maxBarHeight),
              cornerRadius = CornerRadius(6f, 6f)
            )

            val fillHeight = if (stat.totalCount > 0) maxBarHeight * stat.completionRate else 0f
            if (fillHeight > 0) {
              drawRoundRect(
                color = primaryColor,
                topLeft = Offset(left, maxBarHeight - fillHeight),
                size = Size(barWidth, fillHeight),
                cornerRadius = CornerRadius(6f, 6f)
              )
            }
          }
        }
      }

      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
      ) {
        recentStats.forEach { stat ->
          Text(
            text = stat.dayOfWeek.take(2),
            style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
            color = Slate400
          )
        }
      }
    }
  }
}

@Composable
fun DailyStatRow(day: DailyStat) {
  Surface(
    modifier = Modifier.fillMaxWidth(),
    shape = RoundedCornerShape(12.dp),
    color = MaterialTheme.colorScheme.surface,
    border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.4f))
  ) {
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 14.dp, vertical = 10.dp),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically
    ) {
      Column(modifier = Modifier.weight(1f)) {
        Text(
          text = "${day.displayLabel} (${day.dayOfWeek})",
          style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
          color = MaterialTheme.colorScheme.onSurface
        )
        Spacer(modifier = Modifier.height(4.dp))
        LinearProgressIndicator(
          progress = { day.completionRate },
          modifier = Modifier
            .fillMaxWidth(0.85f)
            .height(5.dp)
            .clip(RoundedCornerShape(3.dp)),
          color = BluePrimary,
          trackColor = MaterialTheme.colorScheme.surfaceVariant
        )
      }

      Column(horizontalAlignment = Alignment.End) {
        Text(
          text = "${(day.completionRate * 100).toInt()}%",
          style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
          color = if (day.completionRate >= 1.0f) BluePrimary else MaterialTheme.colorScheme.onSurface
        )
        Text(
          text = "${day.completedCount}/${day.totalCount} Done",
          style = MaterialTheme.typography.labelSmall,
          color = Slate500
        )
      }
    }
  }
}

@Composable
fun WeeklySummaryCard(weeklyStats: List<WeeklyStat>, bestDay: String) {
  Surface(
    modifier = Modifier.fillMaxWidth(),
    shape = RoundedCornerShape(16.dp),
    color = MaterialTheme.colorScheme.surface,
    border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f))
  ) {
    Column(modifier = Modifier.padding(14.dp)) {
      Text(
        text = "Weekly Academic Schedule",
        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
      )
      Spacer(modifier = Modifier.height(6.dp))
      Text(
        text = "Weeks start on Friday and end on Thursday. Each week is color-coded in soft pastel tones.",
        style = MaterialTheme.typography.bodySmall,
        color = Slate500
      )
      Spacer(modifier = Modifier.height(10.dp))
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
      ) {
        Column {
          Text("Best Day of Week", style = MaterialTheme.typography.labelSmall, color = Slate500)
          Text(bestDay, style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold), color = BluePrimary)
        }
        Column(horizontalAlignment = Alignment.End) {
          Text("Total Duration", style = MaterialTheme.typography.labelSmall, color = Slate500)
          Text("13 Weeks (87 Days)", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
        }
      }
    }
  }
}

@Composable
fun WeeklyStatRow(week: WeeklyStat) {
  val palette = WeekColorHelper.getPaletteForDate(week.startDate)

  Surface(
    modifier = Modifier.fillMaxWidth(),
    shape = RoundedCornerShape(14.dp),
    color = palette.backgroundLight,
    border = androidx.compose.foundation.BorderStroke(1.dp, palette.borderLight)
  ) {
    Column(modifier = Modifier.padding(12.dp)) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Box(
            modifier = Modifier
              .clip(RoundedCornerShape(6.dp))
              .background(palette.accentText)
              .padding(horizontal = 6.dp, vertical = 2.dp)
          ) {
            Text(
              text = "W${week.weekNumber}",
              style = MaterialTheme.typography.labelSmall.copy(
                fontWeight = FontWeight.Bold,
                color = Color.White
              )
            )
          }
          Spacer(modifier = Modifier.width(8.dp))
          Text(
            text = "${week.startDate.dayOfMonth} ${com.example.util.BengaliDateHelper.getMonthShort(week.startDate.month)} - ${week.endDate.dayOfMonth} ${com.example.util.BengaliDateHelper.getMonthShort(week.endDate.month)}",
            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
            color = MaterialTheme.colorScheme.onSurface
          )
        }

        Text(
          text = "${(week.completionRate * 100).toInt()}%",
          style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
          color = palette.accentText
        )
      }
      Spacer(modifier = Modifier.height(6.dp))
      LinearProgressIndicator(
        progress = { week.completionRate },
        modifier = Modifier
          .fillMaxWidth()
          .height(5.dp)
          .clip(RoundedCornerShape(3.dp)),
        color = palette.accentText,
        trackColor = palette.borderLight.copy(alpha = 0.5f)
      )
      Spacer(modifier = Modifier.height(4.dp))
      Text(
        text = "${week.completedCount} completed of ${week.totalCount} tasks",
        style = MaterialTheme.typography.labelSmall,
        color = Slate500
      )
    }
  }
}

@Composable
fun RoutineProgressCard(routine: RoutineProgress) {
  Surface(
    modifier = Modifier.fillMaxWidth(),
    shape = RoundedCornerShape(14.dp),
    color = MaterialTheme.colorScheme.surface,
    border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f))
  ) {
    Column(modifier = Modifier.padding(14.dp)) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Box(
            modifier = Modifier
              .size(30.dp)
              .clip(CircleShape)
              .background(BluePrimary.copy(alpha = 0.12f)),
            contentAlignment = Alignment.Center
          ) {
            Icon(
              imageVector = Icons.Default.Repeat,
              contentDescription = null,
              tint = BluePrimary,
              modifier = Modifier.size(15.dp)
            )
          }
          Spacer(modifier = Modifier.width(10.dp))
          Column {
            Text(
              text = routine.groupName,
              style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
              color = MaterialTheme.colorScheme.onSurface
            )
            Text(
              text = "${routine.totalDays}-Day Multi-Day Routine",
              style = MaterialTheme.typography.labelSmall,
              color = Slate500
            )
          }
        }

        Text(
          text = "${(routine.completionRate * 100).toInt()}%",
          style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
          color = BluePrimary
        )
      }

      Spacer(modifier = Modifier.height(8.dp))

      LinearProgressIndicator(
        progress = { routine.completionRate },
        modifier = Modifier
          .fillMaxWidth()
          .height(6.dp)
          .clip(RoundedCornerShape(3.dp)),
        color = BluePrimary,
        trackColor = MaterialTheme.colorScheme.surfaceVariant
      )

      Spacer(modifier = Modifier.height(4.dp))

      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
      ) {
        Text(
          text = "${routine.completedDays} of ${routine.totalDays} Days Completed",
          style = MaterialTheme.typography.labelSmall,
          color = Slate500
        )
        if (routine.completedDays == routine.totalDays) {
          Text(
            text = "Routine Complete!",
            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
            color = Color(0xFF10B981)
          )
        }
      }
    }
  }
}
