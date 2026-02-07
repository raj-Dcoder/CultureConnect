package com.rajveer.cultureconnect.core.data

import com.google.firebase.firestore.FirebaseFirestore
import com.rajveer.cultureconnect.core.model.Highlight
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class HighlightRepository @Inject constructor(
    private val db: FirebaseFirestore
) {
    private val highlights = db.collection("highlights")

    suspend fun getHighlights(city: String = "Goa"): List<Highlight> {
        val snapshot = highlights.whereEqualTo("city", city).get().await()
        return snapshot.toObjects(Highlight::class.java)
    }
}
