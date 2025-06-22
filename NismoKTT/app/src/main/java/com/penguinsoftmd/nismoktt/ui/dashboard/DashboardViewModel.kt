package com.penguinsoftmd.nismoktt.ui.dashboard


import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.penguinsoftmd.nismoktt.data.preferences.PreferencesManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

data class DashboardUiState(
    val spectrumLevel: String = "",
    val totalScore: Int = 0,
    val affections: Map<String, Int> = emptyMap(),
    val isLoading: Boolean = true
)

class DashboardViewModel(
    private val preferencesManager: PreferencesManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(DashboardUiState())
    val uiState: StateFlow<DashboardUiState> = _uiState.asStateFlow()

    init {
        loadDashboardData()
    }

    private fun loadDashboardData() {
        val totalScore = preferencesManager.getTotalImpactScore()
        val categoryScores = preferencesManager.getCategoryScores()
        val spectrum = determineSpectrumLevel(totalScore)

        _uiState.update {
            it.copy(
                spectrumLevel = spectrum,
                totalScore = totalScore,
                affections = categoryScores,
                isLoading = false
            )
        }
    }

    private fun determineSpectrumLevel(score: Int): String {
        return when {
            score > 20 -> "High Spectrum" // Example thresholds
            score > 10 -> "Medium Spectrum"
            else -> "Low Spectrum"
        }
    }
}

class DashboardViewModelFactory(
    private val preferencesManager: PreferencesManager
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(DashboardViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return DashboardViewModel(preferencesManager) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
