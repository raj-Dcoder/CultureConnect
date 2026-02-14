package com.rajveer.cultureconnect.features.explore

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rajveer.cultureconnect.core.data.EventRepository
import com.rajveer.cultureconnect.core.model.Event
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.Calendar
import javax.inject.Inject

@HiltViewModel
class ExploreViewModel @Inject constructor(
    private val eventRepo: EventRepository
) : ViewModel() {

    private val _topEvents = MutableStateFlow<List<Event>>(emptyList())
    val topEvents = _topEvents.asStateFlow()

    init {
        loadTopEvents()
    }

    private fun loadTopEvents() = viewModelScope.launch {
        try {
            // Reuse EventRepository — fetch approved events for Bhubaneswar, take top 3
            val events = eventRepo.getApprovedEvents(
                city = "Bhubaneswar",
                dateFilter = "This Month"
            )
            _topEvents.value = events.take(3)
        } catch (e: Exception) {
            android.util.Log.e("ExploreVM", "Failed to load events", e)
        }
    }

    fun getGreeting(): String {
        val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
        return when {
            hour < 11 -> "Good Morning"
            hour < 15 -> "Good Afternoon"
            else -> "Good Evening"
        }
    }
}
