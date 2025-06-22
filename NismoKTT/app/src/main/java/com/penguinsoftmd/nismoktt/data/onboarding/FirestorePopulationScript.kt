package com.penguinsoftmd.nismoktt.data.onboarding

import android.util.Log
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore
import com.penguinsoftmd.nismoktt.domain.dsm5.Dsm5SpectrumCategory
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

// A temporary data class to hold our script's data structure
private data class QuestionData(
    val category: Dsm5SpectrumCategory,
    val title: String,
    val subtitle: String,
    val allowMultipleSelections: Boolean = false,
    val options: List<OptionData>
)

private data class OptionData(val text: String, val impact: Int)

// This list contains all the data we want to upload
private val dataToUpload = listOf(
    QuestionData(
        category = Dsm5SpectrumCategory.SOCIAL_COMMUNICATION,
        title = "When you're out with other kids, how do they reach out?",
        subtitle = "Reflect on how they naturally initiate connection in playful moments.",
        options = listOf(
            OptionData("Often starts conversations or asks for things clearly.", 0),
            OptionData("Usually only starts conversations about their specific interests.", 1),
            OptionData("Sometimes responds if spoken to, but rarely starts interactions.", 2),
            OptionData("Communicates needs mostly by leading me by the hand or using gestures.", 3),
            OptionData("Almost never starts an interaction, even for basic needs.", 4)
        )
    ),
    QuestionData(
        category = Dsm5SpectrumCategory.BEHAVIORAL_REGULATION,
        title = "When you're going to a grocery shop and a small change appears along the way, how do they cope?",
        subtitle = "Think of those unexpected moments and the care they need to adjust.",
        options = listOf(
            OptionData("Handles small changes easily, may not even notice.", 0),
            OptionData("Gets a bit unsettled but can move on with some reassurance.", 1),
            OptionData("Becomes very anxious and it's hard for them to switch gears.", 2),
            OptionData(
                "Gets extremely distressed and may engage in repetitive behaviors to cope.",
                3
            ),
            OptionData("Often has a major meltdown that can derail the entire day.", 4)
        )
    ),
    QuestionData(
        category = Dsm5SpectrumCategory.SENSORY_PROCESSING,
        title = "When exploring a lively street fair or a bustling art gallery, how do they engage with the myriad of sounds, lights, and textures?",
        subtitle = "Reflect on their comfort when immersed in an environment that is both stimulating and potentially overwhelming.",
        options = listOf(
            OptionData("They fully appreciate the vibrant sensory mix without discomfort.", 0),
            OptionData(
                "They are curious about some details while being cautious with louder or brighter stimuli.",
                1
            ),
            OptionData("They sometimes slow down and seek quieter spots to regain comfort.", 2),
            OptionData(
                "They often seem overwhelmed, preferring a break from the sensory overload.",
                3
            ),
            OptionData(
                "The intensity of sensory input usually leads to noticeable distress or withdrawal.",
                4
            )
        )
    ),
    QuestionData(
        category = Dsm5SpectrumCategory.COGNITIVE_FUNCTIONING,
        title = "When learning a new recipe or playing a fun game, how do they embrace new ideas?",
        subtitle = "Consider the thoughtful way they experiment and absorb new skills.",
        options = listOf(
            OptionData("Picks it up quickly, especially if it relates to their interests.", 0),
            OptionData("Learns best with step-by-step visual instructions and repetition.", 1),
            OptionData("Requires a lot of direct support and practice to learn new things.", 2),
            OptionData("Often struggles to apply a skill from one situation to another.", 3),
            OptionData(
                "Has significant difficulty learning new functional skills, even with support.",
                4
            )
        )
    ),
    QuestionData(
        category = Dsm5SpectrumCategory.MEDICAL_NEEDS,
        title = "When out on an unexpected day—whether it’s sunny, rainy, or breezy—how do they tend to care for themselves?",
        subtitle = "Reflect on their ability to notice and respond to physical discomfort or fatigue during a dynamic outing.",
        options = listOf(
            OptionData(
                "They intuitively recognize their needs and take a break whenever necessary.",
                0
            ),
            OptionData(
                "They sometimes ask for help when feelings of discomfort or tiredness arise.",
                1
            ),
            OptionData(
                "They occasionally seem unsure about what they need and might require gentle prompts.",
                2
            ),
            OptionData(
                "They frequently appear overwhelmed by changes, needing frequent support to manage their care.",
                3
            ),
            OptionData(
                "They often become very distressed by any physical or emotional shifts, relying entirely on others for comfort.",
                4
            )
        )
    )
)

/**
 * A one-time use script to populate the Firestore database with onboarding questions.
 * Call this function from a temporary button in your app to run it.
 */
suspend fun populateFirestore() {
    val db = Firebase.firestore
    val questionsCollection = db.collection("onboarding_steps_questions")
    var questionsAdded = 0
    var optionsAdded = 0

    Log.d("FirestoreScript", "--- Starting Firestore Population Script ---")

    try {
        coroutineScope {
            dataToUpload.forEachIndexed { index, questionData ->
                launch {
                    val questionMap = hashMapOf(
                        "title" to questionData.title,
                        "subtitle" to questionData.subtitle,
                        "allowMultipleSelections" to questionData.allowMultipleSelections,
                        "category" to questionData.category.name, // Store enum name as a String
                        "order" to index // Use the loop index for ordering
                    )

                    // Add the question document
                    val newQuestionRef = questionsCollection.add(questionMap).await()
                    questionsAdded++
                    Log.d(
                        "FirestoreScript",
                        "SUCCESS: Added question '${questionData.title}' with ID: ${newQuestionRef.id}"
                    )

                    // Add the options to the subcollection
                    questionData.options.forEach { optionData ->
                        val optionMap = hashMapOf(
                            "text" to optionData.text,
                            "impact" to optionData.impact
                        )
                        newQuestionRef.collection("options").add(optionMap).await()
                        optionsAdded++
                    }
                    Log.d(
                        "FirestoreScript",
                        "-> Added ${questionData.options.size} options for this question."
                    )
                }
            }
        }
        Log.d("FirestoreScript", "--- SCRIPT FINISHED ---")
        Log.d("FirestoreScript", "Total questions added: $questionsAdded")
        Log.d("FirestoreScript", "Total options added: $optionsAdded")

    } catch (e: Exception) {
        Log.e("FirestoreScript", "Error during Firestore population", e)
    }
}