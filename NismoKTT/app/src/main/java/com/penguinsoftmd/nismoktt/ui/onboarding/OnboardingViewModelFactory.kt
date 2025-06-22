package com.penguinsoftmd.nismoktt.ui.onboarding


import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.penguinsoftmd.nismoktt.data.onboarding.OnboardingStepQuestionService

class OnboardingViewModelFactory(
    private val questionService: OnboardingStepQuestionService
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(OnboardingViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return OnboardingViewModel(questionService) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}