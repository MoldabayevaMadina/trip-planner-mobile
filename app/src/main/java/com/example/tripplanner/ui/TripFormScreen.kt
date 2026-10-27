package com.example.tripplanner.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.tripplanner.ui.components.LabeledTextField


@Composable
fun TripFormRoute(
    onBack: () -> Unit,
    onSaved: (tripId: Long, isNew: Boolean) -> Unit,
    viewModel: TripFormViewModel = viewModel(factory = TripFormViewModel.Factory),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()


    LaunchedEffect(state.savedTripId) {
        state.savedTripId?.let { id -> onSaved(id, !state.isEditing) }
    }

    TripFormScreen(
        state = state,
        onCityChange = viewModel::onCityChange,
        onCountryChange = viewModel::onCountryChange,
        onTitleChange = viewModel::onTitleChange,
        onStartDateChange = viewModel::onStartDateChange,
        onEndDateChange = viewModel::onEndDateChange,
        onNotesChange = viewModel::onNotesChange,
        onSave = viewModel::save,
        onBack = onBack,
    )
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TripFormScreen(
    state: TripFormUiState,
    onCityChange: (String) -> Unit,
    onCountryChange: (String) -> Unit,
    onTitleChange: (String) -> Unit,
    onStartDateChange: (String) -> Unit,
    onEndDateChange: (String) -> Unit,
    onNotesChange: (String) -> Unit,
    onSave: () -> Unit,
    onBack: () -> Unit,
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (state.isEditing) "Edit trip" else "New trip") },
                navigationIcon = { TextButton(onClick = onBack) { Text("←") } },
            )
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .imePadding()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            LabeledTextField(
                value = state.city,
                onValueChange = onCityChange,
                label = "City *",
                error = if (state.showErrors) state.cityError else null,
            )
            LabeledTextField(value = state.country, onValueChange = onCountryChange, label = "Country")
            LabeledTextField(
                value = state.title,
                onValueChange = onTitleChange,
                label = "Title",
                hint = "Optional — the city is used if empty",
            )
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                LabeledTextField(
                    value = state.startDate,
                    onValueChange = onStartDateChange,
                    label = "Start (YYYY-MM-DD)",
                    modifier = Modifier.weight(1f),
                )
                LabeledTextField(
                    value = state.endDate,
                    onValueChange = onEndDateChange,
                    label = "End (YYYY-MM-DD)",
                    modifier = Modifier.weight(1f),
                )
            }
            val dateError = state.dateError
            when {
                state.showErrors && dateError != null ->
                    Text(dateError, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                state.daysCount != null ->
                    Text("${state.daysCount} days", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.primary)
            }
            LabeledTextField(
                value = state.notes,
                onValueChange = onNotesChange,
                label = "Notes",
                hint = "Hotel, booking number, contacts…",
                singleLine = false,
                minLines = 3,
            )
            Button(
                onClick = onSave,
                enabled = !state.isSaving,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(
                    when {
                        state.isSaving -> "Saving…"
                        state.isEditing -> "Save changes"
                        else -> "Create trip"
                    }
                )
            }
        }
    }
}