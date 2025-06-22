package com.penguinsoftmd.nismoktt.data.activities


import android.util.Log
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore
import com.penguinsoftmd.nismoktt.domain.dsm5.Dsm5SpectrumCategory
import kotlinx.coroutines.tasks.await

class ActivityService { // Removed @Inject from constructor as per OnboardingStepQuestionService

    companion object {
        private const val TAG = "ActivityService"
    }

    private val firebaseFirestoreCollectionName = "activities" // Collection name for activities
    private val db = Firebase.firestore
    private val activitiesCollection = db.collection(firebaseFirestoreCollectionName)

    /**
     * Fetches a one-time list of all activities from Firestore.
     * This function performs a single fetch and returns the result, it does not provide real-time updates.
     *
     * @return A List of Activity objects. Returns an empty list on error.
     */
    suspend fun list(): List<Activity> {
        Log.d(TAG, "Fetching all activities (one-shot)...")
        return try {
            val querySnapshot = activitiesCollection.get().await()
            Log.d(TAG, "Successfully fetched ${querySnapshot.documents.size} activity documents.")

            querySnapshot.documents.mapNotNull { doc ->
                try {
                    // Convert Firestore document to Activity data class
                    // Ensure field names match Firestore document keys (e.g., "domain", "type", "ai_cue")
                    val domainName = doc.getString("domain") ?: run {
                        Log.e(TAG, "Missing 'domain' for doc ${doc.id}")
                        return@mapNotNull null
                    }
                    val typeName = doc.getString("type") ?: run {
                        Log.e(TAG, "Missing 'type' for doc ${doc.id}")
                        return@mapNotNull null
                    }

                    Activity(
                        id = doc.id,
                        domain = Dsm5SpectrumCategory.valueOf(domainName),
                        type = ActivityType.valueOf(typeName),
                        title = doc.getString("title") ?: "",
                        description = doc.getString("description") ?: "",
                        aiCue = doc.getString("ai_cue"), // ai_cue can be null, no default needed
                        isHighImpact = doc.getBoolean("is_high_impact") ?: false
                    )
                } catch (parseE: Exception) {
                    Log.e(TAG, "Error parsing activity document: ${doc.id}", parseE)
                    null // Return null for this document if parsing fails
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching activities from Firestore in list() function", e)
            emptyList()
        }
    }

    /**
     * Fetches activities for a specific DSM-5 domain.
     *
     * @param domain The DSM-5 category to filter activities by.
     * @return A list of activities for the given domain. Returns an empty list on error.
     */
    suspend fun getActivitiesByDomain(domain: Dsm5SpectrumCategory): List<Activity> {
        Log.d(TAG, "Fetching activities for domain: ${domain.name}")
        return try {
            val querySnapshot = activitiesCollection
                .whereEqualTo("domain", domain.name)
                .get()
                .await()
            Log.d(
                TAG,
                "Successfully fetched ${querySnapshot.documents.size} activities for domain ${domain.name}."
            )

            querySnapshot.documents.mapNotNull { doc ->
                try {
                    // Convert Firestore document to Activity data class
                    // Ensure field names match Firestore document keys (e.g., "domain", "type", "ai_cue")
                    val domainName = doc.getString("domain") ?: run {
                        Log.e(TAG, "Missing 'domain' for doc ${doc.id}")
                        return@mapNotNull null
                    }
                    val typeName = doc.getString("type") ?: run {
                        Log.e(TAG, "Missing 'type' for doc ${doc.id}")
                        return@mapNotNull null
                    }

                    Activity(
                        id = doc.id,
                        domain = Dsm5SpectrumCategory.valueOf(domainName),
                        type = ActivityType.valueOf(typeName),
                        title = doc.getString("title") ?: "",
                        description = doc.getString("description") ?: "",
                        aiCue = doc.getString("ai_cue"), // ai_cue can be null, no default needed
                        isHighImpact = doc.getBoolean("is_high_impact") ?: false
                    )
                } catch (parseE: Exception) {
                    Log.e(TAG, "Error parsing activity document: ${doc.id}", parseE)
                    null // Return null for this document if parsing fails
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching activities for domain ${domain.name}", e)
            emptyList()
        }
    }
}
