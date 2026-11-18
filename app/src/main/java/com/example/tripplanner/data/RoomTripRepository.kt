package com.example.tripplanner.data

import androidx.room.withTransaction
import com.example.tripplanner.data.local.PackingItemEntity
import com.example.tripplanner.data.local.PlaceEntity
import com.example.tripplanner.data.local.StatusChangeEntity
import com.example.tripplanner.data.local.TripDatabase
import com.example.tripplanner.data.local.toEntity
import com.example.tripplanner.data.local.toTrip
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class RoomTripRepository(private val db: TripDatabase) : TripRepository {

    private val dao = db.tripDao()

    private fun now() = System.currentTimeMillis()



    override fun observeTrips(): Flow<List<Trip>> =
        dao.observeTrips().map { rows -> rows.map { it.toTrip() } }

    override fun observeTrip(id: Long): Flow<Trip?> =
        dao.observeTrip(id).map { it?.toTrip() }

    override suspend fun getTrip(id: Long): Trip? = dao.getTrip(id)?.toTrip()

    // ---------- trips ----------

    override suspend fun saveTrip(trip: Trip): Long = db.withTransaction {
        if (trip.id == 0L) {
            val id = dao.insertTrip(trip.toEntity().copy(id = 0, status = TripStatus.PLANNING))
            dao.insertStatusChange(
                StatusChangeEntity(tripId = id, fromStatus = null, toStatus = TripStatus.PLANNING, changedAt = now())
            )
            id
        } else {
            val current = dao.getTrip(trip.id)?.trip ?: return@withTransaction trip.id
            dao.updateTrip(trip.toEntity().copy(status = current.status))
            trip.id
        }
    }

    override suspend fun deleteTrip(id: Long) = dao.deleteTrip(id)

    // ---------- places ----------

    override suspend fun addPlace(tripId: Long, name: String, category: PlaceCategory) {
        db.withTransaction {
            dao.insertPlace(
                PlaceEntity(
                    tripId = tripId,
                    name = name.trim(),
                    category = category,
                    position = dao.nextPlacePosition(tripId),
                )
            )
        }
    }

    override suspend fun togglePlaceVisited(placeId: Long) = dao.togglePlaceVisited(placeId)

    override suspend fun deletePlace(placeId: Long) = dao.deletePlace(placeId)

    override suspend fun restorePlace(tripId: Long, place: Place) {
        dao.insertPlace(
            PlaceEntity(
                id = place.id,
                tripId = tripId,
                name = place.name,
                category = place.category,
                visited = place.visited,
                position = place.position,
            )
        )
    }

    // ---------- packing list ----------

    override suspend fun addPackingItems(tripId: Long, names: List<String>) {
        if (names.isEmpty()) return
        db.withTransaction {
            val start = dao.nextPackingPosition(tripId)
            dao.insertPackingItems(
                names.mapIndexed { i, name ->
                    PackingItemEntity(tripId = tripId, name = name.trim(), position = start + i)
                }
            )
        }
    }

    override suspend fun togglePacked(itemId: Long) = dao.togglePacked(itemId)

    override suspend fun deletePackingItem(itemId: Long) = dao.deletePackingItem(itemId)

    override suspend fun restorePackingItem(tripId: Long, item: PackingItem) {
        dao.insertPackingItem(
            PackingItemEntity(
                id = item.id,
                tripId = tripId,
                name = item.name,
                packed = item.packed,
                position = item.position,
            )
        )
    }

    // ---------- status workflow ----------

    override suspend fun changeStatus(tripId: Long, target: TripStatus): Boolean = db.withTransaction {
        val trip = dao.getTrip(tripId)?.toTrip() ?: return@withTransaction false
        if (!TripWorkflow.canMove(trip, target)) return@withTransaction false
        dao.updateStatus(tripId, target)
        dao.insertStatusChange(
            StatusChangeEntity(tripId = tripId, fromStatus = trip.status, toStatus = target, changedAt = now())
        )
        true
    }

    // ---------- sample data ----------

    override suspend fun addSampleData() {
        db.withTransaction {
            if (dao.countTrips() > 0) return@withTransaction
            SampleData.trips().forEach { sample ->
                val tripId = dao.insertTrip(sample.toEntity().copy(id = 0))
                sample.places.forEachIndexed { i, place ->
                    dao.insertPlace(
                        PlaceEntity(
                            tripId = tripId, name = place.name, category = place.category,
                            visited = place.visited, position = i,
                        )
                    )
                }
                sample.packing.forEachIndexed { i, item ->
                    dao.insertPackingItem(
                        PackingItemEntity(tripId = tripId, name = item.name, packed = item.packed, position = i)
                    )
                }
                dao.insertStatusChange(
                    StatusChangeEntity(tripId = tripId, fromStatus = null, toStatus = sample.status, changedAt = now())
                )
            }
        }
    }
}