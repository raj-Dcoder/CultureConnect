package com.rajveer.cultureconnect.features.travel

import androidx.compose.animation.*
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.unit.dp

/**
 * Location search field with autocomplete dropdown
 * 
 * @param label Label for the field (e.g., "From" or "To")
 * @param selectedLocation Currently selected location (if any)
 * @param predictions List of autocomplete predictions
 * @param isLoading Whether predictions are loading
 * @param onQueryChange Called when user types
 * @param onPredictionSelected Called when user selects a prediction
 * @param onClear Called when user clears the field
 * @param onCurrentLocationClick Called when user wants to use current location (From only)
 * @param showCurrentLocationButton Whether to show "Use Current Location" button
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LocationSearchField(
    label: String,
    selectedLocation: TravelLocation?,
    predictions: List<PlacePrediction>,
    isLoading: Boolean,
    onQueryChange: (String) -> Unit,
    onPredictionSelected: (PlacePrediction) -> Unit,
    onClear: () -> Unit,
    onCurrentLocationClick: (() -> Unit)? = null,
    showCurrentLocationButton: Boolean = false,
    modifier: Modifier = Modifier
) {
    var query by remember { mutableStateOf("") }
    var isFocused by remember { mutableStateOf(false) }
    var showPredictions by remember { mutableStateOf(false) }
    
    // Update query when location is selected
    LaunchedEffect(selectedLocation) {
        if (selectedLocation != null) {
            query = selectedLocation.name
            showPredictions = false
        }
    }
    
    Column(modifier = modifier) {
        // Search field
        OutlinedTextField(
            value = query,
            onValueChange = { newQuery ->
                query = newQuery
                onQueryChange(newQuery)
                showPredictions = newQuery.isNotBlank() && selectedLocation == null
            },
            label = { Text(label) },
            leadingIcon = {
                Icon(
                    imageVector = if (selectedLocation?.isCurrentLocation == true)
                        Icons.Default.MyLocation
                    else
                        Icons.Default.LocationOn,
                    contentDescription = null
                )
            },
            trailingIcon = {
                Row {
                    // Loading indicator
                    if (isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            strokeWidth = 2.dp
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                    }
                    
                    // Clear button
                    if (query.isNotBlank()) {
                        IconButton(onClick = {
                            query = ""
                            onClear()
                            showPredictions = false
                        }) {
                            Icon(
                                imageVector = Icons.Default.Clear,
                                contentDescription = "Clear"
                            )
                        }
                    }
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .onFocusChanged { focusState ->
                    isFocused = focusState.isFocused
                    if (focusState.isFocused && query.isNotBlank() && selectedLocation == null) {
                        showPredictions = true
                    }
                },
            singleLine = true,
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = MaterialTheme.colorScheme.outline
            )
        )
        
        // Current location button (for "From" field only)
        if (showCurrentLocationButton && onCurrentLocationClick != null && selectedLocation == null) {
            TextButton(
                onClick = onCurrentLocationClick,
                modifier = Modifier.padding(start = 8.dp, top = 4.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.MyLocation,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text("Use Current Location")
            }
        }
        
        // Predictions dropdown
        AnimatedVisibility(
            visible = showPredictions && predictions.isNotEmpty(),
            enter = fadeIn() + expandVertically(),
            exit = fadeOut() + shrinkVertically()
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                LazyColumn(
                    modifier = Modifier.heightIn(max = 300.dp)
                ) {
                    items(predictions) { prediction ->
                        PredictionItem(
                            prediction = prediction,
                            onClick = {
                                onPredictionSelected(prediction)
                                query = prediction.primaryText
                                showPredictions = false
                            }
                        )
                    }
                }
            }
        }
    }
}

/**
 * Single prediction item in the dropdown
 */
@Composable
private fun PredictionItem(
    prediction: PlacePrediction,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Default.LocationOn,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(20.dp)
        )
        
        Spacer(modifier = Modifier.width(12.dp))
        
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = prediction.primaryText,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface
            )
            
            prediction.secondaryText?.let { secondary ->
                Text(
                    text = secondary,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
    
    HorizontalDivider()
}