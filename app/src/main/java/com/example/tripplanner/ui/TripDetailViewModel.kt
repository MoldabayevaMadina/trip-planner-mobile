package com.example.tripplanner.ui

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.tripplanner.TripPlannerApplication
import com.example.tripplanner.data.PackingItem
import com.example.tripplanner.data.PackingTemplate
import com.example.tripplanner.data.Place
import com.example.tripplanner.data.PlaceCategory
import com.example.tripplanner.data.Trip
import com.example.tripplanner.data.TripRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class TripDetailUiState(
    val trip: Trip? = null,
    val isLoading: Boolean = false,
)

class TripDetailViewModel(
    savedStateHandle: SavedStateHandle,
    private val repository: TripRepository,
) : ViewModel() {


    private val tripId: Long = checkNotNull(savedStateHandle.get<Long>("tripId"))

    val uiState: StateFlow<TripDetailUiState> = repository.observeTrip(tripId)
        .map { trip -> TripDetailUiState(trip = trip) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = TripDetailUiState(isLoading = true),
        )


    fun addPlace(name: String, category: PlaceCategory) {
        val place = Place(id = repository.newId(), name = name.trim(), category = category)
        update { trip -> trip.copy(places = trip.places + place) }
    }

    fun togglePlaceVisited(placeId: Long) {
        update { trip ->
            trip.copy(places = trip.places.map { if (it.id == placeId) it.copy(visited = !it.visited) else it })
        }
    }

    fun deletePlace(placeId: Long) {
        update { trip -> trip.copy(places = trip.places.filterNot { it.id == placeId }) }
    }

    fun restorePlace(place: Place, index: Int) {
        update { trip ->
            val places = trip.places.toMutableList()
            places.add(index.coerceIn(0, places.size), place)
            trip.copy(places = places)
        }
    }


    fun addPackingItem(name: String) {
        val item = PackingItem(id = repository.newId(), name = name.trim())
        update { trip -> trip.copy(packing = trip.packing + item) }
    }

    fun togglePacked(itemId: Long) {
        update { trip ->
            trip.copy(packing = trip.packing.map { if (it.id == itemId) it.copy(packed = !it.packed) else it })
        }
    }

    fun deletePackingItem(itemId: Long) {
        update { trip -> trip.copy(packing = trip.packing.filterNot { it.id == itemId }) }
    }

    fun restorePackingItem(item: PackingItem, index: Int) {
        update { trip ->
            val packing = trip.packing.toMutableList()
            packing.add(index.coerceIn(0, packing.size), item)
            trip.copy(packing = packing)
        }
    }


    fun applyTemplate(template: PackingTemplate) {
        val trip = uiState.value.trip ?: return
        val existing = trip.packing.map { it.name.lowercase() }.toSet()
        val newItems = template.items
            .filter { it.lowercase() !in existing }
            .map { PackingItem(id = repository.newId(), name = it) }
        update { it.copy(packing = it.packing + newItems) }
    }



    fun deleteTrip() {
        viewModelScope.launch { repository.deleteTrip(tripId) }
    }

    private fun update(change: (Trip) -> Trip) {
        viewModelScope.launch { repository.updateTrip(tripId, change) }
    }

    companion object {
        val Factory = viewModelFactory {
            initializer {
                val app = this[APPLICATION_KEY] as TripPlannerApplication
                TripDetailViewModel(createSavedStateHandle(), app.repository)
            }
        }
    }
}