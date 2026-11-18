package com.example.tripplanner.data

import java.time.LocalDate
import java.time.temporal.ChronoUnit

enum class PlaceCategory(val emoji: String, val label: String) {
    SIGHT("🏛", "Sight"),
    MUSEUM("🖼", "Museum"),
    FOOD("🍽", "Food"),
    WALK("🚶", "Walk"),
    SHOPPING("🛍", "Shopping"),
    OTHER("📍", "Other"),
}

/** Stage of a trip. Changes only through TripWorkflow rules. */
enum class TripStatus(val emoji: String, val label: String) {
    PLANNING("📝", "Planning"),
    READY("🎒", "Ready"),
    IN_PROGRESS("✈️", "In progress"),
    COMPLETED("✅", "Completed"),
    CANCELLED("✖️", "Cancelled"),
}

data class Place(
    val id: Long,
    val name: String,
    val category: PlaceCategory = PlaceCategory.SIGHT,
    val visited: Boolean = false,
    val position: Int = 0,
)

data class PackingItem(
    val id: Long,
    val name: String,
    val packed: Boolean = false,
    val position: Int = 0,
)

/** One record of the status history: from which status to which, and when (epoch millis). */
data class StatusChange(
    val id: Long,
    val from: TripStatus?,
    val to: TripStatus,
    val changedAt: Long,
)

data class Trip(
    val id: Long,
    val title: String,
    val city: String,
    val country: String,
    val startDate: LocalDate,
    val endDate: LocalDate,
    val places: List<Place> = emptyList(),
    val packing: List<PackingItem> = emptyList(),
    val notes: String = "",
    val status: TripStatus = TripStatus.PLANNING,
    val history: List<StatusChange> = emptyList(),
) {
    val daysCount: Int
        get() = ChronoUnit.DAYS.between(startDate, endDate).toInt() + 1

    val visitedCount: Int
        get() = places.count { it.visited }

    val packedCount: Int
        get() = packing.count { it.packed }

    fun isPast(today: LocalDate = LocalDate.now()): Boolean = endDate.isBefore(today)

    fun statusText(today: LocalDate = LocalDate.now()): String {
        val daysToStart = ChronoUnit.DAYS.between(today, startDate)
        return when {
            isPast(today) -> "Finished"
            daysToStart > 1 -> "In $daysToStart days"
            daysToStart == 1L -> "Tomorrow"
            else -> "Now · day ${-daysToStart + 1} of $daysCount"
        }
    }
}