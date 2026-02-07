package com.rajveer.cultureconnect.features.explore

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rajveer.cultureconnect.core.model.Highlight
import com.rajveer.cultureconnect.core.data.HighlightRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ExploreViewModel @Inject constructor(
    private val repo: HighlightRepository
) : ViewModel() {

    private val _highlights = MutableStateFlow<List<Highlight>>(emptyList())
    val highlights = _highlights.asStateFlow()

    init {
        loadHighlights()
    }

    private fun loadHighlights() = viewModelScope.launch {
        _highlights.value = repo.getHighlights("Goa")
        // TODO: Log to print the size of the list
        Log.d("ExploreViewModel", "Highlights size: ${_highlights.value.size}")
    }
}
