package com.scorller.nav

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import androidx.navigation.NavType
import com.scorller.data.ShareIntentHolder
import com.scorller.ui.additem.AddItemScreen
import com.scorller.ui.deck.SwipeDeckScreen
import com.scorller.ui.home.HomeScreen
import com.scorller.ui.nameentry.DisplayNameScreen
import com.scorller.ui.results.ResultsScreen
import com.scorller.ui.sessionhub.SessionHubScreen
import com.scorller.ui.sharetarget.ShareTargetScreen

private object Routes {
    const val HOME = "home"
    const val SHARE_TARGET = "shareTarget"
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

private fun NavBackStackEntry.sessionId() = arguments?.getString("sessionId").orEmpty()

@Composable
fun ScorllerNavGraph(navController: NavHostController = rememberNavController()) {
    val pendingSharedLink by ShareIntentHolder.pendingLink.collectAsStateWithLifecycle()

    LaunchedEffect(pendingSharedLink) {
        if (pendingSharedLink != null) {
            navController.navigate(Routes.SHARE_TARGET) {
                launchSingleTop = true
            }
        }
    }

    NavHost(navController = navController, startDestination = Routes.HOME) {
        composable(Routes.SHARE_TARGET) {
            val link = pendingSharedLink
            if (link != null) {
                ShareTargetScreen(
                    sharedLink = link,
                    onSessionChosen = { sessionId ->
                        navController.navigate(Routes.addItem(sessionId)) {
                            popUpTo(Routes.HOME)
                        }
                    },
                    onGoHome = {
                        navController.navigate(Routes.HOME) {
                            popUpTo(Routes.HOME) { inclusive = true }
                        }
                    }
                )
            }
        }
        composable(Routes.HOME) {
            HomeScreen(
                onSessionReady = { sessionId ->
                    navController.navigate(Routes.displayName(sessionId))
                },
                onOpenExistingSession = { sessionId ->
                    navController.navigate(Routes.sessionHub(sessionId))
                }
            )
        }
        composable(
            Routes.DISPLAY_NAME,
            arguments = listOf(navArgument("sessionId") { type = NavType.StringType })
        ) { backStackEntry ->
            val sessionId = backStackEntry.sessionId()
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
            val sessionId = backStackEntry.sessionId()
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
            val sessionId = backStackEntry.sessionId()
            AddItemScreen(
                sessionId = sessionId,
                onItemAdded = { navController.popBackStack() }
            )
        }
        composable(
            Routes.SWIPE_DECK,
            arguments = listOf(navArgument("sessionId") { type = NavType.StringType })
        ) { backStackEntry ->
            val sessionId = backStackEntry.sessionId()
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
            val sessionId = backStackEntry.sessionId()
            ResultsScreen(sessionId = sessionId)
        }
    }
}
