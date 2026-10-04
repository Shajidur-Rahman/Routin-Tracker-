package com.example.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.CloudSync
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.TaskEntity
import com.example.ui.theme.BluePrimary
import com.example.ui.theme.Slate400
import com.example.ui.theme.Slate500
import com.example.ui.theme.TaskCompletedBgDark
import com.example.ui.theme.TaskCompletedBgLight
import com.example.ui.theme.TaskCompletedBlue
import com.example.ui.theme.TaskCompletedBorderDark
import com.example.ui.theme.TaskCompletedBorderLight

@Composable
fun TaskItem(
  task: TaskEntity,
  onToggleCompletion: () -> Unit,
  onDelete: () -> Unit,
  modifier: Modifier = Modifier
) {
  val isDark = isSystemInDarkTheme()
  val itemShape = RoundedCornerShape(16.dp)

  // Turn blue when completed!
  val targetBgColor = if (task.isCompleted) {
    if (isDark) TaskCompletedBgDark else TaskCompletedBgLight
  } else {
    MaterialTheme.colorScheme.surface
  }

  val targetBorderColor = if (task.isCompleted) {
    if (isDark) TaskCompletedBorderDark else TaskCompletedBorderLight
  } else {
    MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
  }

  val backgroundColor by animateColorAsState(targetValue = targetBgColor, label = "taskBg")
  val borderColor by animateColorAsState(targetValue = targetBorderColor, label = "taskBorder")

  Surface(
    modifier = modifier
      .fillMaxWidth()
      .clip(itemShape)
      .border(
        width = if (task.isCompleted) 1.8.dp else 1.dp,
        color = borderColor,
        shape = itemShape
      )
      .clickable(onClick = onToggleCompletion)
      .testTag("task_item_${task.id}"),
    color = backgroundColor,
    tonalElevation = if (task.isCompleted) 3.dp else 0.dp
  ) {
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 14.dp, vertical = 12.dp),
      verticalAlignment = Alignment.CenterVertically
    ) {
      // Checkbox circle (turns deep royal blue when checked)
      Box(
        modifier = Modifier
          .size(28.dp)
          .clip(CircleShape)
          .background(if (task.isCompleted) TaskCompletedBlue else Color.Transparent)
          .border(
            width = 2.dp,
            color = if (task.isCompleted) TaskCompletedBlue else Slate400,
            shape = CircleShape
          ),
        contentAlignment = Alignment.Center
      ) {
        if (task.isCompleted) {
          Icon(
            imageVector = Icons.Default.Check,
            contentDescription = "Completed",
            tint = Color.White,
            modifier = Modifier.size(18.dp)
          )
        }
      }

      Spacer(modifier = Modifier.width(14.dp))

      // Middle content: Title, routine badge, sync state
      Column(
        modifier = Modifier.weight(1f),
        verticalArrangement = Arrangement.Center
      ) {
        Row(
          verticalAlignment = Alignment.CenterVertically
        ) {
          Text(
            text = task.title,
            style = MaterialTheme.typography.titleMedium.copy(
              fontWeight = if (task.isCompleted) FontWeight.SemiBold else FontWeight.Medium,
              textDecoration = if (task.isCompleted) TextDecoration.None else TextDecoration.None
            ),
            color = if (task.isCompleted) {
              if (isDark) Color(0xFF93C5FD) else TaskCompletedBlue
            } else {
              MaterialTheme.colorScheme.onSurface
            }
          )

          if (task.routineGroup != null && task.routineDayIndex != null) {
            Spacer(modifier = Modifier.width(6.dp))
            Box(
              modifier = Modifier
                .clip(RoundedCornerShape(6.dp))
                .background(if (task.isCompleted) BluePrimary.copy(alpha = 0.2f) else MaterialTheme.colorScheme.secondaryContainer)
                .padding(horizontal = 6.dp, vertical = 2.dp)
            ) {
              Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                  imageVector = Icons.Default.Repeat,
                  contentDescription = "Routine",
                  tint = if (task.isCompleted) BluePrimary else MaterialTheme.colorScheme.onSecondaryContainer,
                  modifier = Modifier.size(11.dp)
                )
                Spacer(modifier = Modifier.width(3.dp))
                Text(
                  text = "Day ${task.routineDayIndex}/${task.routineTotalDays ?: ""}",
                  style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                  color = if (task.isCompleted) BluePrimary else MaterialTheme.colorScheme.onSecondaryContainer
                )
              }
            }
          }
        }

        Spacer(modifier = Modifier.height(4.dp))

        // Subtitle row: Subject, Reminder time, and Google Sheet Sync status
        Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          if (task.subject.isNotBlank()) {
            Text(
              text = task.subject,
              style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
              color = Slate500
            )
          }

          if (!task.reminderTime.isNullOrBlank()) {
            Row(verticalAlignment = Alignment.CenterVertically) {
              Icon(
                imageVector = Icons.Default.Alarm,
                contentDescription = "Reminder",
                tint = Slate500,
                modifier = Modifier.size(12.dp)
              )
              Spacer(modifier = Modifier.width(2.dp))
              Text(
                text = task.reminderTime,
                style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
                color = Slate500
              )
            }
          }

          if (task.isCompleted) {
            Row(verticalAlignment = Alignment.CenterVertically) {
              Icon(
                imageVector = Icons.Default.Check,
                contentDescription = "Done",
                tint = if (isDark) Color(0xFF93C5FD) else TaskCompletedBlue,
                modifier = Modifier.size(12.dp)
              )
              Spacer(modifier = Modifier.width(3.dp))
              Text(
                text = "Done",
                style = MaterialTheme.typography.labelSmall.copy(
                  fontSize = 11.sp,
                  fontWeight = FontWeight.Bold
                ),
                color = if (isDark) Color(0xFF93C5FD) else TaskCompletedBlue
              )
            }
          }
        }
      }

      // Delete action
      IconButton(
        onClick = onDelete,
        modifier = Modifier
          .size(36.dp)
          .testTag("delete_task_${task.id}")
      ) {
        Icon(
          imageVector = Icons.Default.DeleteOutline,
          contentDescription = "Delete task",
          tint = Slate400,
          modifier = Modifier.size(20.dp)
        )
      }
    }
  }
}
