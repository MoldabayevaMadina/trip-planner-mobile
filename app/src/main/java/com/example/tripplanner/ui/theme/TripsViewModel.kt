package com.example.tripplanner.ui

import androidx.lifecycle.ViewModel
import com.example.tripplanner.data.PackingItem
import com.example.tripplanner.data.PackingTemplate
import com.example.tripplanner.data.Place
import com.example.tripplanner.data.PlaceCategory
import com.example.tripplanner.data.SampleData
import com.example.tripplanner.data.Trip
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.time.LocalDate

enum class TripFilter(val label: String) {
    ALL("All"),
    UPCOMING("Upcoming"),
    PAST("Past"),
}

data class TripsUiState(
    val trips: List<Trip> = emptyList(),
    val query: String = "",
    val filter: TripFilter = TripFilter.ALL,
    val selectedTripId: Long? = null,
) {

    val visibleTrips: List<Trip>
        get() = trips
            .filter { trip ->
                query.isBlank() ||
                        trip.city.contains(query, ignoreCase = true) ||
                        trip.title.contains(query, ignoreCase = true)
            }
            .filter { trip ->
                when (filter) {
                    TripFilter.ALL -> true
                    TripFilter.UPCOMING -> !trip.isPast()
                    TripFilter.PAST -> trip.isPast()
                }
            }
            .sortedBy { it.startDate }


    val selectedTrip: Trip?
        get() = trips.find { it.id == selectedTripId }
}

class TripsViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(TripsUiState(trips = SampleData.trips()))


    val uiState: StateFlow<TripsUiState> = _uiState.asStateFlow()

    private var nextId = 100L



    fun onQueryChange(query: String) {
        _uiState.update { it.copy(query = query) }
    }

    fun onFilterChange(filter: TripFilter) {
        _uiState.update { it.copy(filter = filter) }
    }

    fun openTrip(tripId: Long) {
        _uiState.update { it.copy(selectedTripId = tripId) }
    }

    fun closeTrip() {
        _uiState.update { it.copy(selectedTripId = null) }
    }

    fun addTrip(title: String, city: String, country: String, startDate: LocalDate, endDate: LocalDate) {
        val trip = Trip(
            id = nextId++,
            title = title.ifBlank { city },
            city = city,
            country = country,
            startDate = startDate,
            endDate = endDate,
        )
        _uiState.update { it.copy(trips = it.trips + trip) }
    }

    fun deleteTrip(tripId: Long) {
        _uiState.update { state ->
            state.copy(
                trips = state.trips.filterNot { it.id == tripId },
                selectedTripId = null,
            )
        }
    }


    fun addPlace(tripId: Long, name: String, category: PlaceCategory) {
        val place = Place(id = nextId++, name = name.trim(), category = category)
        updateTrip(tripId) { trip -> trip.copy(places = trip.places + place) }
    }

    fun togglePlaceVisited(tripId: Long, placeId: Long) {
        updateTrip(tripId) { trip ->
            trip.copy(places = trip.places.map { place ->
                if (place.id == placeId) place.copy(visited = !place.visited) else place
            })
        }
    }

    fun deletePlace(tripId: Long, placeId: Long) {
        updateTrip(tripId) { trip -> trip.copy(places = trip.places.filterNot { it.id == placeId }) }
    }


    fun restorePlace(tripId: Long, place: Place, index: Int) {
        updateTrip(tripId) { trip ->
            val places = trip.places.toMutableList()
            places.add(index.coerceIn(0, places.size), place)
            trip.copy(places = places)
        }
    }



    fun addPackingItem(tripId: Long, name: String) {
        val item = PackingItem(id = nextId++, name = name.trim())
        updateTrip(tripId) { trip -> trip.copy(packing = trip.packing + item) }
    }

    fun togglePacked(tripId: Long, itemId: Long) {
        updateTrip(tripId) { trip ->
            trip.copy(packing = trip.packing.map { item ->
                if (item.id == itemId) item.copy(packed = !item.packed) else item
            })
        }
    }

    fun deletePackingItem(tripId: Long, itemId: Long) {
        updateTrip(tripId) { trip -> trip.copy(packing = trip.packing.filterNot { it.id == itemId }) }
    }

    fun restorePackingItem(tripId: Long, item: PackingItem, index: Int) {
        updateTrip(tripId) { trip ->
            val packing = trip.packing.toMutableList()
            packing.add(index.coerceIn(0, packing.size), item)
            trip.copy(packing = packing)
        }
    }


    fun applyTemplate(tripId: Long, template: PackingTemplate) {
        val trip = _uiState.value.trips.find { it.id == tripId } ?: return
        val existing = trip.packing.map { it.name.lowercase() }.toSet()
        val newItems = template.items
            .filter { it.lowercase() !in existing }
            .map { PackingItem(id = nextId++, name = it) }
        updateTrip(tripId) { it.copy(packing = it.packing + newItems) }
    }


    private fun updateTrip(tripId: Long, change: (Trip) -> Trip) {
        _uiState.update { state ->
            state.copy(trips = state.trips.map { if (it.id == tripId) change(it) else it })
        }
    }
}