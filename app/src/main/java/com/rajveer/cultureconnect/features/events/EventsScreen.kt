package com.rajveer.cultureconnect.features.events

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.rajveer.cultureconnect.core.model.Event
import java.text.SimpleDateFormat
import java.util.*

/**
 * Main Events Screen matching the approved design
 * 
 * Layout:
 * - Header: "Discover Events"
 * - Filter chips (Date: Today/This Week/All)
 * - Category chips (Music/Food/Cultural/Festival)
 * - Event cards in scrollable list
 */
@Composable
fun EventsScreen(
    viewModel: EventsViewModel = hiltViewModel(),
    onEventClick: (String) -> Unit
) {
    // Collect state from ViewModel
    val events by viewModel.events.collectAsState()
    val savedIds by viewModel.savedIds.collectAsState()

    // Local state for filters (will connect to ViewModel later)
    val selectedDateFilter by viewModel.selectedDateFilter.collectAsState()
    val selectedCategories by viewModel.selectedCategories.collectAsState()

    // loading state
    val isLoading by viewModel.isLoading.collectAsState()

    val errorMessage by viewModel.errorMessage.collectAsState()
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Header
        Text(
            text = "Discover Events",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 20.dp)
        )

        // Date Filter Chips Row
        LazyRow(
            modifier = Modifier.padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(listOf("Today", "This Week", "All")) { filter ->
                DateFilterChip(
                    label = filter,
                    selected = selectedDateFilter == filter,
                    onClick = { viewModel.setDateFilter(filter) }
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Category Filter Chips Row
        LazyRow(
            modifier = Modifier.padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(listOf("Music", "Food", "Cultural", "Festival")) { category ->
                CategoryFilterChip(
                    label = category,
                    selected = selectedCategories.contains(category),
                    onClick = { viewModel.toggleCategory(category) }
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        when {
            // Error state
            errorMessage != null -> {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Error,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(48.dp)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = errorMessage ?: "Unknown error",
                        color = MaterialTheme.colorScheme.error
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(onClick = { viewModel.loadEvents() }) {
                        Text("Retry")
                    }
                }
            }

            // Loading state
            isLoading->{
                Box(modifier = Modifier.fillMaxWidth().weight(1f), contentAlignment = Alignment.Center){
                    CircularProgressIndicator()
                }
            }
            
            // Empty event List
            events.isEmpty()->{
                Box(modifier = Modifier.fillMaxWidth().weight(1f), contentAlignment = Alignment.Center){
                    Text("No events found")
                }
            }
            // Event Lists
            else->{
                LazyColumn(
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(events) { event ->
                        EventCard(
                            event = event,
                            isSaved = savedIds.contains(event.id),
                            onSaveClick = { viewModel.toggleSave(event.id) },
                            onClick = { onEventClick(event.id) }
                        )
                    }
                }
            }
        }
    }
}

/**
 * Date Filter Chip (Single-select)
 * Selected: Purple background with white text
 * Unselected: White background with gray border
 */
@Composable
fun DateFilterChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    FilterChip(
        selected = selected,
        onClick = onClick,
        label = {
            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium
            )
        },
        colors = FilterChipDefaults.filterChipColors(
            selectedContainerColor = MaterialTheme.colorScheme.primary,
            selectedLabelColor = Color.White
        )
    )
}

/**
 * Category Filter Chip (Multi-select)
 * Same styling as Date chip but allows multiple selections
 */
@Composable
fun CategoryFilterChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    FilterChip(
        selected = selected,
        onClick = onClick,
        label = {
            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium
            )
        },
        colors = FilterChipDefaults.filterChipColors(
            selectedContainerColor = MaterialTheme.colorScheme.primary,
            selectedLabelColor = Color.White
        )
    )
}

/**
 * Event Card Component
 * 
 * Structure:
 * - Event image (16:9 ratio)
 * - Bookmark icon (top-right overlay on image)
 * - Event title (max 2 lines)
 * - Date/time row with calendar icon
 * - Location row with pin icon
 */
@Composable
fun EventCard(
    event: Event,
    isSaved: Boolean,
    onSaveClick: () -> Unit,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column {
            // Image with bookmark overlay
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
            ) {
                // Event Image
                AsyncImage(
                    model = event.imageUrl,
                    contentDescription = event.title,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )

                // Bookmark Icon (top-right corner)
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(12.dp)
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(Color.White)
                        .clickable { onSaveClick() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (isSaved) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                        contentDescription = "Save event",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            // Event Details
            Column(
                modifier = Modifier.padding(12.dp)
            ) {
                // Event Title
                Text(
                    text = event.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Date & Time Row
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.CalendarToday,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = formatEventDate(event.startAt),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                // Location Row
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.LocationOn,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "${event.areaName}, ${event.city}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}

/**
 * Format timestamp to readable date string
 * Format: "Sat, Oct 28 • 6:00 PM"
 */
private fun formatEventDate(timestamp: Long): String {
    val sdf = SimpleDateFormat("EEE, MMM d • h:mm a", Locale.getDefault())
    return sdf.format(Date(timestamp))
}