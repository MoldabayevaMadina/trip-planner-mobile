package com.example.tripplanner

import android.app.Application
import com.example.tripplanner.data.RoomTripRepository
import com.example.tripplanner.data.TripRepository
import com.example.tripplanner.data.local.TripDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class TripPlannerApplication : Application() {

    /** Lives as long as the app; used for work that doesn't belong to one screen. */
    private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    val repository: TripRepository by lazy {
        RoomTripRepository(TripDatabase.getInstance(this))
    }

    override fun onCreate() {
        super.onCreate()

        val prefs = getSharedPreferences("app", MODE_PRIVATE)
        if (!prefs.getBoolean("sample_added", false)) {
            applicationScope.launch {
                repository.addSampleData()
                prefs.edit().putBoolean("sample_added", true).apply()
            }
        }
    }
}