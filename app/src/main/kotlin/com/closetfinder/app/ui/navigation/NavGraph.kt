package com.closetfinder.app.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.closetfinder.app.SharedImageIntent
import com.closetfinder.app.ui.additem.AddItemScreen
import com.closetfinder.app.ui.backup.BackupScreen
import com.closetfinder.app.ui.detail.ItemDetailScreen
import com.closetfinder.app.ui.home.HomeScreen
import com.closetfinder.app.ui.imagesearch.ImageSearchScreen

@Composable
fun ClosetFinderNavGraph(sharedImage: SharedImageIntent? = null) {
    val navController = rememberNavController()

    LaunchedEffect(sharedImage) {
        when (sharedImage?.mode) {
            SharedImageIntent.Mode.ADD_ITEM -> navController.navigate(Routes.ADD_ITEM)
            SharedImageIntent.Mode.FIND_ITEM -> navController.navigate(Routes.IMAGE_SEARCH)
            null -> Unit
        }
    }

    NavHost(navController = navController, startDestination = Routes.HOME) {
        composable(Routes.HOME) {
            HomeScreen(
                onAddItem = { navController.navigate(Routes.ADD_ITEM) },
                onOpenItem = { id -> navController.navigate(Routes.itemDetail(id)) },
                onImageSearch = { navController.navigate(Routes.IMAGE_SEARCH) },
                onBackup = { navController.navigate(Routes.BACKUP) },
            )
        }

        composable(Routes.BACKUP) {
            BackupScreen(onBack = { navController.popBackStack() })
        }

        composable(Routes.IMAGE_SEARCH) {
            ImageSearchScreen(
                initialUri = sharedImage?.takeIf { it.mode == SharedImageIntent.Mode.FIND_ITEM }?.uri,
                onBack = { navController.popBackStack() },
                onOpenItem = { id -> navController.navigate(Routes.itemDetail(id)) },
            )
        }

        composable(Routes.ADD_ITEM) {
            AddItemScreen(
                initialUri = sharedImage?.takeIf { it.mode == SharedImageIntent.Mode.ADD_ITEM }?.uri,
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
