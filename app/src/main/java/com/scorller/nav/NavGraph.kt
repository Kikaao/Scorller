package com.scorller.nav

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import androidx.navigation.NavType
import com.scorller.ui.additem.AddItemScreen
import com.scorller.ui.deck.SwipeDeckScreen
import com.scorller.ui.home.HomeScreen
import com.scorller.ui.nameentry.DisplayNameScreen
import com.scorller.ui.results.ResultsScreen
import com.scorller.ui.sessionhub.SessionHubScreen

private object Routes {
    const val HOME = "home"
    const val DISPLAY_NAME = "displayName/{sessionId}"
    const val SESSION_HUB = "sessionHub/{sessionId}"
    const val ADD_ITEM = "addItem/{sessionId}"
    const val SWIPE_DECK = "swipeDeck/{sessionId}"
    const val RESULTS = "results/{sessionId}"

    fun displayName(sessionId: String) = "displayName/$sessionId"
    fun sessionHub(sessionId: String) = "sessionHub/$sessionId"
    fun addItem(sessionId: String) = "addItem/$sessionId"
    fun swipeDeck(sessionId: String) = "swipeDeck/$sessionId"
    fun results(sessionId: String) = "results/$sessionId"
}

@Composable
fun ScorllerNavGraph(navController: NavHostController = rememberNavController()) {
    NavHost(navController = navController, startDestination = Routes.HOME) {
        composable(Routes.HOME) {
            HomeScreen(
                onSessionReady = { sessionId ->
                    navController.navigate(Routes.displayName(sessionId))
                }
            )
        }
        composable(
            Routes.DISPLAY_NAME,
            arguments = listOf(navArgument("sessionId") { type = NavType.StringType })
        ) { backStackEntry ->
            val sessionId = backStackEntry.arguments?.getString("sessionId").orEmpty()
            DisplayNameScreen(
                sessionId = sessionId,
                onConfirmed = {
                    navController.navigate(Routes.sessionHub(sessionId)) {
                        popUpTo(Routes.HOME)
                    }
                }
            )
        }
        composable(
            Routes.SESSION_HUB,
            arguments = listOf(navArgument("sessionId") { type = NavType.StringType })
        ) { backStackEntry ->
            val sessionId = backStackEntry.arguments?.getString("sessionId").orEmpty()
            SessionHubScreen(
                sessionId = sessionId,
                onAddItem = { navController.navigate(Routes.addItem(sessionId)) },
                onSwipe = { navController.navigate(Routes.swipeDeck(sessionId)) },
                onResults = { navController.navigate(Routes.results(sessionId)) }
            )
        }
        composable(
            Routes.ADD_ITEM,
            arguments = listOf(navArgument("sessionId") { type = NavType.StringType })
        ) { backStackEntry ->
            val sessionId = backStackEntry.arguments?.getString("sessionId").orEmpty()
            AddItemScreen(
                sessionId = sessionId,
                onItemAdded = { navController.popBackStack() }
            )
        }
        composable(
            Routes.SWIPE_DECK,
            arguments = listOf(navArgument("sessionId") { type = NavType.StringType })
        ) { backStackEntry ->
            val sessionId = backStackEntry.arguments?.getString("sessionId").orEmpty()
            SwipeDeckScreen(
                sessionId = sessionId,
                onDone = {
                    navController.navigate(Routes.results(sessionId)) {
                        popUpTo(Routes.sessionHub(sessionId))
                    }
                }
            )
        }
        composable(
            Routes.RESULTS,
            arguments = listOf(navArgument("sessionId") { type = NavType.StringType })
        ) { backStackEntry ->
            val sessionId = backStackEntry.arguments?.getString("sessionId").orEmpty()
            ResultsScreen(sessionId = sessionId)
        }
    }
}
