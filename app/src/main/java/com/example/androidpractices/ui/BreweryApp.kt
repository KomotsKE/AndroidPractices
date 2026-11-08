package com.example.androidpractices.ui

import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.compose.rememberNavController
import com.example.androidpractices.di.ServiceLocator
import com.example.androidpractices.ui.navigation.AppBottomBar
import com.example.androidpractices.ui.navigation.AppNavHost

/** Корень приложения: Single Activity + нижняя навигация + NavHost. */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun BreweryApp() {
    val navController = rememberNavController()
    val favoriteIds by ServiceLocator.favoritesRepository.ids
        .collectAsStateWithLifecycle(initialValue = emptySet())

    Scaffold(
        bottomBar = { AppBottomBar(navController, favoritesCount = favoriteIds.size) },
        // Верхние отступы (status bar) обрабатывают сами экраны — шапка деталей рисуется под ним.
        contentWindowInsets = WindowInsets(0.dp),
    ) { innerPadding ->
        AppNavHost(
            navController = navController,
            modifier = Modifier
                .padding(innerPadding)
                .consumeWindowInsets(innerPadding),
        )
    }
}
