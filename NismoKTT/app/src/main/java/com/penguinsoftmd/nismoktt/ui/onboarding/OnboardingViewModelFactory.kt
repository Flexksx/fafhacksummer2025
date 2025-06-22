package com.penguinsoftmd.nismoktt.ui.onboarding


import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.penguinsoftmd.nismoktt.data.onboarding.OnboardingStepQuestionService
import com.penguinsoftmd.nismoktt.data.preferences.PreferencesManager

class OnboardingViewModelFactory(
    private val questionService: OnboardingStepQuestionService,
    private val preferencesManager: PreferencesManager
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(OnboardingViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return OnboardingViewModel(questionService, preferencesManager) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}