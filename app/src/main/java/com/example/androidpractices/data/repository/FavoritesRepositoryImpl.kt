package com.example.androidpractices.data.repository

import com.example.androidpractices.data.local.FavoritesDao
import com.example.androidpractices.data.local.toDomain
import com.example.androidpractices.data.local.toEntity
import com.example.androidpractices.domain.model.Brewery
import com.example.androidpractices.domain.repository.FavoritesRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map

/** Избранное в Room. Room сам выполняет suspend-запросы вне главного потока. */
class FavoritesRepositoryImpl(
    private val dao: FavoritesDao,
    private val clock: () -> Long = System::currentTimeMillis,
) : FavoritesRepository {

    override val favorites: Flow<List<Brewery>> =
        dao.observeAll().map { list -> list.map { it.toDomain() } }

    override val ids: Flow<Set<String>> =
        dao.observeIds().map { it.toSet() }.distinctUntilChanged()

    override suspend fun get(id: String): Brewery? = dao.getById(id)?.toDomain()

    override suspend fun toggle(brewery: Brewery) = dao.toggle(brewery.toEntity(addedAt = clock()))

    override suspend fun refresh(brewery: Brewery) {
        val saved = dao.getById(brewery.id) ?: return
        val fresh = brewery.toEntity(addedAt = saved.addedAt)
        if (fresh != saved) dao.upsert(fresh)
    }
}
