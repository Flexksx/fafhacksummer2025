package com.penguinsoftmd.nismoktt.ui.home

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.penguinsoftmd.nismoktt.data.preferences.PreferencesManager
import com.penguinsoftmd.nismoktt.ui.dashboard.DashboardUiState
import com.penguinsoftmd.nismoktt.ui.dashboard.DashboardViewModel
import com.penguinsoftmd.nismoktt.ui.dashboard.DashboardViewModelFactory

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen() {
    val context = LocalContext.current
    val preferencesManager = remember { PreferencesManager(context) }
    val viewModelFactory = DashboardViewModelFactory(preferencesManager)
    val viewModel: DashboardViewModel = viewModel(factory = viewModelFactory)
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Child Care Dashboard") })
        }
    ) { paddingValues ->
        if (uiState.isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            DashboardContent(
                modifier = Modifier.padding(paddingValues),
                uiState = uiState
            )
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun DashboardContent(modifier: Modifier = Modifier, uiState: DashboardUiState) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Spectrum Assessment", style = MaterialTheme.typography.headlineSmall)
        Spacer(Modifier.height(8.dp))
        Card(modifier = Modifier.fillMaxWidth()) {
            Column(Modifier.padding(16.dp)) {
                Text("Overall Result:", style = MaterialTheme.typography.titleMedium)
                Text(
                    text = uiState.spectrumLevel,
                    style = MaterialTheme.typography.displaySmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Text("Based on a total score of ${uiState.totalScore}")
            }
        }

        Spacer(Modifier.height(24.dp))

        Text("Affections Breakdown", style = MaterialTheme.typography.headlineSmall)
        Spacer(Modifier.height(8.dp))

        if (uiState.affections.isNotEmpty()) {
            val pagerState = rememberPagerState(pageCount = { uiState.affections.size })
            val affectionsList = uiState.affections.toList()

            HorizontalPager(
                state = pagerState,
                contentPadding = PaddingValues(horizontal = 32.dp),
                modifier = Modifier.fillMaxWidth()
            ) { page ->
                AffectionCard(
                    affectionName = affectionsList[page].first,
                    score = affectionsList[page].second
                )
            }
        } else {
            Text("No affection data available.")
        }
    }
}

@Composable
fun AffectionCard(affectionName: String, score: Int) {
    val isHighImpact = score > 5 // Example threshold for high impact

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = affectionName.replaceFirstChar { it.uppercase() },
                style = MaterialTheme.typography.titleLarge,
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(8.dp))
            Text(
                "Impact Score: $score",
                style = MaterialTheme.typography.bodyLarge
            )
            if (isHighImpact) {
                Spacer(Modifier.height(16.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Favorite,
                        contentDescription = "High Impact",
                        tint = Color.Red
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        "Extra Care Recommended",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.Red
                    )
                }
            }
        }
    }
}
