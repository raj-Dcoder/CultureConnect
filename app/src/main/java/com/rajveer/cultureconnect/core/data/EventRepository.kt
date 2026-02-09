package com.rajveer.cultureconnect.core.data

import com.google.firebase.firestore.FirebaseFirestore
import com.rajveer.cultureconnect.core.model.Event
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class EventRepository @Inject constructor(
    private val db: FirebaseFirestore
) {
    private val events = db.collection("events")

    suspend fun getApprovedEvents(city: String = "Bhubaneswar", categories: Set<String> = emptySet(), dateFilter: String = "This Week"): List<Event> {
        android.util.Log.d("EventRepository", "🔍 Querying Firestore for city: $city")
        android.util.Log.d("EventRepository", "🔍 Querying Firestore for categories: $categories")
        android.util.Log.d("EventRepository", "🔍 Querying Firestore for dateFilter: $dateFilter")
        
        val snapshot = events
            .whereEqualTo("city", city)
            .whereEqualTo("isApproved", true)
            .get()
            .await()
        
        android.util.Log.d("EventRepository", "📊 Firestore returned ${snapshot.size()} documents")
        
        val allEventsList = snapshot.toObjects(Event::class.java)
        android.util.Log.d("EventRepository", "✅ Converted to ${allEventsList.size} Event objects")

        // Filter by category
        val categoryFiltered = if (categories.isEmpty()) {
            allEventsList  // No category filter, show all
        } else {
            allEventsList.filter { event ->
                event.category in categories
            }
        }
        /*Calculate timestamp boundaries*/
        val now = System.currentTimeMillis()
        val startOfToday = java.time.LocalDate.now().atStartOfDay(java.time.ZoneId.systemDefault()).toInstant().toEpochMilli()
        val endOfToday = startOfToday + 24 * 60 * 60 * 1000L
        val endOfWeek = now + 7 * 24 * 60 * 60 * 1000L
        // Filter by date
        val dateFiltered = when (dateFilter) {
            "Today" -> {
                categoryFiltered.filter { event ->
                    event.startAt >= startOfToday && event.startAt <= endOfToday
                }
            }
            "This Week" -> {
                categoryFiltered.filter { event ->
                    event.startAt <= endOfWeek && event.endAt >= now
                }
            }
            else -> categoryFiltered  
        }
        android.util.Log.d("EventRepository", "✅ Filtered to ${dateFiltered.size} events")
        
        return dateFiltered
    }
}
