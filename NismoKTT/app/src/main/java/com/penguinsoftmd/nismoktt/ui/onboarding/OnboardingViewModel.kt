package com.penguinsoftmd.nismoktt.ui.onboarding

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.penguinsoftmd.nismoktt.data.onboarding.OnboardingStepQuestionService
import com.penguinsoftmd.nismoktt.data.preferences.PreferencesManager
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

sealed class OnboardingEvent {
    data object NavigateToHome : OnboardingEvent()
    data object NavigateToDashboard : OnboardingEvent()
}

class OnboardingViewModel(
    private val questionService: OnboardingStepQuestionService,
    private val preferencesManager: PreferencesManager
) : ViewModel() {

    companion object {
        private const val TAG = "OnboardingViewModel"
    }

    // Holds the entire state of the screen. The UI will observe this.
    private val _uiState = MutableStateFlow(OnboardingUiState())
    val uiState: StateFlow<OnboardingUiState> = _uiState.asStateFlow()

    // Used for one-time events like navigation.
    private val _eventFlow = MutableSharedFlow<OnboardingEvent>()
    val eventFlow = _eventFlow.asSharedFlow()

    init {
        loadQuestions()
    }

    /**
     * Called by the UI when an option is tapped.
     * This now controls the entire flow of selecting an answer and advancing.
     */
    fun onOptionSelected(optionId: String) {
        // Check if an option for the current question has already been selected to prevent double taps.
        val alreadyAnswered = _uiState.value.currentQuestion?.options?.any { it.isSelected } == true
        if (alreadyAnswered || _uiState.value.isLoading) return

        viewModelScope.launch {
            // 1. Update the UI state to show the selection.
            updateSelectionState(optionId)

            // 2. Pause briefly so the user can see their selection.
            delay(400) // A short delay for better UX.

            // 3. Decide whether to go to the next step or finish.
            if (_uiState.value.isLastStep) {
                finishOnboarding()
            } else {
                advanceToNextStep()
            }
        }
    }


    fun navigateToHome() {
        viewModelScope.launch {
            _eventFlow.emit(OnboardingEvent.NavigateToHome)
        }
    }

    fun navigateToDashboard() {
        viewModelScope.launch {
            _eventFlow.emit(OnboardingEvent.NavigateToDashboard)
        }
    }

    private fun loadQuestions() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val questions = questionService.list()
            if (questions.isNotEmpty()) {
                _uiState.update {
                    it.copy(isLoading = false, questions = questions)
                }
            } else {
                _uiState.update {
                    it.copy(isLoading = false, error = "Could not load onboarding steps.")
                }
            }
        }
    }

    private fun updateSelectionState(optionId: String) {
        _uiState.update { currentState ->
            val currentQuestion = currentState.currentQuestion ?: return@update currentState

            val updatedOptions = currentQuestion.options.map { option ->
                // Mark the selected option and ensure others are not selected (single choice).
                option.copy(isSelected = option.id == optionId)
            }

            val updatedQuestions = currentState.questions.toMutableList().apply {
                this[currentState.currentStepIndex] = currentQuestion.copy(options = updatedOptions)
            }
            currentState.copy(questions = updatedQuestions)
        }
    }

    private fun advanceToNextStep() {
        _uiState.update { it.copy(currentStepIndex = it.currentStepIndex + 1) }
    }

    private fun finishOnboarding() {
        // Calculate the total impact score from all selected answers
        val totalImpactScore = _uiState.value.questions.sumOf { question ->
            question.options.filter { it.isSelected }.sumOf { it.impact }
        }

        // Calculate scores by category
        val categoryScores = mutableMapOf<String, Int>()

        // Group questions by category and sum the scores
        _uiState.value.questions.forEach { question ->
            val categoryKey = question.category.toString()
            val categoryScore = question.options.filter { it.isSelected }.sumOf { it.impact }
            categoryScores[categoryKey] = (categoryScores[categoryKey] ?: 0) + categoryScore
        }

        Log.d(TAG, "Onboarding finished with total impact score: $totalImpactScore")
        Log.d(TAG, "Category scores: $categoryScores")

        // Save the results if preferences manager is available
        viewModelScope.launch {
            preferencesManager.completeOnboarding(totalImpactScore, categoryScores)
        }

        // Update the state to show the "Done" screen in the UI
        _uiState.update { it.copy(isCompleted = true) }
    }
}