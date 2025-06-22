package com.penguinsoftmd.nismoktt

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import com.penguinsoftmd.nismoktt.ui.nav.NavGraph
import com.penguinsoftmd.nismoktt.ui.theme.NismoKTTTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            NismoKTTTheme {
                Box(modifier = Modifier.fillMaxSize()) {
                    NavGraph(startDestination = "onboarding")


                }
            }
        }
    }
}