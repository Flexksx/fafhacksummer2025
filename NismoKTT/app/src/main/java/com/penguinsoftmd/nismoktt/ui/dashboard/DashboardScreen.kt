package com.penguinsoftmd.nismoktt.ui.dashboard

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.carousel.HorizontalMultiBrowseCarousel
import androidx.compose.material3.carousel.rememberCarouselState
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.penguinsoftmd.nismoktt.data.activities.Activity
import com.penguinsoftmd.nismoktt.data.activities.ActivityService
import com.penguinsoftmd.nismoktt.data.preferences.PreferencesManager
import com.penguinsoftmd.nismoktt.domain.dsm5.Dsm5SpectrumCategory
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    preferencesManager: PreferencesManager,
    activityService: ActivityService,
    onCareAreaClick: (String) -> Unit = {}
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Child Care Dashboard") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = Color.White
                )
            )
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

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            elevation = CardDefaults.elevatedCardElevation(),
            colors = CardDefaults.elevatedCardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer
            ),
            shape = MaterialTheme.shapes.extraLarge
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "✨ You're doing amazing! ✨",
                    style = MaterialTheme.typography.headlineSmall.copy(
                        fontWeight = FontWeight.Bold
                    ),
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )

                val supportMessage = when {
                    totalScore < 5 -> "Every small step counts in your parenting journey. You're providing a loving foundation! 💙"
                    totalScore < 10 -> "You're being attentive to your child's needs. Keep up the thoughtful care! 🌟"
                    totalScore < 15 -> "Your dedication to supporting your child is really showing. You're making a difference! 💪"
                    else -> "Your comprehensive care approach is wonderful. Your child is lucky to have you! 🎉"
                }

                Text(
                    text = supportMessage,
                    style = MaterialTheme.typography.bodyLarge.copy(
                        fontWeight = FontWeight.Medium
                    ),
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )

                val supportLevel = when {
                    totalScore < 5 -> "Minimal Support"
                    totalScore < 10 -> "Some Support"
                    totalScore < 15 -> "Substantial Support"
                    else -> "Very Substantial Support"
                }

                Text(
                    text = "Current Support Level: $supportLevel",
                    style = MaterialTheme.typography.labelLarge.copy(
                        fontWeight = FontWeight.SemiBold
                    ),
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
        }

        Text(
            text = "Care Areas",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )

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

        if (activities.isNotEmpty()) {
            ActivityList(
                activities = activities,
                selectedCategoryName = formatCategoryName(selectedCategoryKey ?: "")
            )
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
            .wrapContentHeight()
            .padding(vertical = 8.dp),
        preferredItemWidth = 280.dp,
        itemSpacing = 12.dp,
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
    Card(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp), // Fixed height for consistent carousel appearance
        elevation = if (isSelected) CardDefaults.elevatedCardElevation() else CardDefaults.cardElevation(),
        shape = if (isSelected) MaterialTheme.shapes.large else MaterialTheme.shapes.medium,
        colors = if (isHighImpact) {
            CardDefaults.elevatedCardColors(
                containerColor = MaterialTheme.colorScheme.errorContainer
            )
        } else {
            if (isSelected) {
                CardDefaults.elevatedCardColors()
            } else {
                CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            }
        }
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f)
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
            val progress = score.toFloat() / 5f

            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp))
            )

            Text(
                text = "Score: $score/5",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium
            )

            if (isHighImpact) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Favorite,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Extra Care Needed",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }

            // Show support recommendations based on score
            val recommendation = when {
                score <= 1 -> "Minimal support needed in this area."
                score <= 2 -> "Some support may be beneficial."
                score <= 3 -> "Regular support recommended."
                score <= 4 -> "Substantial support needed."
                else -> "Very substantial support required."
            }

            Text(
                text = recommendation,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(top = 4.dp)
            )
        }
    }
}

@Composable
fun ActivityList(activities: List<Activity>, selectedCategoryName: String) {
    Column(modifier = Modifier.padding(top = 16.dp)) {
        Text(
            text = "Suggested Activities for $selectedCategoryName",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items(activities) { activity ->
                ActivityListItem(activity = activity)
            }
        }
    }
}

@Composable
fun ActivityListItem(activity: Activity) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = activity.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f)
                )
                if (activity.isHighImpact) {
                    Icon(
                        imageVector = Icons.Default.Favorite,
                        contentDescription = "High Impact Activity",
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
            Text(text = activity.description, style = MaterialTheme.typography.bodyMedium)
        }
    }
}

// Helper function to format category names
fun formatCategoryName(category: String): String {
    return category.replace("_", " ").split(" ").joinToString(" ") {
        it.lowercase().replaceFirstChar { char -> char.uppercase() }
    }
}