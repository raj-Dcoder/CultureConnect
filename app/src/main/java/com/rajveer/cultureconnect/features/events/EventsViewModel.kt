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
import com.rajveer.cultureconnect.core.model.Event

@HiltViewModel
class EventsViewModel
@Inject
constructor(
    private val repo: EventRepository, 
    private val savedRepo: SavedEventsRepository
) : ViewModel() {
    // Filtered events (for EventsScreen with filters)
    private val _events = MutableStateFlow<List<Event>>(emptyList())
    val events = _events.asStateFlow()

    // ALL events (unfiltered, for EventDetailScreen)
    private val _allEvents = MutableStateFlow<List<Event>>(emptyList())
    val allEvents = _allEvents.asStateFlow()

    private val _savedIds = MutableStateFlow<List<String>>(emptyList())
    val savedIds = _savedIds.asStateFlow()

    // filter states
    private val _selectedDateFilter = MutableStateFlow<String>("This Week")
    val selectedDateFilter = _selectedDateFilter.asStateFlow()

    private val _selectedCategories = MutableStateFlow<Set<String>>(emptySet())
    val selectedCategories = _selectedCategories.asStateFlow()
    
    // loading state
    private val _isLoading = MutableStateFlow<Boolean>(true)  // What should initial value be?
    val isLoading = _isLoading.asStateFlow()

    // error message state
    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage = _errorMessage.asStateFlow()

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

    fun loadEvents() =
    
            viewModelScope.launch { 
                _isLoading.value = true
                _errorMessage.value = null //clear pervious error

                try {
                    android.util.Log.d("EventsViewModel", "🔍 Starting to load events for Bhubaneswar...")
                    // Get ALL events (no filters)
                    val allEventsList = repo.getApprovedEvents("Bhubaneswar", emptySet(), "All")
                    _allEvents.value = allEventsList
                    
                    // Get FILTERED events (with current filters)
                    val filteredEvents = repo.getApprovedEvents("Bhubaneswar", selectedCategories.value, selectedDateFilter.value)
                    _events.value = filteredEvents
                    
                } catch (e: Exception) {
                    _errorMessage.value = e.message ?: "Unknown error occurred"
                    android.util.Log.e("EventsViewModel", "❌ Error loading events: ${e.message}", e)
                
                }finally{
                    _isLoading.value = false
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
