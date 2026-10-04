package com.example.data.remote

import android.content.Context
import android.util.Log
import com.example.util.BengaliDateHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.io.BufferedReader
import java.io.StringReader
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit

class GoogleSheetService(private val context: Context) {

  private val client = OkHttpClient.Builder()
    .connectTimeout(15, TimeUnit.SECONDS)
    .readTimeout(15, TimeUnit.SECONDS)
    .followRedirects(true)
    .build()

  companion object {
    const val DEFAULT_SPREADSHEET_ID = "1fDEbV_HayCxgYNMjC1lopt4KIWFVnUnBFbxeWaDdOE4"
    const val OAUTH_CLIENT_ID = "329852113277-h14p20evo26g29lj6cq509u6mbrkq4aa.apps.googleusercontent.com"
    private const val PREFS_NAME = "google_sheet_prefs"
    private const val KEY_SPREADSHEET_ID = "spreadsheet_id"
    private const val KEY_WEBHOOK_URL = "webhook_url"
    private const val KEY_OAUTH_TOKEN = "oauth_access_token"
    private const val KEY_AUTO_SYNC = "auto_sync"
    private const val TAG = "GoogleSheetService"
  }

  private val prefs by lazy {
    context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
  }

  var spreadsheetId: String
    get() = prefs.getString(KEY_SPREADSHEET_ID, DEFAULT_SPREADSHEET_ID) ?: DEFAULT_SPREADSHEET_ID
    set(value) = prefs.edit().putString(KEY_SPREADSHEET_ID, value.trim()).apply()

  var webhookUrl: String
    get() = prefs.getString(KEY_WEBHOOK_URL, "") ?: ""
    set(value) = prefs.edit().putString(KEY_WEBHOOK_URL, value.trim()).apply()

  var oauthAccessToken: String
    get() = prefs.getString(KEY_OAUTH_TOKEN, "") ?: ""
    set(value) = prefs.edit().putString(KEY_OAUTH_TOKEN, value.trim()).apply()

  var autoSync: Boolean
    get() = prefs.getBoolean(KEY_AUTO_SYNC, true)
    set(value) = prefs.edit().putBoolean(KEY_AUTO_SYNC, value).apply()

  val isWriteEnabled: Boolean
    get() = webhookUrl.isNotBlank() || oauthAccessToken.isNotBlank()

  suspend fun fetchSpreadsheetDatesAndTasks(): Result<List<ParsedSheetRow>> = withContext(Dispatchers.IO) {
    try {
      val url = "https://docs.google.com/spreadsheets/d/$spreadsheetId/export?format=csv"
      val request = Request.Builder()
        .url(url)
        .header("User-Agent", "Mozilla/5.0 RoutineTrack-Android")
        .build()

      val response = client.newCall(request).execute()
      if (!response.isSuccessful) {
        return@withContext Result.failure(Exception("Failed to fetch sheet: HTTP ${response.code}"))
      }

      val csvContent = response.body?.string() ?: ""
      val parsedRows = parseCsv(csvContent)
      Result.success(parsedRows)
    } catch (e: Exception) {
      Log.e(TAG, "Error fetching spreadsheet", e)
      Result.failure(e)
    }
  }

  private fun parseCsv(csv: String): List<ParsedSheetRow> {
    val results = mutableListOf<ParsedSheetRow>()
    val reader = BufferedReader(StringReader(csv))
    var line: String? = reader.readLine()
    var rowIndex = 0

    while (line != null) {
      val trimmed = line.trim()
      if (trimmed.isNotEmpty()) {
        val columns = parseCsvLine(trimmed)
        if (columns.isNotEmpty()) {
          val dateText = columns[0].trim()
          val parsedDate = BengaliDateHelper.parseSpreadsheetDate(dateText)
          val extraSubjects = if (columns.size > 1) {
            columns.subList(1, columns.size).map { it.trim() }.filter { it.isNotEmpty() }
          } else {
            emptyList()
          }

          results.add(
            ParsedSheetRow(
              rowIndex = rowIndex,
              rawDateText = dateText,
              date = parsedDate,
              subjects = extraSubjects
            )
          )
        }
      }
      rowIndex++
      line = reader.readLine()
    }
    return results
  }

  private fun parseCsvLine(line: String): List<String> {
    val tokens = mutableListOf<String>()
    val sb = StringBuilder()
    var inQuotes = false
    var i = 0
    while (i < line.length) {
      val c = line[i]
      if (c == '\"') {
        if (inQuotes && i + 1 < line.length && line[i + 1] == '\"') {
          sb.append('\"')
          i++
        } else {
          inQuotes = !inQuotes
        }
      } else if (c == ',' && !inQuotes) {
        tokens.add(sb.toString())
        sb.clear()
      } else {
        sb.append(c)
      }
      i++
    }
    tokens.add(sb.toString())
    return tokens
  }

