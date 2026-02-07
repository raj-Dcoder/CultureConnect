package com.rajveer.cultureconnect.features.events

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.rajveer.cultureconnect.core.model.Event
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun EventsScreen(
    viewModel: EventsViewModel = hiltViewModel(),
    onEventClick: (String) -> Unit
) {
    val list by viewModel.events.collectAsState()
    val saved by viewModel.savedIds.collectAsState()

    LazyColumn(modifier = Modifier.padding(12.dp)) {
        items(list) { item ->
            EventCard(
                item, onClick = { onEventClick(item.id) },
                isSaved = saved.contains(item.id),
                onSaveClick = { viewModel.toggleSave(item.id) }
            )
        }
    }
}

@Composable
fun EventCard(event: Event,  isSaved: Boolean, onSaveClick: () -> Unit, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .clickable { onClick() }
    ) {
        Column {
            AsyncImage(
                model = event.imageUrl,
                contentDescription = event.title,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(150.dp)
            )
            Row(
                modifier = Modifier.fillMaxWidth().padding(8.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(event.title)

                IconButton(onClick = onSaveClick) {
                    Icon(
                        imageVector = if (isSaved) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                        contentDescription = null
                    )
                }
            }
            val sdf = SimpleDateFormat("dd MMM, hh:mm a", Locale.getDefault())
            Text("📅 " + sdf.format(Date(event.startAt)), Modifier.padding(horizontal = 8.dp))
            Text("📍 " + event.areaName, Modifier.padding(8.dp))
        }
    }
}