package com.rajveer.cultureconnect.features.events

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import androidx.compose.runtime.collectAsState
import androidx.core.net.toUri
import androidx.compose.ui.platform.LocalContext
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.ui.graphics.Color

@Composable
fun EventDetailScreen(
    eventId: String,
    viewModel: EventsViewModel = hiltViewModel(),
    onBack: () -> Unit,
) {
    val context = LocalContext.current  // what is the logic to use this //
    val event = viewModel.allEvents.collectAsState().value.firstOrNull { it.id == eventId }

    android.util.Log.d("EventDetail", "🔍 Looking for ID: $eventId")
    android.util.Log.d("EventDetail", "📋 Events in VM: ${viewModel.allEvents.value.size}")
    android.util.Log.d("EventDetail", "✅ Found: ${event?.title ?: "NULL"}")
    event?.let {
        Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {

            AsyncImage(
                model = it.imageUrl,
                contentDescription = it.title,
                modifier = Modifier.fillMaxWidth().height(220.dp)
            )

            Text(it.title, style = MaterialTheme.typography.titleLarge)
            Text("📍 ${it.areaName}", modifier = Modifier.padding(vertical = 4.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                event.tags.forEach{tag->
                    FilterChip(
                        selected = false,
                        onClick = { /* TODO: Define tag click behavior */ },
                        label = {
                            Text(
                                text = tag,
                                style = MaterialTheme.typography.bodyMedium
                            )
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primary,
                            selectedLabelColor = Color.White
                        )
                    )
                }
            }
            Text(it.description, modifier = Modifier.padding(top = 8.dp))
            Spacer(Modifier.height(20.dp))
            Button(onClick = {
                val gmmIntentUri =
                    "geo:0,0?q=${it.areaName} Goa".toUri()/*TODO: Goa should be handled well*/ 
                val mapIntent = android.content.Intent(android.content.Intent.ACTION_VIEW, gmmIntentUri)
                mapIntent.setPackage("com.google.android.apps.maps")
                context.startActivity(mapIntent)
                onBack()
            }) {
                Text("Open in Maps")
            }
        }
    }
}
