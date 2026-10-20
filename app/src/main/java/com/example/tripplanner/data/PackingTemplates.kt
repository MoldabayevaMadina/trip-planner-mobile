package com.example.tripplanner.data

enum class PackingTemplate(val emoji: String, val label: String, val items: List<String>) {
    ESSENTIALS("🎒", "Essentials", listOf("Passport / ID", "Phone charger", "Power bank", "Bank card & cash", "Medicines")),
    CITY("🏙", "City", listOf("Comfortable shoes", "Umbrella", "Day backpack", "Water bottle")),
    BEACH("🏖", "Beach", listOf("Swimsuit", "Sunscreen", "Sunglasses", "Flip-flops", "Hat")),
    MOUNTAINS("🏔", "Mountains", listOf("Warm jacket", "Hiking boots", "Thermos", "Gloves", "Raincoat")),
}