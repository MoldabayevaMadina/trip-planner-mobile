package com.example.tripplanner

import android.app.Application
import com.example.tripplanner.data.InMemoryTripRepository
import com.example.tripplanner.data.TripRepository

class TripPlannerApplication : Application() {
    val repository: TripRepository by lazy { InMemoryTripRepository() }
}