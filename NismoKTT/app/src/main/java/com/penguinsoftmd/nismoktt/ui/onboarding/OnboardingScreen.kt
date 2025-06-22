@file:OptIn(ExperimentalMaterial3ExpressiveApi::class)

package com.penguinsoftmd.nismoktt.ui.onboarding

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.LinearWavyProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.penguinsoftmd.nismoktt.data.onboarding.OnboardingStepQuestion
import com.penguinsoftmd.nismoktt.data.onboarding.OnboardingStepQuestionService

@Composable
fun OnboardingScreen(
    navController: NavController
) {
    // 1. Manually create the dependencies the ViewModel needs.
    val questionService = OnboardingStepQuestionService()

    // 2. Create the factory and pass in the dependencies.
    val viewModelFactory = OnboardingViewModelFactory(questionService)

    // 3. Get the ViewModel instance using the custom factory.
    val viewModel: OnboardingViewModel = viewModel(factory = viewModelFactory)

    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    // Listen for navigation events (this part doesn't change)
    LaunchedEffect(Unit) {
        viewModel.eventFlow.collect { event ->
            when (event) {
                // This is the only branch needed for your sealed class event
                is OnboardingEvent.NavigateToHome -> {
                    navController.navigate("home_route") { // Or your main app route
                        popUpTo("onboarding") { inclusive = true }
                    }
                }
            }
        }
    }

    // The Scaffold no longer has a bottomBar
    Scaffold { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentAlignment = Alignment.Center
        ) {
            when {
                uiState.isLoading -> CircularProgressIndicator()
                uiState.error != null -> Text(text = uiState.error!!)
                uiState.isCompleted -> { // ADDED: The new "Done" state
                    DoneContent(onContinueClicked = { viewModel.navigateToHome() })
                }

                uiState.currentQuestion != null -> {
                    OnboardingStepContent(
                        question = uiState.currentQuestion!!,
                        currentStep = uiState.currentStepIndex + 1,
                        totalSteps = uiState.totalSteps,
                        onOptionSelected = viewModel::onOptionSelected
                    )
                }
            }
        }
    }
}

@Composable
fun OnboardingOptionButton(text: String, isSelected: Boolean, onClick: () -> Unit) {
    // Reduced horizontal padding from 16.dp to 8.dp.
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
            .clickable { onClick() }
            .padding(horizontal = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        RadioButton(
            selected = isSelected,
            onClick = { onClick() }
        )
        Spacer(modifier = Modifier.width(16.dp))
        Text(text = text, style = MaterialTheme.typography.bodyLarge)
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun OnboardingStepContent(
    question: OnboardingStepQuestion,
    currentStep: Int,
    totalSteps: Int,
    onOptionSelected: (String) -> Unit
) {
    var selectedOption by remember { mutableStateOf<String?>(null) }
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        val progressValue = currentStep / totalSteps.toFloat()
        LinearWavyProgressIndicator(
            progress = { progressValue },
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
        )
        Spacer(Modifier.height(24.dp))
        Text(
            text = question.title,
            style = MaterialTheme.typography.headlineMedium,
            textAlign = TextAlign.Center
        )
        question.subtitle?.let {
            Spacer(Modifier.height(8.dp))
            Text(
                text = it,
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center
            )
        }
        Spacer(Modifier.height(40.dp))
        LazyColumn(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(question.options, key = { it.id }) { option ->
                OnboardingOptionButton(
                    text = option.text,
                    isSelected = option.id == selectedOption,
                    onClick = { selectedOption = option.id }
                )
            }
        }
        Spacer(Modifier.height(24.dp))
        Button(
            onClick = { selectedOption?.let { onOptionSelected(it) } },
            shapes = ButtonDefaults.shapes(),
            enabled = selectedOption != null,
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
            elevation = ButtonDefaults.buttonElevation(defaultElevation = 8.dp),
            border = null,
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Submit")
        }
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun DoneContent(onContinueClicked: () -> Unit) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier.padding(16.dp)
    ) {
        Text("All done!", style = MaterialTheme.typography.headlineMedium)
        Spacer(Modifier.height(16.dp))
        Text("Thank you for completing the onboarding.")
        Spacer(Modifier.height(32.dp))
        // Updated Continue button using the expressive large Button API.
        Button(
            onClick = onContinueClicked,
            shapes = ButtonDefaults.shapes(), // round shape
            enabled = true,
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
            elevation = ButtonDefaults.buttonElevation(defaultElevation = 8.dp),
            border = null,
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Continue to App")
        }
    }
}
