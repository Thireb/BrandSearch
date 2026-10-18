package com.closetfinder.app.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.closetfinder.app.ui.additem.AddItemScreen
import com.closetfinder.app.ui.detail.ItemDetailScreen
import com.closetfinder.app.ui.home.HomeScreen

@Composable
fun ClosetFinderNavGraph() {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = Routes.HOME) {
        composable(Routes.HOME) {
            HomeScreen(
                onAddItem = { navController.navigate(Routes.ADD_ITEM) },
                onOpenItem = { id -> navController.navigate(Routes.itemDetail(id)) },
            )
        }

        composable(Routes.ADD_ITEM) {
            AddItemScreen(
                onBack = { navController.popBackStack() },
                onSaved = { navController.popBackStack() },
            )
        }

        composable(
            route = Routes.ITEM_DETAIL,
            arguments = listOf(navArgument("itemId") { type = NavType.LongType }),
        ) {
            ItemDetailScreen(onBack = { navController.popBackStack() })
        }
    }
}
