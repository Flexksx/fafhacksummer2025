package com.penguinsoftmd.nismoktt.data.onboarding

import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore
import kotlinx.coroutines.tasks.await

class OnboardingStepQuestionService {
    val firebaseFirestoreCollectionName = "onboarding_steps_questions"
    private val db = Firebase.firestore
    private val onboardingStepsCollection = db.collection(firebaseFirestoreCollectionName)

    suspend fun list(): List<OnboardingStepQuestion> {
        return try {
            onboardingStepsCollection.get().await().documents.map { document ->
                document.toObject(OnboardingStepQuestion::class.java)
                    ?: throw Exception("Failed to parse OnboardingStepQuestion")
            }
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }

}