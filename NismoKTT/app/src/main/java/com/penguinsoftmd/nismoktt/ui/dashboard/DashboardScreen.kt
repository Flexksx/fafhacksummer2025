package com.penguinsoftmd.nismoktt.ui.dashboard

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.ThumbUp
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarDefaults.topAppBarColors
import androidx.compose.material3.carousel.HorizontalMultiBrowseCarousel
import androidx.compose.material3.carousel.rememberCarouselState
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.penguinsoftmd.nismoktt.data.activities.Activity
import com.penguinsoftmd.nismoktt.data.activities.ActivityService
import com.penguinsoftmd.nismoktt.data.activities.ActivityType
import com.penguinsoftmd.nismoktt.data.preferences.PreferencesManager
import com.penguinsoftmd.nismoktt.domain.dsm5.Dsm5SpectrumCategory
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    navController: NavController,
    preferencesManager: PreferencesManager,
    activityService: ActivityService,
    onCareAreaClick: (String) -> Unit = {}
) {
    val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior(rememberTopAppBarState())
    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Dashboard") },
                colors = topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    scrolledContainerColor = MaterialTheme.colorScheme.surfaceContainer
                ),
                scrollBehavior = scrollBehavior
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { navController.navigate("log_meltdown") }) {
                Icon(Icons.Default.Add, contentDescription = "Log a behavior event")
            }
        }
    ) { paddingValues ->
        DashboardContent(
            modifier = Modifier.padding(paddingValues),
            preferencesManager = preferencesManager,
            activityService = activityService,
            onCareAreaClick = onCareAreaClick
        )
    }
}

@Composable
fun DashboardContent(
    modifier: Modifier = Modifier,
    preferencesManager: PreferencesManager,
    activityService: ActivityService,
    onCareAreaClick: (String) -> Unit = {}
) {
    val totalScore by preferencesManager.totalImpactScore.collectAsState(initial = 0)
    val categoryScores by preferencesManager.categoryScores.collectAsState(initial = emptyMap())
    var selectedCategoryKey by remember { mutableStateOf<String?>(null) }
    var activities by remember { mutableStateOf<List<Activity>>(emptyList()) }
    val coroutineScope = rememberCoroutineScope()

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            SupportSummaryCard(
                totalScore = totalScore,
                modifier = Modifier.padding(horizontal = 16.dp)
            )
        }

        item {
            Text(
                text = "Care Areas",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 16.dp)
            )
        }

        item {
            // The Box with negative padding that caused the crash has been removed.
            // The CategoryCarousel now spans the full width, and its own internal
            // contentPadding will align its items with the rest of the content.
            CategoryCarousel(
                categoryScores = categoryScores,
                onCareAreaClick = { categoryKey ->
                    selectedCategoryKey = if (selectedCategoryKey == categoryKey) {
                        null // Deselect if clicked again
                    } else {
                        categoryKey
                    }

                    coroutineScope.launch {
                        activities = if (selectedCategoryKey != null) {
                            try {
                                val domain = Dsm5SpectrumCategory.valueOf(selectedCategoryKey!!)
                                activityService.getActivitiesByDomain(domain)
                            } catch (e: IllegalArgumentException) {
                                // Handle case where categoryKey is not a valid Dsm5SpectrumCategory
                                emptyList()
                            }
                        } else {
                            emptyList()
                        }
                    }
                }
            )
        }

        if (activities.isNotEmpty()) {
            item {
                Text(
                    text = "Suggested Activities for ${formatCategoryName(selectedCategoryKey ?: "")}",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
            }
            items(activities, key = { it.id }) { activity ->
                ActivityListItem(
                    activity = activity,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
            }
        }
    }
}

