package com.penguinsoftmd.nismoktt.ui.onboarding

import com.penguinsoftmd.nismoktt.data.onboarding.OnboardingStepQuestion

data class OnboardingUiState(
    val isLoading: Boolean = true,
    val currentStepIndex: Int = 0,
    val questions: List<OnboardingStepQuestion> = emptyList(),
    val error: String? = null,
    val isCompleted: Boolean = false
) {
    val totalSteps: Int get() = questions.size
    val currentQuestion: OnboardingStepQuestion? get() = questions.getOrNull(currentStepIndex)
    val isLastStep: Boolean get() = currentStepIndex == totalSteps - 1 && totalSteps > 0
}
