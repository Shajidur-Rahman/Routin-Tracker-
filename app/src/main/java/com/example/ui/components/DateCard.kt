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
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.SheetDateInfo
import com.example.ui.theme.Slate400
import com.example.ui.theme.Slate500

@Composable
fun DateCard(
  dateInfo: SheetDateInfo,
  isSelected: Boolean,
  onSelect: () -> Unit,
  modifier: Modifier = Modifier
) {
  val isDark = isSystemInDarkTheme()
  val cardShape = RoundedCornerShape(20.dp)
  val palette = dateInfo.weekPalette

  val defaultBg = if (isDark) palette.backgroundDark else palette.backgroundLight
  val defaultBorder = if (isDark) palette.borderDark else palette.borderLight

  val backgroundColor by animateColorAsState(
    targetValue = if (isSelected) {
      if (isDark) palette.backgroundDark else palette.backgroundLight
    } else {
      defaultBg
    },
    label = "cardBg"
  )

  val borderColor by animateColorAsState(
    targetValue = when {
      isSelected -> palette.accentText
      dateInfo.isToday -> palette.accentText
      else -> defaultBorder
    },
    label = "cardBorder"
  )

  Surface(
    modifier = modifier
      .width(134.dp)
      .height(180.dp)
      .clip(cardShape)
      .border(
        width = if (isSelected) 2.5.dp else if (dateInfo.isToday) 1.8.dp else 1.2.dp,
        color = borderColor,
        shape = cardShape
      )
      .clickable(onClick = onSelect)
      .testTag("date_card_${dateInfo.dateIso}"),
    color = backgroundColor,
    tonalElevation = if (isSelected) 4.dp else 0.dp
  ) {
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .padding(11.dp),
      horizontalAlignment = Alignment.CenterHorizontally,
      verticalArrangement = Arrangement.SpaceBetween
    ) {
      // Top row: Week Tag & Today badge
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Box(
          modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
            .background(palette.accentText.copy(alpha = 0.15f))
            .padding(horizontal = 5.dp, vertical = 2.dp)
        ) {
          Text(
            text = "W${dateInfo.weekNumber}",
            style = MaterialTheme.typography.labelSmall.copy(
              fontSize = 9.sp,
              fontWeight = FontWeight.Bold
            ),
            color = palette.accentText
          )
        }

        if (dateInfo.isToday) {
          Box(
            modifier = Modifier
              .clip(RoundedCornerShape(6.dp))
              .background(palette.accentText)
              .padding(horizontal = 6.dp, vertical = 2.dp)
          ) {
            Text(
              text = "TODAY",
              style = MaterialTheme.typography.labelSmall.copy(
                fontSize = 8.sp,
                fontWeight = FontWeight.Black
              ),
              color = Color.White
            )
          }
        } else {
          Text(
            text = dateInfo.monthShort.uppercase(),
            style = MaterialTheme.typography.labelSmall.copy(
              fontWeight = FontWeight.Bold,
              letterSpacing = 0.5.sp
            ),
            color = Slate500
          )
        }
      }

      // Middle: Big Bold Day Number (Bengali + English)
      Column(
        horizontalAlignment = Alignment.CenterHorizontally
      ) {
        Text(
          text = dateInfo.bengaliDayNum,
          style = MaterialTheme.typography.headlineLarge.copy(
            fontSize = 38.sp,
            fontWeight = FontWeight.ExtraBold,
            lineHeight = 40.sp
          ),
          color = if (isSelected) palette.accentText else MaterialTheme.colorScheme.onSurface
        )

        Text(
          text = "${dateInfo.englishDayName} • ${dateInfo.bengaliDayName}",
          style = MaterialTheme.typography.bodySmall.copy(
            fontWeight = if (isSelected || dateInfo.isToday) FontWeight.Bold else FontWeight.Medium
          ),
          color = if (isSelected) palette.accentText else Slate500
        )
      }

      // Bottom: Progress and task count
      Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
      ) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Text(
            text = if (dateInfo.totalTasks == 0) "No tasks" else "${dateInfo.completedTasks}/${dateInfo.totalTasks} done",
            style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
            color = if (dateInfo.totalTasks > 0 && dateInfo.completedTasks == dateInfo.totalTasks) {
              palette.accentText
            } else {
              Slate400
            }
          )

          if (dateInfo.totalTasks > 0 && dateInfo.completedTasks == dateInfo.totalTasks) {
            Box(
              modifier = Modifier
                .size(16.dp)
                .clip(CircleShape)
                .background(palette.accentText),
              contentAlignment = Alignment.Center
            ) {
              Icon(
                imageVector = Icons.Default.Check,
                contentDescription = "Done",
                tint = Color.White,
                modifier = Modifier.size(11.dp)
              )
            }
          }
        }

        Spacer(modifier = Modifier.height(4.dp))

        LinearProgressIndicator(
          progress = { if (dateInfo.totalTasks > 0) dateInfo.completionRate else 0f },
          modifier = Modifier
            .fillMaxWidth()
            .height(5.dp)
            .clip(RoundedCornerShape(3.dp)),
          color = palette.accentText,
          trackColor = palette.borderLight.copy(alpha = 0.5f)
        )
      }
    }
  }
}
