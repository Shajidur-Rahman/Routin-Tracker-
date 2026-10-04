package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.BluePrimary
import com.example.ui.theme.Slate400
import com.example.ui.theme.Slate500
import com.example.util.BengaliDateHelper
import java.time.LocalDate

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AddRoutineDialog(
  initialStartDate: LocalDate,
  onDismiss: () -> Unit,
  onConfirm: (name: String, subject: String, startDate: LocalDate, daysToContinue: Int, reminderTime: String?) -> Unit
) {
  // 11 requested subjects:
  // Physics 1st, Physics 2nd, Math 1st, Math 2nd, Chemistry 1st, Chemistry 2nd,
  // Biology 1st, Biology 2nd, Bangla, English, ICT
  val academicSubjects = listOf(
    "Physics 1st", "Physics 2nd",
    "Math 1st", "Math 2nd",
    "Chemistry 1st", "Chemistry 2nd",
    "Biology 1st", "Biology 2nd",
    "Bangla", "English", "ICT"
  )

  var selectedSubject by remember { mutableStateOf(academicSubjects[0]) }
  var taskName by remember { mutableStateOf(academicSubjects[0]) }
  var daysToContinue by remember { mutableIntStateOf(4) }
  var enableReminder by remember { mutableStateOf(false) }
  var reminderTime by remember { mutableStateOf("09:00") }

  val presetDays = listOf(1, 2, 3, 4, 7, 14, 30)
  val reminderPresets = listOf("07:00", "08:30", "10:00", "14:00", "19:00", "21:00")

  val scrollState = rememberScrollState()

  AlertDialog(
    onDismissRequest = onDismiss,
    title = {
      Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
          modifier = Modifier
            .size(36.dp)
            .clip(CircleShape)
            .background(BluePrimary.copy(alpha = 0.12f)),
          contentAlignment = Alignment.Center
        ) {
          Icon(
            imageVector = Icons.Default.Repeat,
            contentDescription = null,
            tint = BluePrimary,
            modifier = Modifier.size(20.dp)
          )
        }
        Spacer(modifier = Modifier.width(10.dp))
        Text(
          text = "Add Subject / Routine",
          style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
        )
      }
    },
    text = {
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .heightIn(max = 500.dp)
          .verticalScroll(scrollState)
      ) {
        // 11 Subject Category Selector
        Text(
          text = "Select Subject (11 Subjects)",
          style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
          color = MaterialTheme.colorScheme.onSurface
        )
        Spacer(modifier = Modifier.height(6.dp))
        FlowRow(
          horizontalArrangement = Arrangement.spacedBy(6.dp),
          verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
          academicSubjects.forEach { subj ->
            val isSelected = selectedSubject == subj
            Box(
              modifier = Modifier
                .clip(RoundedCornerShape(8.dp))
                .background(if (isSelected) BluePrimary else MaterialTheme.colorScheme.surfaceVariant)
                .clickable {
                  selectedSubject = subj
                  taskName = subj
                }
                .padding(horizontal = 9.dp, vertical = 6.dp)
            ) {
              Text(
                text = subj,
                style = MaterialTheme.typography.labelSmall.copy(
                  fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                ),
                color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
              )
            }
          }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Task / Routine Name field
        Text(
          text = "Task / Routine Name",
          style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
          color = MaterialTheme.colorScheme.onSurface
        )
        Spacer(modifier = Modifier.height(4.dp))
        OutlinedTextField(
          value = taskName,
          onValueChange = { taskName = it },
          placeholder = { Text("e.g. Physics 1st, Circle, Math 1st") },
          modifier = Modifier
            .fillMaxWidth()
            .testTag("input_task_name"),
          singleLine = true,
          shape = RoundedCornerShape(12.dp)
        )

        Spacer(modifier = Modifier.height(14.dp))

        // Days to continue (Multi-day Routine feature)
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Column {
            Text(
              text = "How many days to continue?",
              style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
              color = MaterialTheme.colorScheme.onSurface
            )
            Text(
              text = if (daysToContinue == 1) "Single day task" else "Consecutive routine ($daysToContinue days)",
              style = MaterialTheme.typography.labelSmall,
              color = Slate500
            )
          }

          // Stepper (+ and -)
          Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
              .clip(RoundedCornerShape(10.dp))
              .background(MaterialTheme.colorScheme.surfaceVariant)
              .padding(horizontal = 4.dp, vertical = 2.dp)
          ) {
            IconButton(
              onClick = { if (daysToContinue > 1) daysToContinue-- },
              modifier = Modifier.size(32.dp)
            ) {
              Icon(Icons.Default.Remove, contentDescription = "Decrease days", modifier = Modifier.size(16.dp))
            }

            Text(
              text = "$daysToContinue",
              style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
              modifier = Modifier.padding(horizontal = 8.dp)
            )

            IconButton(
              onClick = { if (daysToContinue < 90) daysToContinue++ },
              modifier = Modifier.size(32.dp)
            ) {
              Icon(Icons.Default.Add, contentDescription = "Increase days", modifier = Modifier.size(16.dp))
            }
          }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Preset day chips
        FlowRow(
          horizontalArrangement = Arrangement.spacedBy(6.dp),
          verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
          presetDays.forEach { count ->
            val isSelected = daysToContinue == count
            Box(
              modifier = Modifier
                .clip(RoundedCornerShape(8.dp))
                .background(if (isSelected) BluePrimary else MaterialTheme.colorScheme.surfaceVariant)
                .clickable { daysToContinue = count }
                .padding(horizontal = 10.dp, vertical = 6.dp)
            ) {
              Text(
                text = if (count == 1) "1 Day" else "$count Days",
                style = MaterialTheme.typography.labelSmall.copy(
                  fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                ),
                color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
              )
            }
          }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Routine sequence demo and format box (Requested: "one row shoing how the formet will be")
        Surface(
          modifier = Modifier.fillMaxWidth(),
          shape = RoundedCornerShape(12.dp),
          color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f),
          border = androidx.compose.foundation.BorderStroke(1.dp, BluePrimary.copy(alpha = 0.3f))
        ) {
          Column(modifier = Modifier.padding(10.dp)) {
            val displayName = taskName.trim().ifEmpty { selectedSubject }

            // Dedicated format demo row as requested
            Surface(
              shape = RoundedCornerShape(6.dp),
              color = MaterialTheme.colorScheme.surface,
              modifier = Modifier.fillMaxWidth()
            ) {
              Row(
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp),
                verticalAlignment = Alignment.CenterVertically
              ) {
                Text(
                  text = "FORMAT:",
                  style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 10.sp
                  ),
                  color = BluePrimary
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                  text = "[Task Name]-[Day#]  →  $displayName-1, $displayName-2 ...",
                  style = MaterialTheme.typography.bodySmall.copy(
                    fontFamily = FontFamily.Monospace,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold
                  ),
                  color = MaterialTheme.colorScheme.onSurface
                )
              }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
              text = if (daysToContinue == 1) "Scheduled Sequence:" else "Routine Sequence ($daysToContinue Days):",
              style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
              color = BluePrimary
            )
            Spacer(modifier = Modifier.height(4.dp))
            if (daysToContinue == 1) {
              Text(
                text = "• $displayName on ${BengaliDateHelper.formatSpreadsheetDate(initialStartDate)}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurface
              )
            } else {
              val previewCount = minOf(daysToContinue, 4)
              for (i in 1..previewCount) {
                val targetD = initialStartDate.plusDays((i - 1).toLong())
                Text(
                  text = "• $displayName-$i  (${BengaliDateHelper.formatSpreadsheetDate(targetD)})",
                  style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp),
                  color = MaterialTheme.colorScheme.onSurface
                )
              }
              if (daysToContinue > 4) {
                Text(
                  text = "... up to $displayName-$daysToContinue",
                  style = MaterialTheme.typography.bodySmall.copy(
                    fontSize = 11.sp,
                    color = Slate500
                  )
                )
              }
            }
          }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Reminder toggle & time selector
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
              imageVector = Icons.Default.Alarm,
              contentDescription = null,
              tint = if (enableReminder) BluePrimary else Slate400,
              modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Column {
              Text(
                text = "Daily Reminder",
                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold)
              )
              Text(
                text = if (enableReminder) "Alarm set at $reminderTime" else "No reminder alert",
                style = MaterialTheme.typography.labelSmall,
                color = Slate500
              )
            }
          }

          Switch(
            checked = enableReminder,
            onCheckedChange = { enableReminder = it },
            modifier = Modifier.testTag("toggle_reminder")
          )
        }

        if (enableReminder) {
          Spacer(modifier = Modifier.height(8.dp))
          FlowRow(
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
          ) {
            reminderPresets.forEach { time ->
              val isSelected = reminderTime == time
              Box(
                modifier = Modifier
                  .clip(RoundedCornerShape(8.dp))
                  .background(if (isSelected) BluePrimary else MaterialTheme.colorScheme.surfaceVariant)
                  .clickable { reminderTime = time }
                  .padding(horizontal = 8.dp, vertical = 4.dp)
              ) {
                Text(
                  text = time,
                  style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                  ),
                  color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                )
              }
            }
          }
        }
      }
    },
    confirmButton = {
      Button(
        onClick = {
          val clean = taskName.trim().ifEmpty { selectedSubject }
          onConfirm(
            clean,
            selectedSubject,
            initialStartDate,
            daysToContinue,
            if (enableReminder) reminderTime else null
          )
        },
        colors = ButtonDefaults.buttonColors(containerColor = BluePrimary),
        shape = RoundedCornerShape(10.dp),
        modifier = Modifier.testTag("btn_confirm_add_task")
      ) {
        Text(if (daysToContinue > 1) "Create $daysToContinue-Day Routine" else "Add Subject", fontWeight = FontWeight.Bold)
      }
    },
    dismissButton = {
      TextButton(onClick = onDismiss) {
        Text("Cancel", color = Slate500)
      }
    },
    shape = RoundedCornerShape(20.dp)
  )
}
