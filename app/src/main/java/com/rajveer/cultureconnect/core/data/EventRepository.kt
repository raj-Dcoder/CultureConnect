package com.rajveer.cultureconnect.core.data

import com.google.firebase.firestore.FirebaseFirestore
import com.rajveer.cultureconnect.core.model.Event
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class EventRepository @Inject constructor(
    private val db: FirebaseFirestore
) {
    private val events = db.collection("event")

    suspend fun getApprovedEvents(city: String = "Goa"): List<Event> {
        val snapshot = events
            .whereEqualTo("city", city)
            .whereEqualTo("isApproved", true)
            .get()
            .await()

        return snapshot.toObjects(Event::class.java)
    }
}
