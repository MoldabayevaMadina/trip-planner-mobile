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
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.tripplanner.ui.components.EmptyState
import com.example.tripplanner.ui.components.TripCard
import java.time.LocalDate

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TripsListScreen(
    state: TripsUiState,
    onQueryChange: (String) -> Unit,
    onFilterChange: (TripFilter) -> Unit,
    onTripClick: (Long) -> Unit,
    onAddTrip: (title: String, city: String, country: String, start: LocalDate, end: LocalDate) -> Unit,
) {

    var showAddDialog by rememberSaveable { mutableStateOf(false) }

    Scaffold(
        topBar = { TopAppBar(title = { Text("My trips") }) },
        floatingActionButton = {
            ExtendedFloatingActionButton(onClick = { showAddDialog = true }) { Text("+ New trip") }
        },
    ) { padding ->
        Column(modifier = Modifier.padding(padding).fillMaxSize()) {
            OutlinedTextField(
                value = state.query,
                onValueChange = onQueryChange,
                label = { Text("Search by city or title") },
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

            val trips = state.visibleTrips
            if (trips.isEmpty()) {
                EmptyState(message = "No trips found", modifier = Modifier.fillMaxSize())
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(start = 16.dp, top = 8.dp, end = 16.dp, bottom = 88.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    items(trips, key = { it.id }) { trip ->
                        TripCard(trip = trip, onClick = { onTripClick(trip.id) })
                    }
                }
            }
        }
    }

    if (showAddDialog) {
        AddTripDialog(
            onDismiss = { showAddDialog = false },
            onSave = { title, city, country, start, end ->
                onAddTrip(title, city, country, start, end)
                showAddDialog = false
            },
        )
    }
}

@Composable
fun AddTripDialog(
    onDismiss: () -> Unit,
    onSave: (title: String, city: String, country: String, start: LocalDate, end: LocalDate) -> Unit,
) {

    var city by rememberSaveable { mutableStateOf("") }
    var country by rememberSaveable { mutableStateOf("") }
    var title by rememberSaveable { mutableStateOf("") }
    var startText by rememberSaveable { mutableStateOf(LocalDate.now().plusDays(7).toString()) }
    var endText by rememberSaveable { mutableStateOf(LocalDate.now().plusDays(10).toString()) }

    val start = runCatching { LocalDate.parse(startText) }.getOrNull()
    val end = runCatching { LocalDate.parse(endText) }.getOrNull()
    val error = when {
        city.isBlank() -> "Enter a city"
        start == null || end == null -> "Dates must look like 2026-11-03"
        end.isBefore(start) -> "End date is before start date"
        else -> null
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("New trip") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(value = city, onValueChange = { city = it }, label = { Text("City *") }, singleLine = true)
                OutlinedTextField(value = country, onValueChange = { country = it }, label = { Text("Country") }, singleLine = true)
                OutlinedTextField(value = title, onValueChange = { title = it }, label = { Text("Title (optional)") }, singleLine = true)
                OutlinedTextField(value = startText, onValueChange = { startText = it }, label = { Text("Start (YYYY-MM-DD)") }, singleLine = true)
                OutlinedTextField(value = endText, onValueChange = { endText = it }, label = { Text("End (YYYY-MM-DD)") }, singleLine = true)
                if (error != null) {
                    Text(error, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                }
            }
        },
        confirmButton = {
            TextButton(
                enabled = error == null,
                onClick = { onSave(title.trim(), city.trim(), country.trim(), start!!, end!!) },
            ) { Text("Save") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } },
    )
}