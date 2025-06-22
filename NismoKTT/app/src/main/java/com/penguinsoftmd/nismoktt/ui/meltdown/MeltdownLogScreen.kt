package com.penguinsoftmd.nismoktt.ui.meltdown

import android.util.Log
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController

private const val MELTDOWN_LOG_ENDPOINT_URL = "https://your-backend.com/api/meltdowns"
private const val TAG = "MeltdownLogScreen"

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MeltdownLogScreen(navController: NavController) {
    var location by remember { mutableStateOf("") }
    var conditions by remember { mutableStateOf("") }
    var time by remember { mutableStateOf("") }
    var precedingSigns by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Log a Meltdown") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                "Use this form to log details about a challenging behavior or meltdown. This helps in identifying patterns.",
                style = MaterialTheme.typography.bodyMedium
            )

            OutlinedTextField(
                value = location,
                onValueChange = { location = it },
                label = { Text("Location") },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("e.g., Home, Supermarket, School") }
            )

            OutlinedTextField(
                value = time,
                onValueChange = { time = it },
                label = { Text("Time of event") },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("e.g., 4:30 PM, After lunch") }
            )

            OutlinedTextField(
                value = conditions,
                onValueChange = { conditions = it },
                label = { Text("Conditions / Triggers") },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("e.g., Loud noise, Change in routine") },
                minLines = 3
            )

            OutlinedTextField(
                value = precedingSigns,
                onValueChange = { precedingSigns = it },
                label = { Text("Preceding Signs (if any)") },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("e.g., Hand flapping, Pacing, Vocalizations") },
                minLines = 3
            )

            Button(
                onClick = {
                    // Placeholder for backend submission
                    val meltdownData = """
                        Location: $location
                        Time: $time
                        Conditions: $conditions
                        Preceding Signs: $precedingSigns
                    """.trimIndent()

                    Log.d(TAG, "Submitting to $MELTDOWN_LOG_ENDPOINT_URL")
                    Log.d(TAG, "Data: \n$meltdownData")

                    // Here you would use a network client like Retrofit or Ktor
                    // to send the data to your backend.

                    // After submission, navigate back
                    navController.popBackStack()
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Log Event")
            }
        }
    }
}
