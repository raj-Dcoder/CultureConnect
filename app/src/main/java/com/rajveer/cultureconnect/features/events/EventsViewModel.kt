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
constructor(private val repo: EventRepository, private val savedRepo: SavedEventsRepository) :
        ViewModel() {

    private val _events =
            MutableStateFlow<List<com.rajveer.cultureconnect.core.model.Event>>(emptyList())
    val events = _events.asStateFlow()

    private val _savedIds = MutableStateFlow<List<String>>(emptyList())
    val savedIds = _savedIds.asStateFlow()

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
                _events.value = repo.getApprovedEvents("Goa") 
            }
}
