package com.example.ui.call

import android.app.KeyguardManager
import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.media.Ringtone
import android.media.RingtoneManager
import android.os.Build
import android.os.Bundle
import android.os.CombinedVibration
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.CallEnd
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.PhoneInTalk
import androidx.compose.material.icons.filled.School
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.MainActivity
import com.example.RoutineApplication
import com.example.data.model.TaskEntity
import com.example.ui.theme.BluePrimary
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.theme.Slate400
import com.example.ui.theme.Slate500
import com.example.util.BengaliDateHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.time.LocalDate

class StudyCallActivity : ComponentActivity() {

  private var ringtone: Ringtone? = null
  private var vibrator: Vibrator? = null

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()

    // 1. Wake screen and show on top of lock screen
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
      setShowWhenLocked(true)
      setTurnScreenOn(true)
      val keyguardManager = getSystemService(Context.KEYGUARD_SERVICE) as? KeyguardManager
      keyguardManager?.requestDismissKeyguard(this, null)
    } else {
      @Suppress("DEPRECATION")
      window.addFlags(
        WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or
            WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON or
            WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD
      )
    }
    window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

    // 2. Start ringtone & vibration
    startRingtoneAndVibration()

    val app = application as RoutineApplication
    val repository = app.repository
    val todayIso = BengaliDateHelper.toIsoString(LocalDate.now())

    setContent {
      MyApplicationTheme {
        val tasks by repository.getTasksForDate(todayIso).collectAsState(initial = emptyList())
        val scope = rememberCoroutineScope()

        StudyCallScreen(
          tasks = tasks,
          onDismissCall = {
            stopAudioAndVibrate()
            finish()
          },
          onAcceptCall = {
            stopAudioAndVibrate()
            val mainIntent = Intent(this, MainActivity::class.java).apply {
              flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            }
            startActivity(mainIntent)
            finish()
          },
          onToggleTask = { task ->
            scope.launch(Dispatchers.IO) {
              repository.toggleTaskCompletion(task)
            }
          }
        )
      }
    }
  }

  private fun startRingtoneAndVibration() {
    try {
      val ringtoneUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE)
        ?: RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
      ringtone = RingtoneManager.getRingtone(applicationContext, ringtoneUri)
      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
        ringtone?.audioAttributes = AudioAttributes.Builder()
          .setUsage(AudioAttributes.USAGE_NOTIFICATION_RINGTONE)
          .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
          .build()
      }
      ringtone?.play()

      vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        val vibratorManager = getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
        vibratorManager.defaultVibrator
      } else {
        @Suppress("DEPRECATION")
        getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
      }

      val pattern = longArrayOf(0, 1000, 800, 1000, 800, 1000)
      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        vibrator?.vibrate(VibrationEffect.createWaveform(pattern, 0))
      } else {
        @Suppress("DEPRECATION")
        vibrator?.vibrate(pattern, 0)
      }
    } catch (e: Exception) {
      e.printStackTrace()
    }
  }

  override fun onAttachedToWindow() {
    super.onAttachedToWindow()
    @Suppress("DEPRECATION")
    window.addFlags(
      WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or
          WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD or
          WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON or
          WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON
    )
  }

  private fun stopAudioAndVibrate() {
    try {
      ringtone?.stop()
      vibrator?.cancel()
      val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as? android.app.NotificationManager
      notificationManager?.cancel(com.example.service.StudyCallService.NOTIFICATION_ID)
      val stopServiceIntent = Intent(this, com.example.service.StudyCallService::class.java).apply {
        action = com.example.service.StudyCallService.ACTION_STOP_CALL
      }
      startService(stopServiceIntent)
    } catch (e: Exception) {
      e.printStackTrace()
    }
  }

  override fun onDestroy() {
    super.onDestroy()
    stopAudioAndVibrate()
  }
}