@Composable
fun SupportSummaryCard(totalScore: Int, modifier: Modifier = Modifier) {
    ElevatedCard(
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Icon(
                imageVector = Icons.Default.ThumbUp,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(48.dp)
            )
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = "You're doing great!",
                    style = MaterialTheme.typography.titleLarge,
                )

                val supportMessage = when {
                    totalScore < 5 -> "Every small step counts. You're providing a loving foundation."
                    totalScore < 10 -> "You're being attentive to your child's needs. Keep it up!"
                    totalScore < 15 -> "Your dedication is making a real difference."
                    else -> "Your comprehensive care is wonderful. Your child is lucky to have you!"
                }

                Text(
                    text = supportMessage,
                    style = MaterialTheme.typography.bodyMedium,
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategoryCarousel(
    categoryScores: Map<String, Int>,
    onCareAreaClick: (String) -> Unit = {}
) {
    val categories = categoryScores.entries.toList()

    if (categories.isEmpty()) {
        Text(
            text = "No care areas data available",
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(16.dp)
        )
        return
    }

    val carouselState = rememberCarouselState { categories.size }

    HorizontalMultiBrowseCarousel(
        state = carouselState,
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentHeight(),
        preferredItemWidth = 240.dp, // Adjusted for better visibility of multiple items
        itemSpacing = 8.dp, // Reduced spacing
        contentPadding = PaddingValues(horizontal = 16.dp)
    ) { index ->
        val category = categories[index]
        // Determine if this card is in the center/selected position
        val isSelected = index == carouselState.currentItem
        CategoryCard(
            title = formatCategoryName(category.key),
            score = category.value,
            isHighImpact = category.value >= 3, // Threshold for high impact
            isSelected = isSelected,
            onClick = { onCareAreaClick(category.key) }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategoryCard(
    title: String,
    score: Int,
    isHighImpact: Boolean,
    isSelected: Boolean,
    onClick: () -> Unit = {}
) {
    ElevatedCard( // Changed to ElevatedCard for consistency
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentHeight(), // Dynamic height
        elevation = if (isSelected) CardDefaults.elevatedCardElevation(defaultElevation = 8.dp) else CardDefaults.elevatedCardElevation(),
        shape = MaterialTheme.shapes.large, // Consistent large shape
        colors = if (isHighImpact) {
            CardDefaults.elevatedCardColors(
                containerColor = MaterialTheme.colorScheme.errorContainer,
                contentColor = MaterialTheme.colorScheme.onErrorContainer
            )
        } else {
            if (isSelected) {
                CardDefaults.elevatedCardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            } else {
                CardDefaults.elevatedCardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainerHighest,
                    contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp) // Increased spacing
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f, fill = false)
                )

                if (isHighImpact) {
                    Icon(
                        imageVector = Icons.Default.Favorite,
                        contentDescription = "Needs extra care",
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }

            // Show a progress indicator for the score
            // Assuming max score is 5
            val progress = (score.toFloat() / 5f).coerceIn(0f, 1f)

            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(MaterialTheme.shapes.small) // Use theme shape
            )

            Text(
                text = "Score: $score/5",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium
            )

            // Simplified: Removed recommendation text to declutter the card.
            // The score and high-impact indicator provide enough information at a glance.
        }
    }
}


@Composable
fun ActivityListItem(activity: Activity, modifier: Modifier = Modifier) {
    ElevatedCard( // Changed to ElevatedCard
        modifier = modifier.fillMaxWidth()
    ) {
        Row(
            Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Icon(
                imageVector = iconForActivityType(activity.type),
                contentDescription = activity.type.name,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(40.dp)
            )
            Column(
                verticalArrangement = Arrangement.spacedBy(4.dp),
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = activity.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                )
                Text(text = activity.description, style = MaterialTheme.typography.bodyMedium)
            }
            if (activity.isHighImpact) {
                Icon(
                    imageVector = Icons.Default.Favorite,
                    contentDescription = "High Impact Activity",
                    tint = MaterialTheme.colorScheme.error,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
}

// Helper function to provide an icon for an activity type
@Composable
private fun iconForActivityType(type: ActivityType): ImageVector {
    return when (type.name) {
        "AT_HOME" -> Icons.Default.Home
        "OUTDOOR" -> Icons.Default.Star
        "GENERAL_STRATEGY" -> Icons.Default.Build
        else -> Icons.Default.Person// A sensible default
    }
}

// Helper function to format category names
fun formatCategoryName(category: String): String {
    return category.replace("_", " ").split(" ").joinToString(" ") {
        it.lowercase().replaceFirstChar { char -> char.uppercase() }
    }
}