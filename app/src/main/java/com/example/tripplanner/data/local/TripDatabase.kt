package com.example.tripplanner.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

@Database(
    entities = [TripEntity::class, PlaceEntity::class, PackingItemEntity::class, StatusChangeEntity::class],
    version = 1,
    exportSchema = false,
)
@TypeConverters(Converters::class)
abstract class TripDatabase : RoomDatabase() {

    abstract fun tripDao(): TripDao

    companion object {
        @Volatile
        private var instance: TripDatabase? = null

        /** One database for the whole app (creating several is slow and unsafe). */
        fun getInstance(context: Context): TripDatabase =
            instance ?: synchronized(this) {
                instance ?: Room.databaseBuilder(
                    context.applicationContext,
                    TripDatabase::class.java,
                    "trip_planner.db",
                ).build().also { instance = it }
            }
    }
}