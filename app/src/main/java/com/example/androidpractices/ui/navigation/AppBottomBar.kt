package com.example.androidpractices.ui.navigation

import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState

@Composable
fun AppBottomBar(
    navController: NavHostController,
    favoritesCount: Int,
) {
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = backStackEntry?.destination

    NavigationBar {
        TopLevelDestination.entries.forEach { destination ->
            val selected = currentDestination?.hierarchy?.any { it.route == destination.route } == true
            NavigationBarItem(
                selected = selected,
                onClick = { navController.navigateToTab(destination, alreadySelected = selected) },
                icon = {
                    val icon = if (selected) destination.selectedIcon else destination.unselectedIcon
                    if (destination == TopLevelDestination.FAVORITES && favoritesCount > 0) {
                        BadgedBox(badge = { Badge { Text(favoritesCount.toString()) } }) {
                            Icon(icon, contentDescription = destination.label)
                        }
                    } else {
                        Icon(icon, contentDescription = destination.label)
                    }
                },
                label = { Text(destination.label) },
            )
        }
    }
}

private fun NavHostController.navigateToTab(
    destination: TopLevelDestination,
    alreadySelected: Boolean,
) {
    if (alreadySelected) {
        // Повторное нажатие на активную вкладку — возвращаемся к её корню.
        popBackStack(destination.startRoute, inclusive = false)
        return
    }
    navigate(destination.route) {
        // Сохраняем стек каждой вкладки и восстанавливаем его при возврате.
        popUpTo(graph.findStartDestination().id) { saveState = true }
        launchSingleTop = true
        restoreState = true
    }
}
