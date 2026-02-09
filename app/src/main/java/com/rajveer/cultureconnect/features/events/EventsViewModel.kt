package com.rajveer.cultureconnect.features.events

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rajveer.cultureconnect.core.data.EventRepository
import com.rajveer.cultureconnect.core.data.SavedEventsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

@HiltViewModel
class EventsViewModel
@Inject
constructor(
    private val repo: EventRepository, 
    private val savedRepo: SavedEventsRepository
) : ViewModel() {

    private val _events =
            MutableStateFlow<List<com.rajveer.cultureconnect.core.model.Event>>(emptyList())
    val events = _events.asStateFlow()

    private val _savedIds = MutableStateFlow<List<String>>(emptyList())
    val savedIds = _savedIds.asStateFlow()

    // filter states
    private val _selectedDateFilter = MutableStateFlow<String>("This Week")
    val selectedDateFilter = _selectedDateFilter.asStateFlow()

    private val _selectedCategories = MutableStateFlow<Set<String>>(emptySet())
    val selectedCategories = _selectedCategories.asStateFlow()
    
    init {
        loadEvents()
        loadSavedEvents()
    }


    fun loadSavedEvents() = viewModelScope.launch {
        _savedIds.value = savedRepo.getSavedEventIds()
    }

    fun toggleSave(eventId: String) =
            viewModelScope.launch {
                if (savedIds.value.contains(eventId)) {
                    savedRepo.removeEvent(eventId)
                } else {
                    savedRepo.saveEvent(eventId)
                }
                loadSavedEvents()
            }

    private fun loadEvents() =
    
            viewModelScope.launch { 
                try {
                    android.util.Log.d("EventsViewModel", "🔍 Starting to load events for Bhubaneswar...")
                    val fetchedEvents = repo.getApprovedEvents("Bhubaneswar",selectedCategories.value, selectedDateFilter.value)
                    
                    android.util.Log.d("EventsViewModel", "✅ Fetched ${fetchedEvents.size} events")
                    fetchedEvents.forEach { event ->
                        android.util.Log.d("EventsViewModel", "  📅 ${event.title} - ${event.city}")
                    }
                    _events.value = fetchedEvents
                } catch (e: Exception) {
                    android.util.Log.e("EventsViewModel", "❌ Error loading events: ${e.message}", e)
                }
            }
           
    fun setDateFilter(filter: String) {
        _selectedDateFilter.value = filter
        loadEvents()
    }

    fun toggleCategory(category: String) {
        if (_selectedCategories.value.contains(category)) {
            _selectedCategories.value = _selectedCategories.value - category 
        } else {
            _selectedCategories.value = _selectedCategories.value + category
        }
        loadEvents()
    }
}
