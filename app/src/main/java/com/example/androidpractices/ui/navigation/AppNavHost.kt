package com.example.androidpractices.ui.navigation

import androidx.compose.animation.AnimatedContentTransitionScope.SlideDirection
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Map
import androidx.compose.material.icons.rounded.Tune
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.lifecycle.Lifecycle
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.navigation
import androidx.navigation.navArgument
import com.example.androidpractices.ui.breweries.detail.BreweryDetailRoute
import com.example.androidpractices.ui.breweries.list.BreweryListRoute
import com.example.androidpractices.ui.favorites.FavoritesRoute
import com.example.androidpractices.ui.filters.FiltersRoute
import com.example.androidpractices.ui.placeholder.PlaceholderScreen

private const val ANIM_MS = 300

@Composable
fun AppNavHost(
    navController: NavHostController,
    modifier: Modifier = Modifier,
) {
    NavHost(
        navController = navController,
        startDestination = Routes.BREWERIES_GRAPH,
        modifier = modifier,
        // Переключение вкладок — мягкое затухание.
        enterTransition = { fadeIn(tween(ANIM_MS)) },
        exitTransition = { fadeOut(tween(ANIM_MS)) },
        popEnterTransition = { fadeIn(tween(ANIM_MS)) },
        popExitTransition = { fadeOut(tween(ANIM_MS)) },
    ) {
        navigation(startDestination = Routes.BREWERY_LIST, route = Routes.BREWERIES_GRAPH) {
            composable(Routes.BREWERY_LIST) { entry ->
                BreweryListRoute(
                    onBreweryClick = { id ->
                        if (entry.isResumed()) navController.navigate(Routes.breweryDetail(id))
                    },
                    onOpenFilters = {
                        if (entry.isResumed()) navController.navigate(Routes.BREWERY_FILTERS)
                    },
                )
            }
            breweryDetailDestination(Routes.BREWERY_DETAIL, navController)
            composable(
                route = Routes.BREWERY_FILTERS,
                // Фильтры выезжают снизу, как модальный экран.
                enterTransition = {
                    slideIntoContainer(SlideDirection.Up, tween(ANIM_MS)) + fadeIn(tween(ANIM_MS))
                },
                popExitTransition = {
                    slideOutOfContainer(SlideDirection.Down, tween(ANIM_MS)) + fadeOut(tween(ANIM_MS))
                },
            ) { entry ->
                FiltersRoute(
                    onClose = { if (entry.isResumed()) navController.popBackStack() },
                )
            }
        }

        navigation(startDestination = Routes.FAVORITES_LIST, route = Routes.FAVORITES_GRAPH) {
            composable(Routes.FAVORITES_LIST) { entry ->
                FavoritesRoute(
                    onBreweryClick = { id ->
                        if (entry.isResumed()) navController.navigate(Routes.favoriteDetail(id))
                    },
                )
            }
            breweryDetailDestination(Routes.FAVORITE_DETAIL, navController)
        }

        composable(Routes.MAP) {
            PlaceholderScreen(
                title = "Карта",
                icon = Icons.Rounded.Map,
                headline = "Пивоварни на карте",
                message = "Здесь появится карта со всеми пивоварнями, когда приложение начнёт получать данные из сети.",
                comingIn = "Скоро",
            )
        }

        composable(Routes.SETTINGS) {
            PlaceholderScreen(
                title = "Настройки",
                icon = Icons.Rounded.Tune,
                headline = "Настройки в пути",
                message = "Фильтры списка уже работают: кнопка в правом верхнем углу вкладки «Пивоварни». Остальные настройки — в следующих практиках.",
                comingIn = "Скоро",
            )
        }
    }
}

/** Экран деталей переиспользуется в двух вкладках — у каждой свой стек. */
private fun NavGraphBuilder.breweryDetailDestination(
    route: String,
    navController: NavHostController,
) {
    composable(
        route = route,
        arguments = listOf(navArgument(Routes.ARG_BREWERY_ID) { type = NavType.StringType }),
        enterTransition = {
            slideIntoContainer(SlideDirection.Start, tween(ANIM_MS)) + fadeIn(tween(ANIM_MS))
        },
        popExitTransition = {
            slideOutOfContainer(SlideDirection.End, tween(ANIM_MS)) + fadeOut(tween(ANIM_MS))
        },
    ) { entry ->
        BreweryDetailRoute(
            onBack = { if (entry.isResumed()) navController.popBackStack() },
        )
    }
}

/** Защита от двойных нажатий: навигация только с экрана, который сейчас на переднем плане. */
private fun NavBackStackEntry.isResumed(): Boolean =
    lifecycle.currentState == Lifecycle.State.RESUMED
