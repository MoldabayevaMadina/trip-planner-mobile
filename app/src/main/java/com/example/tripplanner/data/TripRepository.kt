package com.example.tripplanner.data

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import java.util.concurrent.atomic.AtomicLong


interface TripRepository {

    fun observeTrips(): Flow<List<Trip>>

    fun observeTrip(id: Long): Flow<Trip?>

    suspend fun getTrip(id: Long): Trip?


    suspend fun saveTrip(trip: Trip): Long

    suspend fun deleteTrip(id: Long)


    suspend fun updateTrip(id: Long, change: (Trip) -> Trip)


    fun newId(): Long
}

class InMemoryTripRepository(initial: List<Trip> = SampleData.trips()) : TripRepository {

    private val trips = MutableStateFlow(initial)
    private val ids = AtomicLong(1_000)

    override fun observeTrips(): Flow<List<Trip>> = trips.asStateFlow()

    override fun observeTrip(id: Long): Flow<Trip?> =
        trips.map { list -> list.find { it.id == id } }

    override suspend fun getTrip(id: Long): Trip? = trips.value.find { it.id == id }

    override suspend fun saveTrip(trip: Trip): Long {
        val id = if (trip.id == 0L) newId() else trip.id
        val saved = trip.copy(id = id)
        trips.update { list ->
            if (list.any { it.id == id }) list.map { if (it.id == id) saved else it }
            else list + saved
        }
        return id
    }

    override suspend fun deleteTrip(id: Long) {
        trips.update { list -> list.filterNot { it.id == id } }
    }

    override suspend fun updateTrip(id: Long, change: (Trip) -> Trip) {
        trips.update { list -> list.map { if (it.id == id) change(it) else it } }
    }

    override fun newId(): Long = ids.incrementAndGet()
}