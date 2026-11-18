package com.example.tripplanner.data.local

import com.example.tripplanner.data.PackingItem
import com.example.tripplanner.data.Place
import com.example.tripplanner.data.StatusChange
import com.example.tripplanner.data.Trip

fun TripWithDetails.toTrip(): Trip = Trip(
    id = trip.id,
    title = trip.title,
    city = trip.city,
    country = trip.country,
    startDate = trip.startDate,
    endDate = trip.endDate,
    notes = trip.notes,
    status = trip.status,
    places = places.sortedWith(compareBy({ it.position }, { it.id })).map { it.toPlace() },
    packing = packing.sortedWith(compareBy({ it.position }, { it.id })).map { it.toPackingItem() },
    history = history.sortedByDescending { it.changedAt }.map { it.toStatusChange() },
)

fun PlaceEntity.toPlace() =
    Place(id = id, name = name, category = category, visited = visited, position = position)

fun PackingItemEntity.toPackingItem() =
    PackingItem(id = id, name = name, packed = packed, position = position)

fun StatusChangeEntity.toStatusChange() =
    StatusChange(id = id, from = fromStatus, to = toStatus, changedAt = changedAt)

/** Only the trip's own columns; places, packing and history live in their own tables. */
fun Trip.toEntity() = TripEntity(
    id = id,
    title = title,
    city = city,
    country = country,
    startDate = startDate,
    endDate = endDate,
    notes = notes,
    status = status,
)