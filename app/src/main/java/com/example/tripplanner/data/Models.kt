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

data class Place(
    val id: Long,
    val name: String,
    val category: PlaceCategory = PlaceCategory.SIGHT,
    val visited: Boolean = false,
)

data class PackingItem(
    val id: Long,
    val name: String,
    val packed: Boolean = false,
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