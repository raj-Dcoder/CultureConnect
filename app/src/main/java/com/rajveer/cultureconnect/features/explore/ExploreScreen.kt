package com.rajveer.cultureconnect.features.explore

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.rajveer.cultureconnect.core.model.Highlight

@Composable
fun ExploreScreen(viewModel: ExploreViewModel = hiltViewModel()) {
    val list by viewModel.highlights.collectAsState()

    Column(
        modifier = Modifier.padding(16.dp)
    ) {
        Text(text = "Top Highlights", modifier = Modifier.padding(bottom = 8.dp))

        LazyColumn {
            items(list) { item ->
                HighlightCard(item)
            }
        }
    }
}

@Composable
fun HighlightCard(item: Highlight) {
    Card(
        modifier = Modifier
            .width(200.dp)
            .padding(end = 12.dp)
    ) {
        AsyncImage(
            model = item.imageUrl,
            contentDescription = item.title,
            modifier = Modifier.height(120.dp).fillMaxWidth()
        )
        Text(
            text = item.title,
            modifier = Modifier.padding(8.dp)
        )
    }
}
