package com.penguinsoftmd.nismoktt.ui.nav

import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.penguinsoftmd.nismoktt.data.activities.ActivityService
import com.penguinsoftmd.nismoktt.data.preferences.PreferencesManager
import com.penguinsoftmd.nismoktt.ui.dashboard.DashboardScreen
import com.penguinsoftmd.nismoktt.ui.meltdown.MeltdownLogScreen
import com.penguinsoftmd.nismoktt.ui.onboarding.OnboardingScreen
import kotlinx.coroutines.runBlocking

@Composable
fun NavGraph(startDestination: String) {
    val context = LocalContext.current
    val navController = rememberNavController()
    val activityService = ActivityService()

    // Initialize preferences manager
    val preferencesManager = PreferencesManager(context)

    // Determine the actual start destination based on onboarding completion
    val actualStartDestination = if (startDestination == "onboarding") {
        determineStartDestination(preferencesManager)
    } else {
        startDestination
    }

    NavHost(navController = navController, startDestination = actualStartDestination) {
        composable(
            "onboarding",
            enterTransition = {
                slideInHorizontally(
                    initialOffsetX = { 1000 },
                    animationSpec = tween(300)
                )
            },
            exitTransition = {
                slideOutHorizontally(
                    targetOffsetX = { -1000 },
                    animationSpec = tween(300)
                )
            },
            popEnterTransition = {
                slideInHorizontally(
                    initialOffsetX = { -1000 },
                    animationSpec = tween(300)
                )
            },
            popExitTransition = {
                slideOutHorizontally(
                    targetOffsetX = { 1000 },
                    animationSpec = tween(300)
                )
            }
        ) {
            OnboardingScreen(
                navController = navController,
                preferencesManager = preferencesManager
            )
        }
        composable(
            "dashboard",
            enterTransition = {
                slideInHorizontally(
                    initialOffsetX = { 1000 },
                    animationSpec = tween(300)
                )
            },
            exitTransition = {
                slideOutHorizontally(
                    targetOffsetX = { -1000 },
                    animationSpec = tween(300)
                )
            },
            popEnterTransition = {
                slideInHorizontally(
                    initialOffsetX = { -1000 },
                    animationSpec = tween(300)
                )
            },
            popExitTransition = {
                slideOutHorizontally(
                    targetOffsetX = { 1000 },
                    animationSpec = tween(300)
                )
            }
        ) {
            DashboardScreen(
                navController = navController,
                preferencesManager = preferencesManager,
                activityService = activityService,
            )
        }
        composable(
            "log_meltdown",
            enterTransition = {
                slideInHorizontally(
                    initialOffsetX = { 1000 },
                    animationSpec = tween(300)
                )
            },
            exitTransition = {
                slideOutHorizontally(
                    targetOffsetX = { -1000 },
                    animationSpec = tween(300)
                )
            },
            popEnterTransition = {
                slideInHorizontally(
                    initialOffsetX = { -1000 },
                    animationSpec = tween(300)
                )
            },
            popExitTransition = {
                slideOutHorizontally(
                    targetOffsetX = { 1000 },
                    animationSpec = tween(300)
                )
            }
        ) {
            MeltdownLogScreen(navController = navController)
        }
        composable("home_route") {
            // Your main app screen goes here
        }
        // ... other routes
    }
}


// Determine if we should go to onboarding or dashboard
private fun determineStartDestination(preferencesManager: PreferencesManager): String {
    // Check if onboarding has been completed
    return runBlocking {
        if (preferencesManager.getOnboardingCompletedSnapshot()) {
            "dashboard"
        } else {
            "onboarding"
        }
    }
}