  suspend fun sendTaskCompletionToSheet(
    taskId: Long,
    taskTitle: String,
    dateIso: String,
    isCompleted: Boolean,
    timestamp: Long
  ): SyncResult = withContext(Dispatchers.IO) {
    val targetUrl = webhookUrl.trim()
    val token = oauthAccessToken.trim()

    // 1. If Webhook URL is configured (Google Apps Script Web App)
    if (targetUrl.isNotEmpty()) {
      try {
        val json = JSONObject().apply {
          put("action", if (isCompleted) "complete_task" else "uncomplete_task")
          put("taskId", taskId)
          put("taskTitle", taskTitle)
          put("date", dateIso)
          put("isCompleted", isCompleted)
          put("timestamp", timestamp)
          put("spreadsheetId", spreadsheetId)
        }

        val mediaType = "application/json; charset=utf-8".toMediaType()
        val body = json.toString().toRequestBody(mediaType)
        val request = Request.Builder()
          .url(targetUrl)
          .post(body)
          .build()

        val response = client.newCall(request).execute()
        if (response.isSuccessful) {
          return@withContext SyncResult(
            isSuccess = true,
            message = "Saved in Google Sheet file!"
          )
        } else {
          return@withContext SyncResult(
            isSuccess = false,
            message = "Sheet Webhook error: HTTP ${response.code}"
          )
        }
      } catch (e: Exception) {
        Log.e(TAG, "Failed sending to webhook", e)
        return@withContext SyncResult(
          isSuccess = false,
          message = "Network error: ${e.localizedMessage ?: "Failed"}"
        )
      }
    }

    // 2. If OAuth Access Token is configured (Google Sheets API v4)
    if (token.isNotEmpty()) {
      try {
        val sheetsApiUrl = "https://sheets.googleapis.com/v4/spreadsheets/$spreadsheetId/values/A1:append?valueInputOption=USER_ENTERED"
        val timeFormatted = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US).format(Date(timestamp))
        val rowArray = JSONArray().apply {
          put(timeFormatted)
          put(dateIso)
          put(taskTitle)
          put(if (isCompleted) "COMPLETED" else "PENDING")
          put(taskId.toString())
        }
        val valuesArray = JSONArray().apply {
          put(rowArray)
        }
        val bodyJson = JSONObject().apply {
          put("values", valuesArray)
        }

        val mediaType = "application/json; charset=utf-8".toMediaType()
        val body = bodyJson.toString().toRequestBody(mediaType)
        val request = Request.Builder()
          .url(sheetsApiUrl)
          .header("Authorization", "Bearer $token")
          .post(body)
          .build()

        val response = client.newCall(request).execute()
        if (response.isSuccessful) {
          return@withContext SyncResult(
            isSuccess = true,
            message = "Saved in Google Sheet via API!"
          )
        } else {
          return@withContext SyncResult(
            isSuccess = false,
            message = "Sheets API error: HTTP ${response.code}"
          )
        }
      } catch (e: Exception) {
        Log.e(TAG, "Failed writing to Google Sheets API", e)
        return@withContext SyncResult(
          isSuccess = false,
          message = "API error: ${e.localizedMessage ?: "Failed"}"
        )
      }
    }

    // 3. Fallback: Saved locally, notify clearly that write integration is needed
    SyncResult(
      isSuccess = false,
      message = "Saved in app. To save directly inside your Google Sheet file, link Webhook or Google Sign-In in Settings."
    )
  }

  suspend fun testSheetWrite(): Result<String> = withContext(Dispatchers.IO) {
    val targetUrl = webhookUrl.trim()
    val token = oauthAccessToken.trim()

    if (targetUrl.isEmpty() && token.isEmpty()) {
      return@withContext Result.failure(Exception("Please enter your Webhook URL or Google OAuth Token first."))
    }

    val timestamp = System.currentTimeMillis()
    val result = sendTaskCompletionToSheet(
      taskId = 9999,
      taskTitle = "RoutineTrack Connection Test",
      dateIso = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date()),
      isCompleted = true,
      timestamp = timestamp
    )

    if (result.isSuccess) {
      Result.success(result.message)
    } else {
      Result.failure(Exception(result.message))
    }
  }
}

data class ParsedSheetRow(
  val rowIndex: Int,
  val rawDateText: String,
  val date: LocalDate?,
  val subjects: List<String>
)

data class SyncResult(
  val isSuccess: Boolean,
  val message: String
)
