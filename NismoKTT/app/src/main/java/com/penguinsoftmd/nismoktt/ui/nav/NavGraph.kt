package com.penguinsoftmd.nismoktt.ui.nav

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.penguinsoftmd.nismoktt.data.activities.ActivityService
import com.penguinsoftmd.nismoktt.data.preferences.PreferencesManager
import com.penguinsoftmd.nismoktt.ui.dashboard.DashboardScreen
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
        composable("onboarding") {
            OnboardingScreen(
                navController = navController,
                preferencesManager = preferencesManager
            )
        }
        composable("dashboard") {
            DashboardScreen(
                preferencesManager = preferencesManager,
                activityService = activityService,
            )
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
