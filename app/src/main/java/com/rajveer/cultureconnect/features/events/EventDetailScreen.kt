package com.rajveer.cultureconnect.features.events

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.horizontalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.SuggestionChipDefaults
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.ui.Alignment

@Composable
fun EventDetailScreen(
    eventId: String,
    viewModel: EventsViewModel = hiltViewModel(),
    onBack: () -> Unit,
) {
    val context = LocalContext.current
    val event = viewModel.allEvents.collectAsState().value.firstOrNull { it.id == eventId }
    var isSaved by remember { mutableStateOf(false) } // Dummy save state for UI demo

    // If event is not found (loading or error), show simple message
    if (event == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("Loading event details...")
        }
        return
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        // Hero Image Section with Top Bar
        Box(modifier = Modifier.fillMaxWidth().height(250.dp)) {
            AsyncImage(
                model = event.imageUrl,
                contentDescription = event.title,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
            // Gradient or Scrim could be added here for text readability if title was over image
            
            // Top Bar Overlay
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .padding(top = 24.dp), // Check status bar handling
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                IconButton(
                    onClick = onBack,
                    colors = IconButtonDefaults.iconButtonColors(
                        containerColor = Color.Black.copy(alpha = 0.4f)
                    )
                ) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                }

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    IconButton(
                        onClick = {
                            val shareIntent = android.content.Intent(android.content.Intent.ACTION_SEND).apply {
                                type = "text/plain"
                                putExtra(android.content.Intent.EXTRA_SUBJECT, event.title)
                                putExtra(android.content.Intent.EXTRA_TEXT, "Check out this event: ${event.title} at ${event.areaName}!")
                            }
                            context.startActivity(android.content.Intent.createChooser(shareIntent, "Share Event"))
                        },
                        colors = IconButtonDefaults.iconButtonColors(
                            containerColor = Color.Black.copy(alpha = 0.4f)
                        )
                    ) {
                        Icon(Icons.Default.Share, contentDescription = "Share", tint = Color.White)
                    }
                    IconButton(
                        onClick = { isSaved = !isSaved },
                        colors = IconButtonDefaults.iconButtonColors(
                            containerColor = Color.Black.copy(alpha = 0.4f)
                        )
                    ) {
                        Icon(
                            imageVector = if (isSaved) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                            contentDescription = "Save",
                            tint = if (isSaved) Color.Red else Color.White
                        )
                    }
                }
            }
        }

        // Content Section
        Column(modifier = Modifier.padding(16.dp)) {
            // Category Badge
            SuggestionChip(
                onClick = { },
                label = { Text(event.category.uppercase()) },
                colors = SuggestionChipDefaults.suggestionChipColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer,
                    labelColor = MaterialTheme.colorScheme.onSecondaryContainer
                ),
                border = null
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(event.title, style = MaterialTheme.typography.headlineMedium)
            Text("📍 ${event.areaName}, ${event.city}", style = MaterialTheme.typography.bodyLarge, color = Color.Gray)

            Spacer(modifier = Modifier.height(16.dp))

            // Date Logic
            val startDate = java.util.Date(event.startAt)
            val endDate = java.util.Date(event.endAt)
            val dateFormatter = java.text.SimpleDateFormat("EEE, MMM d", java.util.Locale.getDefault())
            val timeFormatter = java.text.SimpleDateFormat("h:mm a", java.util.Locale.getDefault())

            val dateText = if (dateFormatter.format(startDate) == dateFormatter.format(endDate)) {
                 "${dateFormatter.format(startDate)} • ${timeFormatter.format(startDate)} - ${timeFormatter.format(endDate)}"
            } else {
                 "${dateFormatter.format(startDate)} • ${timeFormatter.format(startDate)} - ${dateFormatter.format(endDate)} • ${timeFormatter.format(endDate)}"
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.DateRange, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                Spacer(modifier = Modifier.padding(4.dp))
                Text(dateText, style = MaterialTheme.typography.bodyLarge)
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Tags
            if (event.tags.isNotEmpty()) {
                Row(
                     modifier = Modifier.horizontalScroll(rememberScrollState()),
                     horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                     event.tags.forEach { tag ->
                         SuggestionChip(onClick = {}, label = { Text(tag) })
                     }
                }
                Spacer(modifier = Modifier.height(16.dp))
            }
            
            Text("About", style = MaterialTheme.typography.titleMedium)
            Text(event.description, style = MaterialTheme.typography.bodyMedium)
            
            Spacer(modifier = Modifier.height(24.dp))
            
            Button(
                onClick = {
                    val gmmIntentUri = if (event.latitude != 0.0 && event.longitude != 0.0) {
                        "geo:${event.latitude},${event.longitude}?q=${event.latitude},${event.longitude}(${event.title})".toUri()
                    } else {
                        "geo:0,0?q=${event.areaName}, ${event.city}".toUri()
                    }
                    val mapIntent = android.content.Intent(android.content.Intent.ACTION_VIEW, gmmIntentUri)
                    mapIntent.setPackage("com.google.android.apps.maps")
                    try {
                        context.startActivity(mapIntent)
                    } catch (e: Exception) {
                        // Fallback logic if maps app is not installed could go here
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("View on Map")
            }
            Spacer(modifier = Modifier.height(32.dp)) // Bottom padding
        }
    }
}
