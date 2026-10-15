package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.example.feetrack.ui.FeeTrackApp
import com.example.feetrack.ui.FeeTrackViewModel
import com.example.ui.theme.FeeTrackTheme

class MainActivity : ComponentActivity() {
  private val viewModel: FeeTrackViewModel by viewModels()

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()
    setContent {
      FeeTrackTheme {
        Surface(modifier = Modifier.fillMaxSize()) {
          FeeTrackApp(viewModel = viewModel)
        }
      }
    }
  }
}
