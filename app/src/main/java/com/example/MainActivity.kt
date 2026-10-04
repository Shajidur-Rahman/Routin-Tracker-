package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.BarChart
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.screens.AnalysisScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.theme.BluePrimary
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.viewmodel.MainViewModel
import com.example.util.BengaliDateHelper

enum class AppScreen(val title: String) {
  HOME("Routines"),
  ANALYSIS("Analysis"),
  SETTINGS("Settings")
}

class MainActivity : ComponentActivity() {

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()

    val targetDateIso = intent?.getStringExtra("SELECTED_DATE_ISO")

    setContent {
      MyApplicationTheme {
        val viewModel: MainViewModel = viewModel()
        var currentScreen by remember { mutableStateOf(AppScreen.HOME) }

        LaunchedEffect(targetDateIso) {
          if (!targetDateIso.isNullOrBlank()) {
            val date = BengaliDateHelper.fromIsoString(targetDateIso)
            viewModel.selectDate(date)
            currentScreen = AppScreen.HOME
          }
        }

        // Handle system back navigation when on secondary screens
        if (currentScreen != AppScreen.HOME) {
          BackHandler {
            currentScreen = AppScreen.HOME
          }
        }

        Scaffold(
          modifier = Modifier.fillMaxSize(),
          contentWindowInsets = WindowInsets.statusBars,
          bottomBar = {
            NavigationBar(
              modifier = Modifier.navigationBarsPadding(),
              containerColor = MaterialTheme.colorScheme.surface,
              tonalElevation = 8.dp
            ) {
              NavigationBarItem(
                selected = currentScreen == AppScreen.HOME,
                onClick = { currentScreen = AppScreen.HOME },
                icon = {
                  Icon(
                    imageVector = if (currentScreen == AppScreen.HOME) Icons.Filled.CalendarMonth else Icons.Outlined.CalendarMonth,
                    contentDescription = "Routines & Tasks"
                  )
                },
                label = { Text("Routines") },
                colors = NavigationBarItemDefaults.colors(
                  selectedIconColor = BluePrimary,
                  selectedTextColor = BluePrimary,
                  indicatorColor = BluePrimary.copy(alpha = 0.15f)
                ),
                modifier = Modifier.testTag("nav_routines")
              )

              NavigationBarItem(
                selected = currentScreen == AppScreen.ANALYSIS,
                onClick = {
                  viewModel.refreshAnalysis()
                  currentScreen = AppScreen.ANALYSIS
                },
                icon = {
                  Icon(
                    imageVector = if (currentScreen == AppScreen.ANALYSIS) Icons.Filled.BarChart else Icons.Outlined.BarChart,
                    contentDescription = "Analysis"
                  )
                },
                label = { Text("Analysis") },
                colors = NavigationBarItemDefaults.colors(
                  selectedIconColor = BluePrimary,
                  selectedTextColor = BluePrimary,
                  indicatorColor = BluePrimary.copy(alpha = 0.15f)
                ),
                modifier = Modifier.testTag("nav_analysis")
              )

              NavigationBarItem(
                selected = currentScreen == AppScreen.SETTINGS,
                onClick = { currentScreen = AppScreen.SETTINGS },
                icon = {
                  Icon(
                    imageVector = if (currentScreen == AppScreen.SETTINGS) Icons.Filled.Settings else Icons.Outlined.Settings,
                    contentDescription = "Settings"
                  )
                },
                label = { Text("Settings") },
                colors = NavigationBarItemDefaults.colors(
                  selectedIconColor = BluePrimary,
                  selectedTextColor = BluePrimary,
                  indicatorColor = BluePrimary.copy(alpha = 0.15f)
                ),
                modifier = Modifier.testTag("nav_settings")
              )
            }
          }
        ) { paddingValues ->
          Box(
            modifier = Modifier
              .fillMaxSize()
              .padding(paddingValues)
          ) {
            Crossfade(
              targetState = currentScreen,
              label = "ScreenTransition"
            ) { screen ->
              when (screen) {
                AppScreen.HOME -> HomeScreen(viewModel = viewModel)
                AppScreen.ANALYSIS -> AnalysisScreen(viewModel = viewModel)
                AppScreen.SETTINGS -> SettingsScreen(viewModel = viewModel)
              }
            }
          }
        }
      }
    }
  }
}
