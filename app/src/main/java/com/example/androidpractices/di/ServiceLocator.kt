package com.example.androidpractices.di

import com.example.androidpractices.data.repository.BreweryRepository
import com.example.androidpractices.data.repository.FavoritesRepository
import com.example.androidpractices.data.repository.MockBreweryRepository

/**
 * Простейший DI: единственные экземпляры зависимостей на весь процесс.
 * Когда появится сеть, здесь достаточно подменить реализацию репозитория.
 */
object ServiceLocator {
    val breweryRepository: BreweryRepository by lazy { MockBreweryRepository() }
    val favoritesRepository: FavoritesRepository by lazy { FavoritesRepository() }
}
