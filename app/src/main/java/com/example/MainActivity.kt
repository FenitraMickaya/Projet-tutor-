package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import com.example.ui.StaffFlowApp
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.viewmodel.StaffViewModel

class MainActivity : ComponentActivity() {
  private val viewModel: StaffViewModel by viewModels()

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()
    setContent {
      val themeState by viewModel.appTheme.collectAsState()
      val isDarkTheme = when (themeState) {
        "LIGHT" -> false
        "DARK" -> true
        else -> isSystemInDarkTheme()
      }
      MyApplicationTheme(darkTheme = isDarkTheme) {
        Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
          StaffFlowApp(
            viewModel = viewModel,
            modifier = Modifier.padding(innerPadding)
          )
        }
      }
    }
  }
}
