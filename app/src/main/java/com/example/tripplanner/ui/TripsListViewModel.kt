package com.example.tripplanner.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.tripplanner.TripPlannerApplication
import com.example.tripplanner.data.Trip
import com.example.tripplanner.data.TripRepository
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.stateIn

enum class TripFilter(val label: String) {
    ALL("All"),
    UPCOMING("Upcoming"),
    PAST("Past"),
}

data class TripsListUiState(
    val trips: List<Trip> = emptyList(),
    val filter: TripFilter = TripFilter.ALL,
    val totalCount: Int = 0,
    val isLoading: Boolean = false,
)

@OptIn(FlowPreview::class)
class TripsListViewModel(repository: TripRepository) : ViewModel() {


    private val _query = MutableStateFlow("")
    val query: StateFlow<String> = _query.asStateFlow()

    private val filter = MutableStateFlow(TripFilter.ALL)


    val uiState: StateFlow<TripsListUiState> = combine(
        repository.observeTrips(),
        _query.debounce { text -> if (text.isBlank()) 0L else 300L },
        filter,
    ) { trips, search, selectedFilter ->
        TripsListUiState(
            trips = trips
                .filter { it.matchesSearch(search) && it.matchesFilter(selectedFilter) }
                .sortedBy { it.startDate },
            filter = selectedFilter,
            totalCount = trips.size,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = TripsListUiState(isLoading = true),
    )

    fun onQueryChange(text: String) {
        _query.value = text
    }

    fun onFilterChange(value: TripFilter) {
        filter.value = value
    }

    private fun Trip.matchesSearch(search: String): Boolean =
        search.isBlank() ||
                city.contains(search, ignoreCase = true) ||
                title.contains(search, ignoreCase = true) ||
                country.contains(search, ignoreCase = true)

    private fun Trip.matchesFilter(value: TripFilter): Boolean = when (value) {
        TripFilter.ALL -> true
        TripFilter.UPCOMING -> !isPast()
        TripFilter.PAST -> isPast()
    }

    companion object {
        val Factory = viewModelFactory {
            initializer {
                val app = this[APPLICATION_KEY] as TripPlannerApplication
                TripsListViewModel(app.repository)
            }
        }
    }
}