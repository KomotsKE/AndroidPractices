package com.example.androidpractices.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import kotlinx.coroutines.flow.Flow

@Dao
abstract class FavoritesDao {

    @Query("SELECT * FROM favorite_breweries ORDER BY added_at DESC")
    abstract fun observeAll(): Flow<List<FavoriteBreweryEntity>>

    @Query("SELECT id FROM favorite_breweries")
    abstract fun observeIds(): Flow<List<String>>

    @Query("SELECT * FROM favorite_breweries WHERE id = :id")
    abstract suspend fun getById(id: String): FavoriteBreweryEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract suspend fun upsert(entity: FavoriteBreweryEntity)

    @Query("DELETE FROM favorite_breweries WHERE id = :id")
    abstract suspend fun deleteById(id: String)

    /** Атомарно: если уже в избранном — удаляем, иначе добавляем. */
    @Transaction
    open suspend fun toggle(entity: FavoriteBreweryEntity) {
        if (getById(entity.id) != null) deleteById(entity.id) else upsert(entity)
    }
}
