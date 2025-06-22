package com.penguinsoftmd.nismoktt.ui.nav

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.penguinsoftmd.nismoktt.ui.onboarding.OnboardingScreen

@Composable
fun NavGraph(startDestination: String) {
    val navController = rememberNavController()
    NavHost(navController = navController, startDestination = startDestination) {
        composable("onboarding") {
            OnboardingScreen(navController = navController)
        }
        composable("home_route") {
            // Your main app screen goes here
        }
        // ... other routes
    }
}