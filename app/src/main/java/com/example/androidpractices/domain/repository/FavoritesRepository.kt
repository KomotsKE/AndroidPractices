package com.example.androidpractices.domain.repository

import com.example.androidpractices.domain.model.Brewery
import kotlinx.coroutines.flow.Flow

/**
 * Избранные пивоварни. Хранятся локально целиком (а не только id),
 * поэтому список избранного и их детали доступны без интернета.
 */
interface FavoritesRepository {

    /** Избранное, последние добавленные — первыми. */
    val favorites: Flow<List<Brewery>>

    /** Только id — чтобы быстро отмечать сердечком карточки в общем списке. */
    val ids: Flow<Set<String>>

    /** Сохранённая копия пивоварни или `null`, если её нет в избранном. */
    suspend fun get(id: String): Brewery?

    /** Добавляет в избранное или убирает из него. */
    suspend fun toggle(brewery: Brewery)

    /** Обновляет сохранённую копию свежими данными с сервера (если пивоварня в избранном). */
    suspend fun refresh(brewery: Brewery)
}
