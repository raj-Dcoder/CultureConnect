package com.rajveer.cultureconnect.features.events

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.rajveer.cultureconnect.core.model.Event

@Composable
fun EventDetailScreen(
    eventId: String,
    viewModel: EventsViewModel = hiltViewModel(),
    onBack: () -> Unit,
    onLetsGo: (String) -> Unit = {},
) {
    val context = LocalContext.current
    var isSaved by remember { mutableStateOf(false) }

    // Try to find event in ViewModel's cache first, then fetch from Firestore
    val cachedEvent = viewModel.allEvents.collectAsState().value.firstOrNull { it.id == eventId }
    var fetchedEvent by remember { mutableStateOf<Event?>(null) }
    var isLoading by remember { mutableStateOf(cachedEvent == null) }

    // If not in cache, fetch directly from Firestore
    LaunchedEffect(eventId) {
        if (cachedEvent == null) {
            isLoading = true
            fetchedEvent = viewModel.getEventById(eventId)
            isLoading = false
        }
    }

    val event = cachedEvent ?: fetchedEvent

    // Loading state
    if (isLoading && event == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                CircularProgressIndicator()
                Spacer(modifier = Modifier.height(8.dp))
                Text("Loading event details...")
            }
        }
        return
    }

    // Event not found
    if (event == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("Event not found")
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

            // Top Bar Overlay
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .padding(top = 24.dp),
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
                        // Fallback if maps app not installed
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("View on Map")
            }
            Spacer(modifier = Modifier.height(12.dp))

            // Let's Go button
            Button(
                onClick = { onLetsGo("${event.areaName}, ${event.city}") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            ) {
                Icon(
                    Icons.Default.ArrowForward,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Let's Go", fontWeight = FontWeight.Bold)
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}
