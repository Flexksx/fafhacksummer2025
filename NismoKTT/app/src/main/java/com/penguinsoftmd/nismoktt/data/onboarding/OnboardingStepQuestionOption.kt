package com.penguinsoftmd.nismoktt.data.onboarding

import android.util.Log
import com.google.firebase.firestore.DocumentSnapshot

data class OnboardingStepQuestionOption(
    val id: String = "",
    val text: String = "",
    val impact: Int = 0,
    var isSelected: Boolean = false
) {
    companion object {
        fun fromDocument(document: DocumentSnapshot): OnboardingStepQuestionOption? {
            return try {
                OnboardingStepQuestionOption(
                    id = document.id,
                    text = document.getString("text") ?: "",
                    impact = (document.getLong("impact"))?.toInt() ?: 0
                )
            } catch (e: Exception) {
                Log.e("OnboardingOption", "Error parsing option document ${document.id}", e)
                null
            }
        }
    }
}