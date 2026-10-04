package com.example.ui.screens

import android.Manifest
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.PhoneInTalk
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.RingVolume
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.TableChart
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.BluePrimary
import com.example.ui.theme.Slate400
import com.example.ui.theme.Slate500
import com.example.ui.viewmodel.MainViewModel
import com.example.util.StudyCallIntensity
import com.example.util.StudyCallManager
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun SettingsScreen(
  viewModel: MainViewModel,
  modifier: Modifier = Modifier
) {
  val context = LocalContext.current
  val scope = rememberCoroutineScope()
  val isSyncing by viewModel.isSyncing.collectAsState()
  val isWriteConfigured by viewModel.isSheetWriteConfigured.collectAsState()

  var spreadsheetId by remember { mutableStateOf(viewModel.getSpreadsheetId()) }
  var webhookUrl by remember { mutableStateOf(viewModel.getWebhookUrl()) }
  var oauthToken by remember { mutableStateOf(viewModel.getOAuthToken()) }
  var isTestingWrite by remember { mutableStateOf(false) }

  // Study Call state
  var isStudyCallEnabled by remember { mutableStateOf(StudyCallManager.isEnabled(context)) }
  var studyIntensity by remember { mutableStateOf(StudyCallManager.getIntensity(context)) }
  var testCallCountdown by remember { mutableIntStateOf(0) }

  val permissionLauncher = rememberLauncherForActivityResult(
    contract = ActivityResultContracts.RequestPermission(),
    onResult = { isGranted ->
      val msg = if (isGranted) "Notification permission granted" else "Notifications disabled"
      Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
    }
  )

  val appsScriptTemplate = """
function doPost(e) {
  try {
    var data = JSON.parse(e.postData.contents);
    var sheet = SpreadsheetApp.getActiveSpreadsheet().getActiveSheet();
    // Appends new completion or task row
    sheet.appendRow([
      new Date(),
      data.date || "",
      data.taskTitle || "Task",
      data.isCompleted ? "COMPLETED" : "PENDING",
      data.taskId || ""
    ]);
    return ContentService.createTextOutput(JSON.stringify({status: "ok", message: "Saved in sheet"}))
      .setMimeType(ContentService.MimeType.JSON);
  } catch(err) {
    return ContentService.createTextOutput(JSON.stringify({error: err.toString()}))
      .setMimeType(ContentService.MimeType.JSON);
  }
}

function doGet(e) {
  return ContentService.createTextOutput("RoutineTrack Sync Endpoint Active")
    .setMimeType(ContentService.MimeType.TEXT);
}
""".trimIndent()

  LazyColumn(
    modifier = modifier
      .fillMaxSize()
      .background(MaterialTheme.colorScheme.background)
      .padding(horizontal = 20.dp),
    contentPadding = PaddingValues(top = 16.dp, bottom = 80.dp),
    verticalArrangement = Arrangement.spacedBy(16.dp)
  ) {
    item {
      Column {
        Text(
          text = "Settings & Study Call",
          style = MaterialTheme.typography.headlineMedium.copy(
            fontWeight = FontWeight.ExtraBold,
            letterSpacing = (-0.5).sp
          ),
          color = MaterialTheme.colorScheme.onBackground
        )
        Text(
          text = "Configure Study Calls, Google Sheet sync, and routine reminders",
          style = MaterialTheme.typography.bodyMedium,
          color = Slate500
        )
      }
    }

    // ================= STUDY CALL SECTION =================
    item {
      Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        color = if (isStudyCallEnabled) Color(0xFFEFF6FF) else MaterialTheme.colorScheme.surface,
        border = androidx.compose.foundation.BorderStroke(
          1.5.dp,
          if (isStudyCallEnabled) BluePrimary else MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
        )
      ) {
        Column(modifier = Modifier.padding(16.dp)) {
          // Header with Icon & On/Off Switch
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Row(
              verticalAlignment = Alignment.CenterVertically,
              modifier = Modifier.weight(1f)
            ) {
              Box(
                modifier = Modifier
                  .size(40.dp)
                  .clip(CircleShape)
                  .background(if (isStudyCallEnabled) BluePrimary else MaterialTheme.colorScheme.surfaceVariant),
                contentAlignment = Alignment.Center
              ) {
                Icon(
                  imageVector = Icons.Default.PhoneInTalk,
                  contentDescription = null,
                  tint = if (isStudyCallEnabled) Color.White else Slate500,
                  modifier = Modifier.size(22.dp)
                )
              }
              Spacer(modifier = Modifier.width(12.dp))
              Column {
                Text(
                  text = "Study Call",
                  style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                  color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                  text = if (isStudyCallEnabled) "Active • Incoming call alarms enabled" else "Turned Off",
                  style = MaterialTheme.typography.labelSmall,
                  color = if (isStudyCallEnabled) BluePrimary else Slate500
                )
              }
            }

            Switch(
              checked = isStudyCallEnabled,
              onCheckedChange = { enabled ->
                isStudyCallEnabled = enabled
                StudyCallManager.setEnabled(context, enabled)
                val msg = if (enabled) "Study Call activated! Phone will ring with study tasks." else "Study Call turned off"
                Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
              },
              modifier = Modifier.testTag("toggle_study_call")
            )
          }

          Spacer(modifier = Modifier.height(12.dp))

          Text(
            text = "Wake up your phone like a real incoming call. Even if your screen is off or locked, a call will pop up, ring, and show remaining % work and pending tasks with 1-tap completion.",
            style = MaterialTheme.typography.bodySmall,
            color = Slate500
          )

          // Permission for Display Over Other Apps so calling screen pops up everywhere
          val canDrawOverApps = remember(isStudyCallEnabled) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
              android.provider.Settings.canDrawOverlays(context)
            } else {
              true
            }
          }

          if (isStudyCallEnabled && !canDrawOverApps) {
            Spacer(modifier = Modifier.height(10.dp))
            Surface(
              shape = RoundedCornerShape(10.dp),
              color = Color(0xFFFEF3C7),
              border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFF59E0B)),
              modifier = Modifier.fillMaxWidth()
            ) {
              Column(modifier = Modifier.padding(10.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                  Icon(
                    imageVector = Icons.Default.Warning,
                    contentDescription = null,
                    tint = Color(0xFFD97706),
                    modifier = Modifier.size(16.dp)
                  )
                  Spacer(modifier = Modifier.width(6.dp))
                  Text(
                    text = "Permission Needed for Calling Screen",
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                    color = Color(0xFF92400E)
                  )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                  text = "To allow the full calling screen to pop up over the lock screen and other apps (instead of just a notification), enable 'Display Over Other Apps'.",
                  style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                  color = Color(0xFF78350F)
                )
                Spacer(modifier = Modifier.height(8.dp))
                Button(
                  onClick = {
                    try {
                      val intent = Intent(
                        android.provider.Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                        Uri.parse("package:${context.packageName}")
                      )
                      context.startActivity(intent)
                    } catch (e: Exception) {
                      try {
                        context.startActivity(Intent(android.provider.Settings.ACTION_MANAGE_OVERLAY_PERMISSION))
                      } catch (e2: Exception) {
                        Toast.makeText(context, "Open Settings > Apps > RoutineTrack > Display over other apps", Toast.LENGTH_LONG).show()
                      }
                    }
                  },
                  colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD97706)),
                  shape = RoundedCornerShape(8.dp),
                  contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                ) {
                  Text("Enable Pop-up Everywhere", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
              }
            }
          }

          Spacer(modifier = Modifier.height(14.dp))

          // Intensity Selector: High, Middle, Low
          Text(
            text = "Call Frequency / Intensity:",
            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.onSurface
          )
          Spacer(modifier = Modifier.height(8.dp))

          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
          ) {
            val intensities = listOf(
              StudyCallIntensity.HIGH,
              StudyCallIntensity.MIDDLE,
              StudyCallIntensity.LOW
            )

            intensities.forEach { intensity ->
              val isSelected = studyIntensity == intensity
              Surface(
                modifier = Modifier
                  .weight(1f)
                  .clip(RoundedCornerShape(10.dp))
                  .clickable {
                    studyIntensity = intensity
                    StudyCallManager.setIntensity(context, intensity)
                    Toast.makeText(context, "Set to ${intensity.title}", Toast.LENGTH_SHORT).show()
                  }
                  .border(
                    width = if (isSelected) 2.dp else 1.dp,
                    color = if (isSelected) BluePrimary else MaterialTheme.colorScheme.outline.copy(alpha = 0.4f),
                    shape = RoundedCornerShape(10.dp)
                  ),
                color = if (isSelected) BluePrimary.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surfaceVariant
              ) {
                Column(
                  modifier = Modifier.padding(vertical = 10.dp, horizontal = 6.dp),
                  horizontalAlignment = Alignment.CenterHorizontally
                ) {
                  Text(
                    text = when (intensity) {
                      StudyCallIntensity.HIGH -> "High"
                      StudyCallIntensity.MIDDLE -> "Middle"
                      StudyCallIntensity.LOW -> "Low"
                    },
                    style = MaterialTheme.typography.labelMedium.copy(
                      fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                    ),
                    color = if (isSelected) BluePrimary else MaterialTheme.colorScheme.onSurface
                  )
                  Text(
                    text = "${intensity.callsPerDay}x/day",
                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                    color = if (isSelected) BluePrimary else Slate400
                  )
                }
              }
            }
          }

          Spacer(modifier = Modifier.height(10.dp))

          // Timing Description Box (Strictly 9:00 AM - 11:59 PM)
          Surface(
            shape = RoundedCornerShape(8.dp),
            color = MaterialTheme.colorScheme.surface,
            modifier = Modifier.fillMaxWidth()
          ) {
            Column(modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp)) {
              Text(
                text = "☀️ Active Window: 09:00 AM – 11:59 PM",
                style = MaterialTheme.typography.labelSmall.copy(
                  fontWeight = FontWeight.Bold,
                  color = BluePrimary
                )
              )
              Spacer(modifier = Modifier.height(2.dp))
              Text(
                text = when (studyIntensity) {
                  StudyCallIntensity.HIGH -> "10 calls daily: 09:00 AM, 10:30 AM, 12:00 PM, 01:30 PM, 03:00 PM, 04:30 PM, 06:00 PM, 07:30 PM, 09:00 PM, 10:30 PM"
                  StudyCallIntensity.MIDDLE -> "5 calls daily: 09:00 AM, 12:00 PM, 03:00 PM, 06:00 PM, 09:00 PM"
                  StudyCallIntensity.LOW -> "3 calls daily: 10:00 AM, 04:00 PM, 09:30 PM"
                },
                style = MaterialTheme.typography.labelSmall.copy(
                  fontSize = 11.sp,
                  color = Slate500
                )
              )
              Text(
                text = "No calls will ring during resting hours (12:00 AM – 08:59 AM).",
                style = MaterialTheme.typography.labelSmall.copy(
                  fontSize = 10.sp,
                  color = Slate400
                )
              )
            }
          }

          Spacer(modifier = Modifier.height(14.dp))

          // Test Button with strict 5-second delay
          Button(
            onClick = {
              if (testCallCountdown == 0) {
                scope.launch {
                  testCallCountdown = 5
                  StudyCallManager.triggerTestCall(context, delaySeconds = 5)
                  Toast.makeText(
                    context,
                    "📞 Study Call scheduled in 5 seconds. Lock screen or wait 5s to test!",
                    Toast.LENGTH_SHORT
                  ).show()
                  while (testCallCountdown > 0) {
                    delay(1000)
                    testCallCountdown--
                  }
                }
              }
            },
            colors = ButtonDefaults.buttonColors(
              containerColor = if (testCallCountdown > 0) Color(0xFFF97316) else Color(0xFF16A34A)
            ),
            shape = RoundedCornerShape(10.dp),
            modifier = Modifier
              .fillMaxWidth()
              .testTag("btn_test_study_call")
          ) {
            if (testCallCountdown > 0) {
              CircularProgressIndicator(
                modifier = Modifier.size(16.dp),
                color = Color.White,
                strokeWidth = 2.dp
              )
              Spacer(modifier = Modifier.width(8.dp))
              Text("Starting call in $testCallCountdown seconds...", fontWeight = FontWeight.Bold)
            } else {
              Icon(Icons.Default.RingVolume, contentDescription = null, modifier = Modifier.size(18.dp))
              Spacer(modifier = Modifier.width(8.dp))
              Text("Test Study Call (5s Delay)", fontWeight = FontWeight.Bold)
            }
          }
        }
      }
    }

    // Live Sheet Write Status Card
    item {
      Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        color = if (isWriteConfigured) Color(0xFFECFDF5) else Color(0xFFEFF6FF),
        border = androidx.compose.foundation.BorderStroke(
          1.dp,
          if (isWriteConfigured) Color(0xFF10B981) else BluePrimary.copy(alpha = 0.4f)
        )
      ) {
        Column(modifier = Modifier.padding(16.dp)) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
              modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(if (isWriteConfigured) Color(0xFF10B981) else BluePrimary),
              contentAlignment = Alignment.Center
            ) {
              Icon(
                imageVector = if (isWriteConfigured) Icons.Default.CheckCircle else Icons.Default.Info,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(20.dp)
              )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column {
              Text(
                text = if (isWriteConfigured) "Live Google Sheet Write: ACTIVE" else "Google Sheet: READ-ONLY MODE",
                style = MaterialTheme.typography.titleMedium.copy(
                  fontWeight = FontWeight.Bold,
                  color = if (isWriteConfigured) Color(0xFF065F46) else Color(0xFF1E3A8A)
                )
              )
              Text(
                text = if (isWriteConfigured) {
                  "Tasks marked done and routines are saved directly to your sheet file."
                } else {
                  "App can read your dates, but Google requires Webhook or OAuth to write data into your sheet."
                },
                style = MaterialTheme.typography.bodySmall,
                color = if (isWriteConfigured) Color(0xFF047857) else Slate500
              )
            }
          }

          Spacer(modifier = Modifier.height(12.dp))

          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
          ) {
            // Open sheet in browser
            OutlinedButton(
              onClick = {
                try {
                  val intent = Intent(
                    Intent.ACTION_VIEW,
                    Uri.parse("https://docs.google.com/spreadsheets/d/$spreadsheetId/edit")
                  )
                  context.startActivity(intent)
                } catch (e: Exception) {
                  Toast.makeText(context, "Could not open browser", Toast.LENGTH_SHORT).show()
                }
              },
              shape = RoundedCornerShape(10.dp),
              modifier = Modifier.weight(1f)
            ) {
              Icon(Icons.Default.OpenInNew, contentDescription = null, modifier = Modifier.size(16.dp))
              Spacer(modifier = Modifier.width(6.dp))
              Text("Open Sheet")
            }

            // Test write button
            Button(
              onClick = {
                isTestingWrite = true
                viewModel.testSheetWrite { success, msg ->
                  isTestingWrite = false
                  val toastMsg = if (success) "✓ Verified! Saved in your sheet file!" else "Failed: $msg"
                  Toast.makeText(context, toastMsg, Toast.LENGTH_LONG).show()
                }
              },
              colors = ButtonDefaults.buttonColors(
                containerColor = if (isWriteConfigured) Color(0xFF10B981) else BluePrimary
              ),
              shape = RoundedCornerShape(10.dp),
              modifier = Modifier.weight(1f)
            ) {
              if (isTestingWrite) {
                CircularProgressIndicator(modifier = Modifier.size(16.dp), color = Color.White, strokeWidth = 2.dp)
              } else {
                Icon(Icons.Default.Send, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Test Write Row")
              }
            }
          }
        }
      }
    }

    // Google Spreadsheet ID Card
    item {
      Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        color = MaterialTheme.colorScheme.surface,
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f))
      ) {
        Column(modifier = Modifier.padding(16.dp)) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
              modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(Color(0xFF10B981).copy(alpha = 0.15f)),
              contentAlignment = Alignment.Center
            ) {
              Icon(
                imageVector = Icons.Default.TableChart,
                contentDescription = null,
                tint = Color(0xFF10B981),
                modifier = Modifier.size(20.dp)
              )
            }
            Spacer(modifier = Modifier.width(10.dp))
            Column {
              Text(
                text = "Target Google Spreadsheet",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
              )
              Text(
                text = "ID: $spreadsheetId",
                style = MaterialTheme.typography.labelSmall,
                color = Slate500
              )
            }
          }

          Spacer(modifier = Modifier.height(14.dp))

          OutlinedTextField(
            value = spreadsheetId,
            onValueChange = { spreadsheetId = it },
            label = { Text("Google Spreadsheet ID") },
            modifier = Modifier
              .fillMaxWidth()
              .testTag("input_spreadsheet_id"),
            singleLine = true,
            shape = RoundedCornerShape(12.dp)
          )

          Spacer(modifier = Modifier.height(12.dp))

          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
          ) {
            Button(
              onClick = {
                viewModel.setSpreadsheetId(spreadsheetId)
                Toast.makeText(context, "Spreadsheet ID saved!", Toast.LENGTH_SHORT).show()
              },
              colors = ButtonDefaults.buttonColors(containerColor = BluePrimary),
              shape = RoundedCornerShape(10.dp),
              modifier = Modifier.weight(1f)
            ) {
              Icon(Icons.Default.Save, contentDescription = null, modifier = Modifier.size(16.dp))
              Spacer(modifier = Modifier.width(6.dp))
              Text("Save ID")
            }

            OutlinedButton(
              onClick = { viewModel.manualSyncWithSheet() },
              shape = RoundedCornerShape(10.dp),
              modifier = Modifier.weight(1f)
            ) {
              if (isSyncing) {
                CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
              } else {
                Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Fetch Dates Now")
              }
            }
          }
        }
      }
    }

    // Option 1: 1-Minute Webhook Setup (Recommended)
    item {
      Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        color = MaterialTheme.colorScheme.surface,
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f))
      ) {
        Column(modifier = Modifier.padding(16.dp)) {
          Text(
            text = "Option 1: Google Apps Script Webhook (Recommended)",
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.onSurface
          )
          Spacer(modifier = Modifier.height(4.dp))
          Text(
            text = "Follow these 4 simple steps to allow the app to save directly into your sheet file:",
            style = MaterialTheme.typography.bodySmall,
            color = Slate500
          )

          Spacer(modifier = Modifier.height(10.dp))

          Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(
              text = "1. Open your sheet: docs.google.com/spreadsheets/d/$spreadsheetId",
              style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium)
            )
            Text(
              text = "2. In the top menu, click Extensions > Apps Script",
              style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium)
            )
            Text(
              text = "3. Paste the script below (tap 'Copy Script')",
              style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium)
            )
            Text(
              text = "4. Click Deploy > New deployment > Web app:\n   • Execute as: Me\n   • Who has access: Anyone\n   Copy the Web app URL and paste it below.",
              style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium)
            )
          }

          Spacer(modifier = Modifier.height(12.dp))

          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Text(
              text = "Apps Script Code",
              style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
            )
            Button(
              onClick = {
                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                val clip = ClipData.newPlainText("AppsScript", appsScriptTemplate)
                clipboard.setPrimaryClip(clip)
                Toast.makeText(context, "Script copied to clipboard!", Toast.LENGTH_SHORT).show()
              },
              colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
              contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
              shape = RoundedCornerShape(8.dp)
            ) {
              Icon(
                imageVector = Icons.Default.ContentCopy,
                contentDescription = null,
                tint = BluePrimary,
                modifier = Modifier.size(14.dp)
              )
              Spacer(modifier = Modifier.width(4.dp))
              Text("Copy Script", color = BluePrimary, fontSize = 12.sp)
            }
          }

          Spacer(modifier = Modifier.height(8.dp))

          Surface(
            shape = RoundedCornerShape(10.dp),
            color = MaterialTheme.colorScheme.surfaceVariant,
            modifier = Modifier.fillMaxWidth()
          ) {
            Text(
              text = appsScriptTemplate,
              style = MaterialTheme.typography.bodySmall.copy(
                fontFamily = FontFamily.Monospace,
                fontSize = 11.sp
              ),
              color = MaterialTheme.colorScheme.onSurfaceVariant,
              modifier = Modifier.padding(10.dp)
            )
          }

          Spacer(modifier = Modifier.height(14.dp))

          OutlinedTextField(
            value = webhookUrl,
            onValueChange = { webhookUrl = it },
            label = { Text("Paste Web App URL here") },
            placeholder = { Text("https://script.google.com/macros/s/.../exec") },
            modifier = Modifier
              .fillMaxWidth()
              .testTag("input_webhook_url"),
            shape = RoundedCornerShape(12.dp)
          )

          Spacer(modifier = Modifier.height(10.dp))

          Button(
            onClick = {
              viewModel.setWebhookUrl(webhookUrl)
              Toast.makeText(context, "Webhook URL saved! Tap 'Test Write Row' to verify.", Toast.LENGTH_SHORT).show()
            },
            colors = ButtonDefaults.buttonColors(containerColor = BluePrimary),
            shape = RoundedCornerShape(10.dp),
            modifier = Modifier.fillMaxWidth()
          ) {
            Icon(Icons.Default.Save, contentDescription = null, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text("Save Webhook URL")
          }
        }
      }
    }

    // Option 2: Direct Google OAuth Token
    item {
      Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        color = MaterialTheme.colorScheme.surface,
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f))
      ) {
        Column(modifier = Modifier.padding(16.dp)) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.Key, contentDescription = null, tint = BluePrimary, modifier = Modifier.size(20.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text(
              text = "Option 2: Google OAuth Token (Sheets API v4)",
              style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
            )
          }
          Spacer(modifier = Modifier.height(6.dp))
          Text(
            text = "OAuth client initialized for gen-lang-client-0904412781. If you have an OAuth Access Token, enter it below to append rows via Sheets REST API:",
            style = MaterialTheme.typography.bodySmall,
            color = Slate500
          )

          Spacer(modifier = Modifier.height(12.dp))

          OutlinedTextField(
            value = oauthToken,
            onValueChange = { oauthToken = it },
            label = { Text("OAuth Bearer Access Token") },
            placeholder = { Text("ya29.a0...") },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
          )

          Spacer(modifier = Modifier.height(10.dp))

          Button(
            onClick = {
              viewModel.setOAuthToken(oauthToken)
              Toast.makeText(context, "OAuth Token saved!", Toast.LENGTH_SHORT).show()
            },
            colors = ButtonDefaults.buttonColors(containerColor = BluePrimary),
            shape = RoundedCornerShape(10.dp),
            modifier = Modifier.fillMaxWidth()
          ) {
            Icon(Icons.Default.Save, contentDescription = null, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text("Save OAuth Token")
          }
        }
      }
    }

    // Reminders & Notifications Permission
    item {
      Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        color = MaterialTheme.colorScheme.surface,
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f))
      ) {
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Row(
            modifier = Modifier.weight(1f),
            verticalAlignment = Alignment.CenterVertically
          ) {
            Box(
              modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(BluePrimary.copy(alpha = 0.12f)),
              contentAlignment = Alignment.Center
            ) {
              Icon(
                imageVector = Icons.Default.Notifications,
                contentDescription = null,
                tint = BluePrimary,
                modifier = Modifier.size(20.dp)
              )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column {
              Text(
                text = "Task Reminders",
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold)
              )
              Text(
                text = "Alarms for scheduled routines",
                style = MaterialTheme.typography.labelSmall,
                color = Slate500
              )
            }
          }

          if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            Button(
              onClick = { permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS) },
              colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary),
              shape = RoundedCornerShape(10.dp)
            ) {
              Text("Allow")
            }
          }
        }
      }
    }
  }
}