@Composable
fun StudyCallScreen(
  tasks: List<TaskEntity>,
  onDismissCall: () -> Unit,
  onAcceptCall: () -> Unit,
  onToggleTask: (TaskEntity) -> Unit
) {
  val total = tasks.size
  val completed = tasks.count { it.isCompleted }
  val pending = tasks.filter { !it.isCompleted }
  val completionRate = if (total > 0) completed.toFloat() / total else 0f
  val remainingPercent = ((1f - completionRate) * 100).toInt()

  // Pulsing animation for phone call avatar
  val infiniteTransition = rememberInfiniteTransition(label = "pulse")
  val pulseScale by infiniteTransition.animateFloat(
    initialValue = 1f,
    targetValue = 1.18f,
    animationSpec = infiniteRepeatable(
      animation = tween(900, easing = FastOutSlowInEasing),
      repeatMode = RepeatMode.Reverse
    ),
    label = "pulseScale"
  )

  Surface(
    modifier = Modifier.fillMaxSize(),
    color = Color(0xFF0F172A) // Sleek dark slate phone screen background
  ) {
    Column(
      modifier = Modifier
        .fillMaxSize()
        .padding(horizontal = 24.dp, vertical = 32.dp),
      horizontalAlignment = Alignment.CenterHorizontally,
      verticalArrangement = Arrangement.SpaceBetween
    ) {
      // Top: Caller Info & Ringing state
      Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.padding(top = 16.dp)
      ) {
        // Pulsing phone icon avatar
        Box(contentAlignment = Alignment.Center) {
          Box(
            modifier = Modifier
              .size(96.dp)
              .scale(pulseScale)
              .clip(CircleShape)
              .background(BluePrimary.copy(alpha = 0.25f))
          )
          Box(
            modifier = Modifier
              .size(76.dp)
              .clip(CircleShape)
              .background(
                Brush.linearGradient(
                  listOf(Color(0xFF2563EB), Color(0xFF1D4ED8))
                )
              ),
            contentAlignment = Alignment.Center
          ) {
            Icon(
              imageVector = Icons.Default.School,
              contentDescription = "Caller",
              tint = Color.White,
              modifier = Modifier.size(40.dp)
            )
          }
        }

        Spacer(modifier = Modifier.height(14.dp))

        Text(
          text = "Study Supervisor",
          style = MaterialTheme.typography.headlineMedium.copy(
            fontWeight = FontWeight.ExtraBold,
            color = Color.White
          )
        )

        Spacer(modifier = Modifier.height(4.dp))

        Text(
          text = "📞 INCOMING STUDY CALL • RINGING...",
          style = MaterialTheme.typography.labelMedium.copy(
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.sp,
            color = Color(0xFF60A5FA)
          )
        )
      }

      // Middle: Work Progress & Pending Tasks
      Surface(
        modifier = Modifier
          .fillMaxWidth()
          .weight(1f)
          .padding(vertical = 16.dp),
        shape = RoundedCornerShape(20.dp),
        color = Color(0xFF1E293B),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF334155))
      ) {
        Column(
          modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
        ) {
          // Progress Header
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Column {
              Text(
                text = "${(completionRate * 100).toInt()}% Done",
                style = MaterialTheme.typography.titleLarge.copy(
                  fontWeight = FontWeight.Bold,
                  color = Color.White
                )
              )
              Text(
                text = "$remainingPercent% work remaining to finish",
                style = MaterialTheme.typography.bodySmall,
                color = if (remainingPercent > 0) Color(0xFFF87171) else Color(0xFF4ADE80)
              )
            }

            Box(
              modifier = Modifier
                .clip(RoundedCornerShape(8.dp))
                .background(Color(0xFF0F172A))
                .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
              Text(
                text = "$completed/$total Tasks Done",
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                color = Color(0xFF93C5FD)
              )
            }
          }

          Spacer(modifier = Modifier.height(10.dp))

          LinearProgressIndicator(
            progress = { completionRate },
            modifier = Modifier
              .fillMaxWidth()
              .height(8.dp)
              .clip(RoundedCornerShape(4.dp)),
            color = Color(0xFF3B82F6),
            trackColor = Color(0xFF0F172A)
          )

          Spacer(modifier = Modifier.height(16.dp))

          Text(
            text = "PENDING WORKS FOR TODAY:",
            style = MaterialTheme.typography.labelSmall.copy(
              fontWeight = FontWeight.Bold,
              letterSpacing = 0.5.sp,
              color = Slate400
            )
          )

          Spacer(modifier = Modifier.height(8.dp))

          if (pending.isEmpty()) {
            Box(
              modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
              contentAlignment = Alignment.Center
            ) {
              Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(
                  imageVector = Icons.Default.CheckCircle,
                  contentDescription = null,
                  tint = Color(0xFF22C55E),
                  modifier = Modifier.size(48.dp)
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                  text = "🎉 All Works Completed!",
                  style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                  )
                )
                Text(
                  text = "Great discipline! You have finished everything for today.",
                  style = MaterialTheme.typography.bodySmall,
                  color = Slate400,
                  textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
              }
            }
          } else {
            LazyColumn(
              modifier = Modifier.weight(1f),
              verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
              items(pending, key = { it.id }) { task ->
                Surface(
                  modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .clickable { onToggleTask(task) }
                    .border(1.dp, Color(0xFF3B82F6).copy(alpha = 0.4f), RoundedCornerShape(12.dp)),
                  color = Color(0xFF0F172A)
                ) {
                  Row(
                    modifier = Modifier
                      .fillMaxWidth()
                      .padding(horizontal = 12.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                  ) {
                    // Checkbox
                    Box(
                      modifier = Modifier
                        .size(22.dp)
                        .clip(CircleShape)
                        .border(1.5.dp, Color(0xFF60A5FA), CircleShape),
                      contentAlignment = Alignment.Center
                    ) {
                      // Empty check indicator
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column(modifier = Modifier.weight(1f)) {
                      Text(
                        text = task.title,
                        style = MaterialTheme.typography.bodyMedium.copy(
                          fontWeight = FontWeight.SemiBold,
                          color = Color.White
                        )
                      )
                      Text(
                        text = "${task.subject} • Tap to mark done",
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                        color = Color(0xFF93C5FD)
                      )
                    }
                  }
                }
              }
            }
          }
        }
      }

      // Bottom: Answer & Dismiss Call Action Buttons
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .padding(bottom = 12.dp),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically
      ) {
        // Red Decline / Dismiss Button
        Column(
          horizontalAlignment = Alignment.CenterHorizontally,
          modifier = Modifier.clickable(onClick = onDismissCall)
        ) {
          Box(
            modifier = Modifier
              .size(68.dp)
              .clip(CircleShape)
              .background(Color(0xFFDC2626)),
            contentAlignment = Alignment.Center
          ) {
            Icon(
              imageVector = Icons.Default.CallEnd,
              contentDescription = "Dismiss Call",
              tint = Color.White,
              modifier = Modifier.size(32.dp)
            )
          }
          Spacer(modifier = Modifier.height(8.dp))
          Text(
            text = "Dismiss",
            style = MaterialTheme.typography.labelMedium.copy(
              fontWeight = FontWeight.Bold,
              color = Color.White
            )
          )
        }

        // Green Accept & Study Button
        Column(
          horizontalAlignment = Alignment.CenterHorizontally,
          modifier = Modifier.clickable(onClick = onAcceptCall)
        ) {
          Box(
            modifier = Modifier
              .size(68.dp)
              .clip(CircleShape)
              .background(Color(0xFF16A34A)),
            contentAlignment = Alignment.Center
          ) {
            Icon(
              imageVector = Icons.Default.Call,
              contentDescription = "Accept Call",
              tint = Color.White,
              modifier = Modifier.size(32.dp)
            )
          }
          Spacer(modifier = Modifier.height(8.dp))
          Text(
            text = "Study Now",
            style = MaterialTheme.typography.labelMedium.copy(
              fontWeight = FontWeight.Bold,
              color = Color.White
            )
          )
        }
      }
    }
  }
}
