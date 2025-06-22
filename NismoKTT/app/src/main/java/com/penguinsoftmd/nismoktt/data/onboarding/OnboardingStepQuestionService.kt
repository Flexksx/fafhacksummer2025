package com.penguinsoftmd.nismoktt.data.onboarding

import android.util.Log
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.tasks.await

class OnboardingStepQuestionService {

    companion object {
        private const val TAG = "OnboardingService"
    }

    private val firebaseFirestoreCollectionName = "onboarding_steps_questions"
    private val db = Firebase.firestore
    private val onboardingStepsCollection = db.collection(firebaseFirestoreCollectionName)

    suspend fun list(): List<OnboardingStepQuestion> {
        Log.d(TAG, "Fetching parent question documents...")

        return try {
            // 1. Fetch all the main question documents first.
            val questionDocuments = onboardingStepsCollection.get().await().documents
            Log.d(TAG, "Successfully fetched ${questionDocuments.size} parent documents.")

            // Use a coroutineScope to perform parallel fetches for the options
            coroutineScope {
                // 2. For each question document, start an ASYNCHRONOUS task to fetch its options.
                val deferredQuestions = questionDocuments.map { questionDoc ->
                    async {
                        // Parse the parent question data (without options)
                        val question = OnboardingStepQuestion.fromDocument(questionDoc)

                        if (question != null) {
                            val optionsSnapshot = onboardingStepsCollection
                                .document(question.id)
                                .collection("options") // <-- Accessing the subcollection
                                .get()
                                .await()

                            val options = optionsSnapshot.documents.mapNotNull { optionDoc ->
                                OnboardingStepQuestionOption.fromDocument(optionDoc)
                            }
                            Log.d(
                                TAG,
                                "Fetched ${options.size} options for question '${question.title}'"
                            )

                            // 4. Return the final, assembled question with its options.
                            question.copy(options = options.sortedBy { it.impact }) // Sorting by impact as you suggested
                        } else {
                            null
                        }
                    }
                }
                // 5. Wait for all the asynchronous tasks to complete and filter out any nulls.
                deferredQuestions.awaitAll().filterNotNull()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching questions and their options from Firestore", e)
            emptyList()
        }
    }
}