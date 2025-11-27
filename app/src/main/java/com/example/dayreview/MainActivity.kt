package com.example.dayreview

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.example.dayreview.ui.TodayScreen
import com.example.dayreview.ui.theme.DayReviewTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Connect to Database via ViewModel
        val viewModel: DayReviewViewModel by viewModels { DayReviewViewModelFactory(application) }

        setContent {
            DayReviewTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    // Pass the live ViewModel to the screen
                    TodayScreen(viewModel = viewModel)
                }
            }
        }
    }
}
