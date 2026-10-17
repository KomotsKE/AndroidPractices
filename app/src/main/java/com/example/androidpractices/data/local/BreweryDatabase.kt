package com.example.androidpractices.data.local

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [FavoriteBreweryEntity::class],
    version = 1,
    exportSchema = false,
)
abstract class BreweryDatabase : RoomDatabase() {

    abstract fun favoritesDao(): FavoritesDao

    companion object {
        const val NAME = "breweries.db"
    }
}
