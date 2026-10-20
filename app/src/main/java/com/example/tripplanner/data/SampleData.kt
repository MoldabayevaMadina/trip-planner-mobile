package com.example.tripplanner.data

import java.time.LocalDate

object SampleData {
    fun trips(): List<Trip> {
        val today = LocalDate.now()
        return listOf(
            Trip(
                id = 1, title = "Istanbul with friends", city = "Istanbul", country = "Türkiye",
                startDate = today.plusDays(5), endDate = today.plusDays(9),
                places = listOf(
                    Place(1, "Hagia Sophia", PlaceCategory.SIGHT),
                    Place(2, "Grand Bazaar", PlaceCategory.SHOPPING),
                    Place(3, "Bosphorus ferry", PlaceCategory.WALK),
                    Place(4, "Dinner in Karaköy", PlaceCategory.FOOD),
                ),
                packing = listOf(
                    PackingItem(20, "Passport / ID", packed = true),
                    PackingItem(21, "Phone charger"),
                    PackingItem(22, "Comfortable shoes"),
                ),
            ),
            Trip(
                id = 2, title = "Almaty weekend", city = "Almaty", country = "Kazakhstan",
                startDate = today.plusDays(20), endDate = today.plusDays(22),
                places = listOf(
                    Place(5, "Medeu", PlaceCategory.WALK),
                    Place(6, "Kok-Tobe", PlaceCategory.SIGHT),
                ),
            ),
            Trip(
                id = 3, title = "Tbilisi trip", city = "Tbilisi", country = "Georgia",
                startDate = today.minusDays(40), endDate = today.minusDays(35),
                places = listOf(
                    Place(7, "Old Town walk", PlaceCategory.WALK, visited = true),
                    Place(8, "Narikala Fortress", PlaceCategory.SIGHT, visited = true),
                    Place(9, "Georgian National Museum", PlaceCategory.MUSEUM),
                ),
            ),
        )
    }
}