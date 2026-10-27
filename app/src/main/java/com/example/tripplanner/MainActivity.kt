package com.example.tripplanner

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.tripplanner.ui.TripPlannerNavHost
import com.example.tripplanner.ui.theme.TripPlannerTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            TripPlannerTheme {
                TripPlannerNavHost()
            }
        }
    }
}