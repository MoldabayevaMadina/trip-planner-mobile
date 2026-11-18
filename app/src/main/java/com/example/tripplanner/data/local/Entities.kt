package com.example.tripplanner.data.local

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import androidx.room.Relation
import com.example.tripplanner.data.PlaceCategory
import com.example.tripplanner.data.TripStatus
import java.time.LocalDate

/** Table "trips": one row per trip. */
@Entity(tableName = "trips")
data class TripEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val city: String,
    val country: String,
    val startDate: LocalDate,
    val endDate: LocalDate,
    val notes: String = "",
    val status: TripStatus = TripStatus.PLANNING,
)

/** Table "places": places of a trip. Deleted together with the trip (CASCADE). */
@Entity(
    tableName = "places",
    foreignKeys = [ForeignKey(
        entity = TripEntity::class,
        parentColumns = ["id"],
        childColumns = ["tripId"],
        onDelete = ForeignKey.CASCADE,
    )],
    indices = [Index("tripId")],
)
data class PlaceEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val tripId: Long,
    val name: String,
    val category: PlaceCategory = PlaceCategory.SIGHT,
    val visited: Boolean = false,
    val position: Int = 0,
)

/** Table "packing_items": the packing list of a trip. */
@Entity(
    tableName = "packing_items",
    foreignKeys = [ForeignKey(
        entity = TripEntity::class,
        parentColumns = ["id"],
        childColumns = ["tripId"],
        onDelete = ForeignKey.CASCADE,
    )],
    indices = [Index("tripId")],
)
data class PackingItemEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val tripId: Long,
    val name: String,
    val packed: Boolean = false,
    val position: Int = 0,
)

/** Table "status_changes": history of status transitions of a trip. */
@Entity(
    tableName = "status_changes",
    foreignKeys = [ForeignKey(
        entity = TripEntity::class,
        parentColumns = ["id"],
        childColumns = ["tripId"],
        onDelete = ForeignKey.CASCADE,
    )],
    indices = [Index("tripId")],
)
data class StatusChangeEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val tripId: Long,
    val fromStatus: TripStatus?,
    val toStatus: TripStatus,
    val changedAt: Long,
)

/**
 * Not a table: a trip together with its rows from the three child tables.
 * Room fills the lists itself using tripId (@Relation).
 */
data class TripWithDetails(
    @Embedded val trip: TripEntity,
    @Relation(parentColumn = "id", entityColumn = "tripId")
    val places: List<PlaceEntity>,
    @Relation(parentColumn = "id", entityColumn = "tripId")
    val packing: List<PackingItemEntity>,
    @Relation(parentColumn = "id", entityColumn = "tripId")
    val history: List<StatusChangeEntity>,
)