package com.example.tripplanner.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.tripplanner.ui.components.EmptyState
import com.example.tripplanner.ui.components.TripCard

@Composable
fun TripsListRoute(
    onTripClick: (tripId: Long) -> Unit,
    onAddTrip: () -> Unit,
    viewModel: TripsListViewModel = viewModel(factory = TripsListViewModel.Factory),
) {

    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val query by viewModel.query.collectAsStateWithLifecycle()

    TripsListScreen(
        state = state,
        query = query,
        onQueryChange = viewModel::onQueryChange,
        onFilterChange = viewModel::onFilterChange,
        onTripClick = onTripClick,
        onAddTrip = onAddTrip,
    )
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TripsListScreen(
    state: TripsListUiState,
    query: String,
    onQueryChange: (String) -> Unit,
    onFilterChange: (TripFilter) -> Unit,
    onTripClick: (Long) -> Unit,
    onAddTrip: () -> Unit,
) {
    Scaffold(
        topBar = { TopAppBar(title = { Text("My trips") }) },
        floatingActionButton = {
            ExtendedFloatingActionButton(onClick = onAddTrip) { Text("+ New trip") }
        },
    ) { padding ->
        Column(modifier = Modifier.padding(padding).fillMaxSize()) {
            OutlinedTextField(
                value = query,
                onValueChange = onQueryChange,
                label = { Text("Search by city, country or title") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
            )
            Row(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                TripFilter.entries.forEach { filter ->
                    FilterChip(
                        selected = state.filter == filter,
                        onClick = { onFilterChange(filter) },
                        label = { Text(filter.label) },
                    )
                }
            }

            when {
                state.isLoading -> Unit
                state.trips.isEmpty() -> EmptyState(
                    message = if (state.totalCount == 0) "No trips yet. Plan your first one!" else "No trips match your search",
                    modifier = Modifier.fillMaxSize(),
                )
                else -> LazyColumn(
                    contentPadding = PaddingValues(start = 16.dp, top = 8.dp, end = 16.dp, bottom = 88.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    items(state.trips, key = { it.id }) { trip ->
                        TripCard(trip = trip, onClick = { onTripClick(trip.id) })
                    }
                }
            }
        }
    }
}