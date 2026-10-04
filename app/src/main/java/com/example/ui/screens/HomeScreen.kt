package com.example.ui.screens

import android.content.res.Configuration
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Today
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.SheetDateInfo
import com.example.data.model.TaskEntity
import com.example.ui.components.AddRoutineDialog
import com.example.ui.components.DateCard
import com.example.ui.components.TaskItem
import com.example.ui.theme.BluePrimary
import com.example.ui.theme.Slate400
import com.example.ui.theme.Slate500
import com.example.ui.viewmodel.MainViewModel
import com.example.util.BengaliDateHelper
import kotlinx.coroutines.launch
import java.time.LocalDate

@Composable
fun HomeScreen(
  viewModel: MainViewModel,
  modifier: Modifier = Modifier
) {
  val selectedDate by viewModel.selectedDate.collectAsState()
  val dateCards by viewModel.dateCards.collectAsState()
  val tasks by viewModel.tasksForSelectedDate.collectAsState()
  val isSyncing by viewModel.isSyncing.collectAsState()
  val syncMessage by viewModel.syncMessage.collectAsState()
  val isWriteConfigured by viewModel.isSheetWriteConfigured.collectAsState()

  var showAddDialog by remember { mutableStateOf(false) }
  val horizontalListState = rememberLazyListState()
  val verticalListState = rememberLazyListState()
  val scope = rememberCoroutineScope()

  val configuration = LocalConfiguration.current
  val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE

  // Auto-scroll to today or selected date
  LaunchedEffect(dateCards.isNotEmpty(), isLandscape) {
    if (dateCards.isNotEmpty()) {
      val today = LocalDate.now()
      val targetIndex = dateCards.indexOfFirst { it.date == today }.coerceAtLeast(0)
      if (targetIndex >= 0) {
        if (isLandscape) {
          verticalListState.scrollToItem(maxOf(0, targetIndex - 1))
        } else {
          horizontalListState.scrollToItem(maxOf(0, targetIndex - 1))
        }
      }
    }
  }

  Box(modifier = modifier.fillMaxSize()) {
    Column(
      modifier = Modifier
        .fillMaxSize()
        .background(MaterialTheme.colorScheme.background)
    ) {
      // Top App Bar
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Column {
          Text(
            text = "RoutineTrack",
            style = MaterialTheme.typography.titleLarge.copy(
              fontWeight = FontWeight.ExtraBold,
              letterSpacing = (-0.5).sp
            ),
            color = MaterialTheme.colorScheme.onBackground
          )
          Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
              modifier = Modifier
                .size(7.dp)
                .clip(CircleShape)
                .background(if (isWriteConfigured) Color(0xFF10B981) else Color(0xFFF59E0B))
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
              text = if (isWriteConfigured) "Sheet: Live Write Active" else "Sheet: 5 Oct - 30 Dec (Read-Only)",
              style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
              color = if (isWriteConfigured) Color(0xFF059669) else Slate500
            )
          }
        }

        Row(verticalAlignment = Alignment.CenterVertically) {
          // Jump to Today button
          IconButton(
            onClick = {
              viewModel.jumpToToday()
              val today = LocalDate.now()
              val idx = dateCards.indexOfFirst { it.date == today }
              if (idx >= 0) {
                scope.launch {
                  if (isLandscape) {
                    verticalListState.animateScrollToItem(maxOf(0, idx - 1))
                  } else {
                    horizontalListState.animateScrollToItem(maxOf(0, idx - 1))
                  }
                }
              }
            },
            modifier = Modifier.testTag("btn_jump_today")
          ) {
            Icon(
              imageVector = Icons.Default.Today,
              contentDescription = "Jump to Today",
              tint = BluePrimary
            )
          }

          // Manual Sheet Sync
          IconButton(
            onClick = { viewModel.manualSyncWithSheet() },
            modifier = Modifier.testTag("btn_sync_sheet")
          ) {
            if (isSyncing) {
              CircularProgressIndicator(
                modifier = Modifier.size(20.dp),
                strokeWidth = 2.dp,
                color = BluePrimary
              )
            } else {
              Icon(
                imageVector = Icons.Default.Refresh,
                contentDescription = "Sync Google Sheet",
                tint = MaterialTheme.colorScheme.onSurfaceVariant
              )
            }
          }
        }
      }

      // Sync Notification Banner
      AnimatedVisibility(
        visible = syncMessage != null,
        enter = fadeIn(),
        exit = fadeOut()
      ) {
        syncMessage?.let { msg ->
          Surface(
            modifier = Modifier
              .fillMaxWidth()
              .padding(horizontal = 16.dp, vertical = 2.dp),
            shape = RoundedCornerShape(10.dp),
            color = BluePrimary.copy(alpha = 0.12f),
            border = androidx.compose.foundation.BorderStroke(1.dp, BluePrimary.copy(alpha = 0.3f))
          ) {
            Row(
              modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 10.dp, vertical = 6.dp),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically
              ) {
                Icon(
                  imageVector = Icons.Default.CloudDone,
                  contentDescription = null,
                  tint = BluePrimary,
                  modifier = Modifier.size(15.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                  text = msg,
                  style = MaterialTheme.typography.bodySmall.copy(
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium
                  ),
                  color = MaterialTheme.colorScheme.onSurface
                )
              }
              IconButton(
                onClick = { viewModel.clearSyncMessage() },
                modifier = Modifier.size(20.dp)
              ) {
                Icon(
                  imageVector = Icons.Default.Close,
                  contentDescription = "Dismiss",
                  tint = Slate400,
                  modifier = Modifier.size(14.dp)
                )
              }
            }
          }
        }
      }

      // Split Screen when phone is rotated (Landscape) vs Stacked (Portrait)
      if (isLandscape) {
        // ROTATED / LANDSCAPE: Left side = Dates, Right side = Subjects / To-Do
        Row(
          modifier = Modifier
            .fillMaxSize()
            .padding(top = 4.dp)
        ) {
          // Left Pane: Dates List
          Column(
            modifier = Modifier
              .width(280.dp)
              .fillMaxHeight()
              .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
              .padding(horizontal = 10.dp, vertical = 6.dp)
          ) {
            Text(
              text = "Dates (5 Oct - 30 Dec)",
              style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
              color = Slate500,
              modifier = Modifier.padding(bottom = 6.dp)
            )

            LazyColumn(
              state = verticalListState,
              verticalArrangement = Arrangement.spacedBy(10.dp),
              modifier = Modifier.fillMaxSize()
            ) {
              items(dateCards, key = { it.dateIso }) { dateInfo ->
                val isSelected = dateInfo.date == selectedDate
                DateCard(
                  dateInfo = dateInfo,
                  isSelected = isSelected,
                  onSelect = { viewModel.selectDate(dateInfo.date) },
                  modifier = Modifier.fillMaxWidth()
                )
              }
            }
          }

          // Divider
          Box(
            modifier = Modifier
              .width(1.dp)
              .fillMaxHeight()
              .background(MaterialTheme.colorScheme.outline.copy(alpha = 0.4f))
          )

          // Right Pane: Active Selected Date & Subjects / Tasks
          Column(
            modifier = Modifier
              .weight(1f)
              .fillMaxHeight()
              .padding(horizontal = 16.dp, vertical = 6.dp)
          ) {
            RightPaneContent(
              selectedDate = selectedDate,
              tasks = tasks,
              onAddSubjectClick = { showAddDialog = true },
              onToggleTask = { viewModel.toggleTaskCompletion(it) },
              onDeleteTask = { viewModel.deleteTask(it) }
            )
          }
        }
      } else {
        // PORTRAIT: Top = Horizontal Date Cards Carousel, Bottom = Subjects / Tasks
        Column(modifier = Modifier.fillMaxSize()) {
          // Date Cards Carousel
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .padding(horizontal = 16.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Text(
              text = "5 Oct - 30 Dec (Fri - Thu Weeks)",
              style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
              color = MaterialTheme.colorScheme.onBackground
            )
            Text(
              text = "${dateCards.size} Days",
              style = MaterialTheme.typography.labelSmall,
              color = Slate400
            )
          }

          LazyRow(
            state = horizontalListState,
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier
              .fillMaxWidth()
              .testTag("date_cards_row")
          ) {
            items(dateCards, key = { it.dateIso }) { dateInfo ->
              val isSelected = dateInfo.date == selectedDate
              DateCard(
                dateInfo = dateInfo,
                isSelected = isSelected,
                onSelect = { viewModel.selectDate(dateInfo.date) }
              )
            }
          }

          Spacer(modifier = Modifier.height(12.dp))

          // Subjects and tasks list
          Column(
            modifier = Modifier
              .fillMaxWidth()
              .weight(1f)
              .padding(horizontal = 16.dp)
          ) {
            RightPaneContent(
              selectedDate = selectedDate,
              tasks = tasks,
              onAddSubjectClick = { showAddDialog = true },
              onToggleTask = { viewModel.toggleTaskCompletion(it) },
              onDeleteTask = { viewModel.deleteTask(it) }
            )
          }
        }
      }
    }

    // Add Routine Dialog
    if (showAddDialog) {
      AddRoutineDialog(
        initialStartDate = selectedDate,
        onDismiss = { showAddDialog = false },
        onConfirm = { name, subject, startDate, days, reminderTime ->
          showAddDialog = false
          viewModel.addTaskOrRoutine(
            name = name,
            subject = subject,
            startDate = startDate,
            daysToContinue = days,
            reminderTime = reminderTime
          )
        }
      )
    }
  }
}

