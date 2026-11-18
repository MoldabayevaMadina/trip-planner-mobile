package com.example.tripplanner.data

import java.time.LocalDate

/** A button on the Status tab: where it leads and why it is disabled (null = allowed). */
data class StatusAction(
    val target: TripStatus,
    val label: String,
    val blockedReason: String? = null,
) {
    val isAllowed: Boolean get() = blockedReason == null
}

/**
 * Rules of the trip workflow:
 *
 *   PLANNING ──→ READY ──→ IN_PROGRESS ──→ COMPLETED
 *      │  ↑        │
 *      │  └────────┘ (back to planning)
 *      ↓
 *   CANCELLED ──→ PLANNING (restore)
 */
object TripWorkflow {

    fun actions(trip: Trip, today: LocalDate = LocalDate.now()): List<StatusAction> =
        when (trip.status) {
            TripStatus.PLANNING -> listOf(
                StatusAction(TripStatus.READY, "Mark as ready", readyBlockedReason(trip)),
                StatusAction(TripStatus.CANCELLED, "Cancel trip"),
            )
            TripStatus.READY -> listOf(
                StatusAction(
                    TripStatus.IN_PROGRESS, "Start trip",
                    if (today.isBefore(trip.startDate)) "You can start on ${trip.startDate}" else null,
                ),
                StatusAction(TripStatus.PLANNING, "Back to planning"),
                StatusAction(TripStatus.CANCELLED, "Cancel trip"),
            )
            TripStatus.IN_PROGRESS -> listOf(
                StatusAction(TripStatus.COMPLETED, "Finish trip", finishBlockedReason(trip, today)),
            )
            TripStatus.CANCELLED -> listOf(
                StatusAction(TripStatus.PLANNING, "Restore trip"),
            )
            TripStatus.COMPLETED -> emptyList()   // final status
        }

    /** True if the trip may move to [target] right now. Checked again before saving. */
    fun canMove(trip: Trip, target: TripStatus, today: LocalDate = LocalDate.now()): Boolean =
        actions(trip, today).any { it.target == target && it.isAllowed }

    private fun readyBlockedReason(trip: Trip): String? {
        val left = trip.packing.size - trip.packedCount
        return when {
            trip.places.isEmpty() -> "Add at least one place"
            trip.packing.isEmpty() -> "Add items to the packing list"
            left > 0 -> "Pack $left more item${if (left == 1) "" else "s"}"
            else -> null
        }
    }

    private fun finishBlockedReason(trip: Trip, today: LocalDate): String? {
        val allVisited = trip.visitedCount == trip.places.size
        val lastDayCame = !today.isBefore(trip.endDate)
        return if (allVisited || lastDayCame) null
        else "Visit all places or wait until ${trip.endDate}"
    }
}