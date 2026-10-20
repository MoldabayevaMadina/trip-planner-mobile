package com.example.tripplanner

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.tripplanner.ui.TripDetailScreen
import com.example.tripplanner.ui.TripsListScreen
import com.example.tripplanner.ui.TripsViewModel
import com.example.tripplanner.ui.theme.TripPlannerTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            TripPlannerTheme {
                TripPlannerApp()
            }
        }
    }
}


@Composable
fun TripPlannerApp(viewModel: TripsViewModel = viewModel()) {
    val state by viewModel.uiState.collectAsState()
    val trip = state.selectedTrip

    if (trip == null) {
        TripsListScreen(
            state = state,
            onQueryChange = viewModel::onQueryChange,
            onFilterChange = viewModel::onFilterChange,
            onTripClick = viewModel::openTrip,
            onAddTrip = viewModel::addTrip,
        )
    } else {
        BackHandler { viewModel.closeTrip() }
        TripDetailScreen(
            trip = trip,
            onBack = viewModel::closeTrip,
            onDeleteTrip = { viewModel.deleteTrip(trip.id) },
            onAddPlace = { name, category -> viewModel.addPlace(trip.id, name, category) },
            onTogglePlace = { placeId -> viewModel.togglePlaceVisited(trip.id, placeId) },
            onDeletePlace = { placeId -> viewModel.deletePlace(trip.id, placeId) },
            onRestorePlace = { place, index -> viewModel.restorePlace(trip.id, place, index) },
            onAddPacking = { name -> viewModel.addPackingItem(trip.id, name) },
            onTogglePacked = { itemId -> viewModel.togglePacked(trip.id, itemId) },
            onDeletePacking = { itemId -> viewModel.deletePackingItem(trip.id, itemId) },
            onRestorePacking = { item, index -> viewModel.restorePackingItem(trip.id, item, index) },
            onApplyTemplate = { template -> viewModel.applyTemplate(trip.id, template) },
        )
    }
}