package com.penguinsoftmd.nismoktt.data.onboarding

import android.util.Log
import com.google.firebase.firestore.DocumentSnapshot
import com.penguinsoftmd.nismoktt.domain.dsm5.Dsm5SpectrumCategory

data class OnboardingStepQuestion(
    val id: String = "",
    val title: String = "",
    val subtitle: String? = null,
    val category: Dsm5SpectrumCategory,
    val allowMultipleSelections: Boolean = false,
    val options: List<OnboardingStepQuestionOption> = emptyList()
) {
    companion object {
        fun fromDocument(document: DocumentSnapshot): OnboardingStepQuestion? {
            return try {
                OnboardingStepQuestion(
                    id = document.id,
                    title = document.getString("title") ?: "",
                    subtitle = document.getString("subtitle"),
                    category = Dsm5SpectrumCategory.valueOf(
                        document.getString("category") ?: "MEDICAL_NEEDS"
                    ),
                )
            } catch (e: Exception) {
                Log.e("OnboardingQuestion", "Error parsing question document ${document.id}", e)
                null
            }
        }
    }
}