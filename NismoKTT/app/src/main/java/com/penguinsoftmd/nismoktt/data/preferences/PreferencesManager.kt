package com.penguinsoftmd.nismoktt.data.preferences

import android.content.Context
import android.content.SharedPreferences
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class PreferencesManager(context: Context) {
    private val sharedPreferences: SharedPreferences =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    private val gson = Gson()

    // Private MutableStateFlows to hold the current values of preferences.
    // Initialized with the current values from SharedPreferences.
    private val _totalImpactScore = MutableStateFlow(getTotalImpactScoreInternal())
    private val _categoryScores = MutableStateFlow(getCategoryScoresInternal())
    private val _onboardingCompleted = MutableStateFlow(isOnboardingCompletedInternal())

    // Public StateFlows for external observation (e.g., by Compose UI).
    // .asStateFlow() provides a read-only StateFlow.
    val totalImpactScore: StateFlow<Int> = _totalImpactScore.asStateFlow()
    val categoryScores: StateFlow<Map<String, Int>> = _categoryScores.asStateFlow()
    val onboardingCompleted: StateFlow<Boolean> = _onboardingCompleted.asStateFlow()

    // Listener for SharedPreferences changes that happen outside this class instance.
    private val preferenceChangeListener =
        SharedPreferences.OnSharedPreferenceChangeListener { sharedPrefs, key ->
            when (key) {
                KEY_TOTAL_IMPACT_SCORE -> _totalImpactScore.value = getTotalImpactScoreInternal()
                KEY_CATEGORY_SCORES -> _categoryScores.value = getCategoryScoresInternal()
                KEY_ONBOARDING_COMPLETED -> _onboardingCompleted.value =
                    isOnboardingCompletedInternal()
            }
        }

    init {
        // Register the listener when the PreferencesManager is initialized
        sharedPreferences.registerOnSharedPreferenceChangeListener(preferenceChangeListener)
    }

    // --- Internal functions to read from SharedPreferences ---
    // These are used for initial setup of StateFlows and by the change listener.
    private fun getTotalImpactScoreInternal(): Int {
        return sharedPreferences.getInt(KEY_TOTAL_IMPACT_SCORE, 0)
    }

    private fun getCategoryScoresInternal(): Map<String, Int> {
        val json = sharedPreferences.getString(KEY_CATEGORY_SCORES, null)
        return if (json != null) {
            val type = object : TypeToken<Map<String, Int>>() {}.type
            gson.fromJson(json, type)
        } else {
            emptyMap()
        }
    }

    private fun isOnboardingCompletedInternal(): Boolean {
        return sharedPreferences.getBoolean(KEY_ONBOARDING_COMPLETED, false)
    }

    // --- Public functions to update preferences and notify observers ---
    fun completeOnboarding(totalImpactScore: Int, categoryScores: Map<String, Int>) {
        val categoryScoresJson = gson.toJson(categoryScores)
        sharedPreferences.edit()
            .putBoolean(KEY_ONBOARDING_COMPLETED, true)
            .putInt(KEY_TOTAL_IMPACT_SCORE, totalImpactScore)
            .putString(KEY_CATEGORY_SCORES, categoryScoresJson)
            .apply() // Use apply() for async save

        // Immediately update the backing MutableStateFlows to reflect changes in UI
        _totalImpactScore.value = totalImpactScore
        _categoryScores.value = categoryScores
        _onboardingCompleted.value = true
    }

    // Optional: If you still need direct snapshot getters (not recommended for Compose reactivity)
    fun getOnboardingCompletedSnapshot(): Boolean = isOnboardingCompletedInternal()
    fun getTotalImpactScoreSnapshot(): Int = getTotalImpactScoreInternal()
    fun getCategoryScoresSnapshot(): Map<String, Int> = getCategoryScoresInternal()
    fun getTotalImpactScore(): Int {
        return _totalImpactScore.value // Expose the current value of totalImpactScore
    }

    fun getCategoryScores(): Map<String, Int> {
        return _categoryScores.value // Expose the current value of categoryScores
    }


    companion object {
        private const val PREFS_NAME = "nismo_ktt_prefs"
        private const val KEY_ONBOARDING_COMPLETED = "onboarding_completed"
        private const val KEY_TOTAL_IMPACT_SCORE = "total_impact_score"
        private const val KEY_CATEGORY_SCORES = "category_scores"
    }
}