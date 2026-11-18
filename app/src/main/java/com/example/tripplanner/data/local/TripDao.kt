package com.example.tripplanner.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.example.tripplanner.data.TripStatus
import kotlinx.coroutines.flow.Flow

@Dao
interface TripDao {

    @Transaction
    @Query("SELECT * FROM trips ORDER BY startDate, id")
    fun observeTrips(): Flow<List<TripWithDetails>>

    @Transaction
    @Query("SELECT * FROM trips WHERE id = :id")
    fun observeTrip(id: Long): Flow<TripWithDetails?>

    @Transaction
    @Query("SELECT * FROM trips WHERE id = :id")
    suspend fun getTrip(id: Long): TripWithDetails?

    @Query("SELECT COUNT(*) FROM trips")
    suspend fun countTrips(): Int

    // ---------- trips ----------

    @Insert
    suspend fun insertTrip(trip: TripEntity): Long

    @Update
    suspend fun updateTrip(trip: TripEntity)

    @Query("UPDATE trips SET status = :status WHERE id = :tripId")
    suspend fun updateStatus(tripId: Long, status: TripStatus)

    @Query("DELETE FROM trips WHERE id = :id")
    suspend fun deleteTrip(id: Long)

    // ---------- places ----------

    @Insert
    suspend fun insertPlace(place: PlaceEntity): Long

    @Query("UPDATE places SET visited = NOT visited WHERE id = :id")
    suspend fun togglePlaceVisited(id: Long)

    @Query("DELETE FROM places WHERE id = :id")
    suspend fun deletePlace(id: Long)

    @Query("SELECT COALESCE(MAX(position), -1) + 1 FROM places WHERE tripId = :tripId")
    suspend fun nextPlacePosition(tripId: Long): Int

    // ---------- packing list ----------

    @Insert
    suspend fun insertPackingItem(item: PackingItemEntity): Long

    @Insert
    suspend fun insertPackingItems(items: List<PackingItemEntity>)

    @Query("UPDATE packing_items SET packed = NOT packed WHERE id = :id")
    suspend fun togglePacked(id: Long)

    @Query("DELETE FROM packing_items WHERE id = :id")
    suspend fun deletePackingItem(id: Long)

    @Query("SELECT COALESCE(MAX(position), -1) + 1 FROM packing_items WHERE tripId = :tripId")
    suspend fun nextPackingPosition(tripId: Long): Int

    // ---------- status history ----------

    @Insert
    suspend fun insertStatusChange(change: StatusChangeEntity)
}