@Composable
private fun RightPaneContent(
  selectedDate: LocalDate,
  tasks: List<TaskEntity>,
  onAddSubjectClick: () -> Unit,
  onToggleTask: (TaskEntity) -> Unit,
  onDeleteTask: (TaskEntity) -> Unit
) {
  val activeLabel = BengaliDateHelper.formatSpreadsheetDate(selectedDate)
  val weekNum = com.example.util.WeekColorHelper.getWeekNumber(selectedDate)

  Row(
    modifier = Modifier
      .fillMaxWidth()
      .padding(vertical = 4.dp),
    horizontalArrangement = Arrangement.SpaceBetween,
    verticalAlignment = Alignment.CenterVertically
  ) {
    Column {
      Row(verticalAlignment = Alignment.CenterVertically) {
        Text(
          text = activeLabel,
          style = MaterialTheme.typography.titleMedium.copy(
            fontWeight = FontWeight.Bold,
            fontSize = 18.sp
          ),
          color = BluePrimary
        )
        Spacer(modifier = Modifier.width(8.dp))
        Box(
          modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
            .background(BluePrimary.copy(alpha = 0.12f))
            .padding(horizontal = 6.dp, vertical = 2.dp)
        ) {
          Text(
            text = "Week $weekNum",
            style = MaterialTheme.typography.labelSmall.copy(
              fontWeight = FontWeight.Bold,
              fontSize = 10.sp
            ),
            color = BluePrimary
          )
        }
      }
      Text(
        text = if (selectedDate == LocalDate.now()) "Today's Routine & Subjects" else "Scheduled Subjects",
        style = MaterialTheme.typography.labelSmall,
        color = Slate500
      )
    }

    // Add Subject button
    Button(
      onClick = onAddSubjectClick,
      colors = ButtonDefaults.buttonColors(containerColor = BluePrimary),
      shape = RoundedCornerShape(10.dp),
      contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
      modifier = Modifier.testTag("btn_add_subject")
    ) {
      Icon(
        imageVector = Icons.Default.Add,
        contentDescription = "Add",
        modifier = Modifier.size(16.dp)
      )
      Spacer(modifier = Modifier.width(4.dp))
      Text(
        text = "Add Subject",
        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
      )
    }
  }

  Spacer(modifier = Modifier.height(6.dp))

  if (tasks.isEmpty()) {
    Box(
      modifier = Modifier
        .fillMaxWidth()
        .padding(vertical = 32.dp),
      contentAlignment = Alignment.Center
    ) {
      Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
      ) {
        Surface(
          modifier = Modifier.size(52.dp),
          shape = CircleShape,
          color = MaterialTheme.colorScheme.surfaceVariant
        ) {
          Box(contentAlignment = Alignment.Center) {
            Icon(
              imageVector = Icons.Default.CalendarToday,
              contentDescription = null,
              tint = Slate400,
              modifier = Modifier.size(26.dp)
            )
          }
        }
        Spacer(modifier = Modifier.height(10.dp))
        Text(
          text = "No subjects scheduled for this date",
          style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
          color = MaterialTheme.colorScheme.onSurface
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
          text = "Tap '+ Add Subject' to select from the 11 subjects or create a multi-day routine.",
          style = MaterialTheme.typography.bodySmall,
          color = Slate500,
          textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )
      }
    }
  } else {
    LazyColumn(
      modifier = Modifier
        .fillMaxWidth()
        .testTag("tasks_list"),
      contentPadding = PaddingValues(top = 4.dp, bottom = 80.dp),
      verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
      items(tasks, key = { it.id }) { task ->
        TaskItem(
          task = task,
          onToggleCompletion = { onToggleTask(task) },
          onDelete = { onDeleteTask(task) }
        )
      }
    }
  }
}
