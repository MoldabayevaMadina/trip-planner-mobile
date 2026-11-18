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
import com.example.tripplanner.data.StatusAction
import com.example.tripplanner.data.Trip
import com.example.tripplanner.data.TripRepository
import com.example.tripplanner.data.TripStatus
import com.example.tripplanner.data.TripWorkflow
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class TripDetailUiState(
    val trip: Trip? = null,
    val statusActions: List<StatusAction> = emptyList(),
    val isLoading: Boolean = false,
)

class TripDetailViewModel(
    savedStateHandle: SavedStateHandle,
    private val repository: TripRepository,
) : ViewModel() {

    private val tripId: Long = checkNotNull(savedStateHandle.get<Long>("tripId"))

    /** One-time messages for a snackbar ("Status: Ready", "Added 4 items"...). */
    private val _messages = Channel<String>(Channel.BUFFERED)
    val messages: Flow<String> = _messages.receiveAsFlow()

    val uiState: StateFlow<TripDetailUiState> = repository.observeTrip(tripId)
        .map { trip ->
            TripDetailUiState(
                trip = trip,
                statusActions = trip?.let { TripWorkflow.actions(it) } ?: emptyList(),
            )
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = TripDetailUiState(isLoading = true),
        )

    // ---------- places ----------

    fun addPlace(name: String, category: PlaceCategory) = launch {
        repository.addPlace(tripId, name, category)
    }

    fun togglePlaceVisited(placeId: Long) = launch { repository.togglePlaceVisited(placeId) }

    fun deletePlace(placeId: Long) = launch { repository.deletePlace(placeId) }

    fun restorePlace(place: Place) = launch { repository.restorePlace(tripId, place) }

    // ---------- packing list ----------

    fun addPackingItem(name: String) = launch { repository.addPackingItems(tripId, listOf(name)) }

    fun togglePacked(itemId: Long) = launch { repository.togglePacked(itemId) }

    fun deletePackingItem(itemId: Long) = launch { repository.deletePackingItem(itemId) }

    fun restorePackingItem(item: PackingItem) = launch { repository.restorePackingItem(tripId, item) }

    /** Adds the template's items, skipping ones already in the list (case-insensitive). */
    fun applyTemplate(template: PackingTemplate) {
        val trip = uiState.value.trip ?: return
        val existing = trip.packing.map { it.name.lowercase() }.toSet()
        val newNames = template.items.filter { it.lowercase() !in existing }
        launch {
            repository.addPackingItems(tripId, newNames)
            _messages.send(
                if (newNames.isEmpty()) "All \"${template.label}\" items are already in the list"
                else "Added ${newNames.size} item${if (newNames.size == 1) "" else "s"} from \"${template.label}\""
            )
        }
    }

    // ---------- status workflow ----------

    fun changeStatus(target: TripStatus) = launch {
        val changed = repository.changeStatus(tripId, target)
        _messages.send(
            if (changed) "Status changed to ${target.emoji} ${target.label}"
            else "Can't change the status right now"
        )
    }

    // ---------- trip ----------

    fun deleteTrip() = launch { repository.deleteTrip(tripId) }

    /** Every database call runs in a coroutine, off the main thread. */
    private fun launch(block: suspend () -> Unit) {
        viewModelScope.launch { block() }
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