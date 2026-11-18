package com.example.tripplanner.data

import kotlinx.coroutines.flow.Flow

/**
 * Single source of truth for trips. Only ViewModels use it, screens never touch it.
 * Implemented with Room (RoomTripRepository).
 */
interface TripRepository {

    // ---------- read ----------
    fun observeTrips(): Flow<List<Trip>>
    fun observeTrip(id: Long): Flow<Trip?>
    suspend fun getTrip(id: Long): Trip?

    // ---------- trips ----------
    /** Insert a new trip (id == 0) or update title, city, dates and notes of an existing one. Returns the id. */
    suspend fun saveTrip(trip: Trip): Long
    suspend fun deleteTrip(id: Long)

    // ---------- places ----------
    suspend fun addPlace(tripId: Long, name: String, category: PlaceCategory)
    suspend fun togglePlaceVisited(placeId: Long)
    suspend fun deletePlace(placeId: Long)
    /** Undo: put a deleted place back with its old id and position. */
    suspend fun restorePlace(tripId: Long, place: Place)

    // ---------- packing list ----------
    suspend fun addPackingItems(tripId: Long, names: List<String>)
    suspend fun togglePacked(itemId: Long)
    suspend fun deletePackingItem(itemId: Long)
    suspend fun restorePackingItem(tripId: Long, item: PackingItem)

    // ---------- status workflow ----------
    /** Moves the trip to [target] if TripWorkflow allows it and records the change. Returns false if not allowed. */
    suspend fun changeStatus(tripId: Long, target: TripStatus): Boolean

    /** Fills an empty database with the sample trips (first launch only). */
    suspend fun addSampleData()
}