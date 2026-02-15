package com.rajveer.cultureconnect.core.data

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class SavedEventsRepository @Inject constructor(
    private val auth: FirebaseAuth,
    private val db: FirebaseFirestore
) {
    private val users get() = db.collection("users")

    private fun savedEventsRef() =
        auth.currentUser?.let { users.document(it.uid).collection("savedEvents") }

    suspend fun saveEvent(eventId: String) {
        savedEventsRef()?.document(eventId)?.set(mapOf("savedAt" to System.currentTimeMillis()))?.await()
    }

    suspend fun removeEvent(eventId: String) {
        savedEventsRef()?.document(eventId)?.delete()?.await()
    }

    suspend fun isEventSaved(eventId: String): Boolean {
        val doc = savedEventsRef()?.document(eventId)?.get()?.await()
        return doc?.exists() ?: false
    }

    suspend fun getSavedEventIds(): List<String> {
        val snapshot = savedEventsRef()?.get()?.await() ?: return emptyList()
        return snapshot.documents.map { it.id }
    }

    suspend fun clearAll() {
        val snapshot = savedEventsRef()?.get()?.await() ?: return
        snapshot.documents.forEach { it.reference.delete().await() }
    }
}
