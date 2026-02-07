package com.rajveer.cultureconnect.features.events

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
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

@Composable
fun EventDetailScreen(
    eventId: String,
    viewModel: EventsViewModel = hiltViewModel(),
    onBack: () -> Unit,
) {
    val context = LocalContext.current
    val event = viewModel.events.collectAsState().value.firstOrNull { it.id == eventId }

    event?.let {
        Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {

            AsyncImage(
                model = it.imageUrl,
                contentDescription = it.title,
                modifier = Modifier.fillMaxWidth().height(220.dp)
            )

            Text(it.title, style = MaterialTheme.typography.titleLarge)
            Text("📍 ${it.areaName}", modifier = Modifier.padding(vertical = 4.dp))

            Text(it.description, modifier = Modifier.padding(top = 8.dp))

            Spacer(Modifier.height(20.dp))

            Button(onClick = {
                val gmmIntentUri =
                    "geo:0,0?q=${it.areaName} Goa".toUri()
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
