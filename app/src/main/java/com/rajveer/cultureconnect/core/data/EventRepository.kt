package com.rajveer.cultureconnect.core.data

import com.google.firebase.firestore.FirebaseFirestore
import com.rajveer.cultureconnect.core.model.Event
import kotlinx.coroutines.tasks.await
import java.util.Date
import javax.inject.Inject

class EventRepository @Inject constructor(
    private val db: FirebaseFirestore
) {
    private val events = db.collection("events")

    /** Fetch a single event by its Firestore document ID */
    suspend fun getEventById(eventId: String): Event? {
        return try {
            val doc = events.document(eventId).get().await()
            doc.toObject(Event::class.java)?.copy(id = doc.id)
        } catch (e: Exception) {
            android.util.Log.e("EventRepository", "Error fetching event $eventId", e)
            null
        }
    }

    suspend fun getApprovedEvents(city: String = "Bhubaneswar", categories: Set<String> = emptySet(), dateFilter: String = "This Week"): List<Event> {
        android.util.Log.d("EventRepository", "🔍 Querying Firestore for city: $city")
        android.util.Log.d("EventRepository", "🔍 Querying Firestore for categories: $categories")
        android.util.Log.d("EventRepository", "🔍 Querying Firestore for dateFilter: $dateFilter")
        val now = System.currentTimeMillis()
        android.util.Log.d("EventRepository", "⏰ Current timestamp (now): $now = ${Date(now)}")
        android.util.Log.d("EventRepository", "🔍 Filtering: endAt >= $now")

        val snapshot = events
            .whereEqualTo("city", city)
            .whereEqualTo("isApproved", true)
            // .whereGreaterThanOrEqualTo("endAt", now)
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
        val startOfToday = java.time.LocalDate.now().atStartOfDay(java.time.ZoneId.systemDefault()).toInstant().toEpochMilli()
        val endOfToday = startOfToday + 24 * 60 * 60 * 1000L
        val endOfWeek = now + 7 * 24 * 60 * 60 * 1000L
        // Filter by date
        val dateFiltered = when (dateFilter) {
            "Today" -> {
                // Show events happening today (started before/today AND ending today/after)
                categoryFiltered.filter { event ->
                    event.endAt >= startOfToday && event.startAt <= endOfToday
                }
            }
            "This Week" -> {
                // Show events in the next 7 days
                categoryFiltered.filter { event ->
                    event.endAt >= now && event.startAt <= endOfWeek
                }
            }
            "This Weekend" -> {
                // Calculate this Saturday and Sunday
                val calendar = java.util.Calendar.getInstance()
                val today = calendar.get(java.util.Calendar.DAY_OF_WEEK)
                val daysUntilSaturday = (java.util.Calendar.SATURDAY - today + 7) % 7
                val daysUntilSunday = daysUntilSaturday + 1
                
                val saturdayStart = startOfToday + (daysUntilSaturday * 24 * 60 * 60 * 1000L)
                val sundayEnd = startOfToday + (daysUntilSunday * 24 * 60 * 60 * 1000L) + (24 * 60 * 60 * 1000L)
                
                categoryFiltered.filter { event ->
                    event.endAt >= saturdayStart && event.startAt <= sundayEnd
                }
            }
            "This Month" -> {
                // Show events in the next 30 days
                val endOfMonth = now + (30 * 24 * 60 * 60 * 1000L)
                categoryFiltered.filter { event ->
                    event.endAt >= now && event.startAt <= endOfMonth
                }
            }
            else -> categoryFiltered  // "All" - no date filtering
        }
        android.util.Log.d("EventRepository", "✅ Filtered to ${dateFiltered.size} events")
        return dateFiltered
    }
}
