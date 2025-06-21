package com.penguinsoftmd.nismoktt.data.onboarding

data class OnboardingStepQuestion(
    val id: String,
    val title: String,
    val subtitle: String? = null,
    val options: List<OnboardingStepQuestionOption> = emptyList()
)
