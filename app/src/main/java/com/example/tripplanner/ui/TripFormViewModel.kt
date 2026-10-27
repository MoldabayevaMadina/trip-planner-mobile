package com.example.tripplanner.ui

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.tripplanner.TripPlannerApplication
import com.example.tripplanner.data.Trip
import com.example.tripplanner.data.TripRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.temporal.ChronoUnit


data class TripFormUiState(
    val isEditing: Boolean = false,
    val city: String = "",
    val country: String = "",
    val title: String = "",
    val startDate: String = LocalDate.now().plusDays(7).toString(),
    val endDate: String = LocalDate.now().plusDays(10).toString(),
    val notes: String = "",
    val showErrors: Boolean = false,
    val isSaving: Boolean = false,
    val savedTripId: Long? = null,
) {
    val parsedStart: LocalDate? get() = runCatching { LocalDate.parse(startDate) }.getOrNull()
    val parsedEnd: LocalDate? get() = runCatching { LocalDate.parse(endDate) }.getOrNull()

    val cityError: String?
        get() = if (city.isBlank()) "Enter a city" else null

    val dateError: String?
        get() {
            val start = parsedStart
            val end = parsedEnd
            return when {
                start == null || end == null -> "Dates must look like 2026-11-03"
                end.isBefore(start) -> "End date is before start date"
                else -> null
            }
        }

    val daysCount: Long?
        get() {
            val start = parsedStart ?: return null
            val end = parsedEnd ?: return null
            return if (end.isBefore(start)) null else ChronoUnit.DAYS.between(start, end) + 1
        }

    val isValid: Boolean get() = cityError == null && dateError == null
}

class TripFormViewModel(
    savedStateHandle: SavedStateHandle,
    private val repository: TripRepository,
) : ViewModel() {

    private val tripId: Long = savedStateHandle.get<Long>("tripId") ?: NEW_TRIP

    private var original: Trip? = null

    private val _uiState = MutableStateFlow(TripFormUiState(isEditing = tripId != NEW_TRIP))
    val uiState: StateFlow<TripFormUiState> = _uiState.asStateFlow()

    init {
        if (tripId != NEW_TRIP) {
            viewModelScope.launch {
                val trip = repository.getTrip(tripId) ?: return@launch
                original = trip
                _uiState.update {
                    it.copy(
                        city = trip.city,
                        country = trip.country,
                        title = trip.title,
                        startDate = trip.startDate.toString(),
                        endDate = trip.endDate.toString(),
                        notes = trip.notes,
                    )
                }
            }
        }
    }

    fun onCityChange(value: String) = _uiState.update { it.copy(city = value) }
    fun onCountryChange(value: String) = _uiState.update { it.copy(country = value) }
    fun onTitleChange(value: String) = _uiState.update { it.copy(title = value) }
    fun onStartDateChange(value: String) = _uiState.update { it.copy(startDate = value) }
    fun onEndDateChange(value: String) = _uiState.update { it.copy(endDate = value) }
    fun onNotesChange(value: String) = _uiState.update { it.copy(notes = value) }

    fun save() {
        val state = _uiState.value
        val start = state.parsedStart
        val end = state.parsedEnd
        if (!state.isValid || start == null || end == null) {
            _uiState.update { it.copy(showErrors = true) }
            return
        }
        _uiState.update { it.copy(isSaving = true) }

        viewModelScope.launch {
            val base = original ?: Trip(id = 0, title = "", city = "", country = "", startDate = start, endDate = end)
            val id = repository.saveTrip(
                base.copy(
                    title = state.title.trim().ifBlank { state.city.trim() },
                    city = state.city.trim(),
                    country = state.country.trim(),
                    startDate = start,
                    endDate = end,
                    notes = state.notes.trim(),
                )
            )
            _uiState.update { it.copy(isSaving = false, savedTripId = id) }
        }
    }

    companion object {
        const val NEW_TRIP = -1L

        val Factory = viewModelFactory {
            initializer {
                val app = this[APPLICATION_KEY] as TripPlannerApplication
                TripFormViewModel(createSavedStateHandle(), app.repository)
            }
        }
    }
}