package com.rajveer.cultureconnect.features.travel

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import kotlinx.coroutines.launch
import androidx.compose.runtime.rememberCoroutineScope

/** Travel Screen - with sticky collapsing route bar */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TravelScreen(
    viewModel: TravelViewModel = hiltViewModel(),
    destination: String? = null
) {
    val uiState by viewModel.uiState.collectAsState()
    val fromLocation by viewModel.fromLocation.collectAsState()
    val toLocation by viewModel.toLocation.collectAsState()
    val predictions by viewModel.predictions.collectAsState()
    val isPredictionsLoading by viewModel.isPredictionsLoading.collectAsState()

    var activeField by remember { mutableStateOf<LocationField?>(null) }
    var showPermissionHandler by remember { mutableStateOf(false) }

    var fromQuery by remember { mutableStateOf("") }
    var toQuery by remember { mutableStateOf("") }

    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current
    val coroutineScope = rememberCoroutineScope()

    // Auto-fill destination from navigation
    LaunchedEffect(destination) {
        if (!destination.isNullOrEmpty()) {
            viewModel.setDestination(destination)
            toQuery = destination
        }
    }


    // Track if the full card has scrolled out of view
    val listState = rememberLazyListState()
    val showCompactBar by remember {
        derivedStateOf {
            // Show compact bar when first item (full card) has scrolled past
            listState.firstVisibleItemIndex >= 1
        }
    }

    // Update queries when locations are selected
    LaunchedEffect(fromLocation) { fromLocation?.let { fromQuery = it.name } }
    LaunchedEffect(toLocation) { toLocation?.let { toQuery = it.name } }

    Box(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            state = listState,
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 100.dp)
        ) {
            // ─── Item 0: Header + Full Input Card ────────────
            item {
                Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Compare Rides",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Find the cheapest ride across providers",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = 4.dp)
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Full Input Card
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surface
                        ),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            // Current Location chip
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.End
                            ) {
                                AssistChip(
                                    onClick = { showPermissionHandler = true },
                                    label = { Text("Current Location", fontSize = 12.sp) },
                                    leadingIcon = {
                                        Icon(
                                            Icons.Default.MyLocation,
                                            contentDescription = null,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    },
                                    shape = RoundedCornerShape(20.dp)
                                )
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            // Pickup Field (compact)
                            OutlinedTextField(
                                value = fromQuery,
                                onValueChange = { query ->
                                    fromQuery = query
                                    activeField = LocationField.FROM
                                    viewModel.onSearchQueryChanged(query)
                                },
                                modifier = Modifier.fillMaxWidth(),
                                placeholder = { Text("Pickup Location", fontSize = 14.sp) },
                                leadingIcon = {
                                    Icon(
                                        Icons.Default.TripOrigin,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(16.dp)
                                    )
                                },
                                trailingIcon = if (fromQuery.isNotEmpty()) {
                                    {
                                        IconButton(
                                            onClick = {
                                                fromQuery = ""
                                                viewModel.clearLocation(isFromLocation = true)
                                                activeField = null
                                            },
                                            modifier = Modifier.size(32.dp)
                                        ) {
                                            Icon(Icons.Default.Close, contentDescription = "Clear", modifier = Modifier.size(16.dp))
                                        }
                                    }
                                } else null,
                                shape = RoundedCornerShape(10.dp),
                                singleLine = true,
                                textStyle = MaterialTheme.typography.bodyMedium,
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                                    unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
                                )
                            )

                            // FROM predictions
                            if (activeField == LocationField.FROM && predictions.isNotEmpty()) {
                                PredictionsDropdown(predictions) { prediction ->
                                    viewModel.onPlaceSelected(prediction, isFromLocation = true)
                                    activeField = null
                                }
                            }

                            // Swap button
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 2.dp),
                                contentAlignment = Alignment.CenterEnd
                            ) {
                                Surface(
                                    onClick = {
                                        if (fromLocation != null && toLocation != null) {
                                            val tempQuery = fromQuery
                                            fromQuery = toQuery
                                            toQuery = tempQuery
                                            viewModel.swapLocations()
                                        }
                                    },
                                    modifier = Modifier.size(32.dp),
                                    shape = CircleShape,
                                    color = MaterialTheme.colorScheme.surfaceVariant
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(
                                            Icons.Default.SwapVert,
                                            contentDescription = "Swap",
                                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }
                            }

                            // Drop Field (compact)
                            OutlinedTextField(
                                value = toQuery,
                                onValueChange = { query ->
                                    toQuery = query
                                    activeField = LocationField.TO
                                    viewModel.onSearchQueryChanged(query)
                                },
                                modifier = Modifier.fillMaxWidth(),
                                placeholder = { Text("Drop Location", fontSize = 14.sp) },
                                leadingIcon = {
                                    Icon(
                                        Icons.Default.LocationOn,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.error,
                                        modifier = Modifier.size(16.dp)
                                    )
                                },
                                trailingIcon = if (toQuery.isNotEmpty()) {
                                    {
                                        IconButton(
                                            onClick = {
                                                toQuery = ""
                                                viewModel.clearLocation(isFromLocation = false)
                                                activeField = null
                                            },
                                            modifier = Modifier.size(32.dp)
                                        ) {
                                            Icon(Icons.Default.Close, contentDescription = "Clear", modifier = Modifier.size(16.dp))
                                        }
                                    }
                                } else null,
                                shape = RoundedCornerShape(10.dp),
                                singleLine = true,
                                textStyle = MaterialTheme.typography.bodyMedium,
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                                    unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
                                )
                            )

                            // TO predictions
                            if (activeField == LocationField.TO && predictions.isNotEmpty()) {
                                PredictionsDropdown(predictions) { prediction ->
                                    viewModel.onPlaceSelected(prediction, isFromLocation = false)
                                    activeField = null
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            // Compare Button
                            Button(
                                onClick = {
                                    focusManager.clearFocus()
                                    keyboardController?.hide()
                                    viewModel.compareFares()
                                },
                                enabled = fromLocation != null && toLocation != null,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(44.dp),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text("Compare Fares", fontWeight = FontWeight.SemiBold)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))
                }
            }

            // ─── Item 1+: Results / Status ───────────────────
            item {
                Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                    when (val state = uiState) {
                        is TravelUiState.LoadingLocation -> {
                            LoadingCard(message = "Getting your location...")
                        }
                        is TravelUiState.LoadingFares -> {
                            FareLoadingIndicator()
                        }
                        is TravelUiState.FareResultsReady -> {
                            FareResultsSection(fareResponse = state.fareResponse)
                        }
                        is TravelUiState.Success -> {
                            InfoCard(message = state.message, type = InfoCardType.SUCCESS)
                        }
                        is TravelUiState.Error -> {
                            InfoCard(message = state.message, type = InfoCardType.ERROR)
                        }
                        is TravelUiState.Idle -> {
                            HowItWorksSection()
                        }
                    }
                }
            }
        }

        // ─── Compact Sticky Route Bar (appears when scrolled) ──
        AnimatedVisibility(
            visible = showCompactBar && (fromLocation != null || toLocation != null),
            enter = slideInVertically(initialOffsetY = { -it }) + fadeIn(),
            exit = slideOutVertically(targetOffsetY = { -it }) + fadeOut(),
            modifier = Modifier.align(Alignment.TopCenter)
        ) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(4.dp),
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 3.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // From
                    Icon(
                        Icons.Default.TripOrigin,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(14.dp)
                    )
                    Text(
                        text = fromLocation?.name ?: "Pickup",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Medium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )

                    // Arrow
                    Icon(
                        Icons.Default.ArrowForward,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(14.dp)
                    )

                    // To
                    Icon(
                        Icons.Default.LocationOn,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(14.dp)
                    )
                    Text(
                        text = toLocation?.name ?: "Drop",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Medium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )

                    // Edit button to scroll back up
                    IconButton(
                        onClick = {
                            // Scroll back to top when edit is tapped
                            coroutineScope.launch {
                                listState.animateScrollToItem(0)
                            }
                        },
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(
                            Icons.Default.Edit,
                            contentDescription = "Edit route",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
        }

        // Location Permission Handler
        if (showPermissionHandler) {
            LocationPermissionHandler(
                onPermissionGranted = {
                    showPermissionHandler = false
                    viewModel.useCurrentLocation()
                },
                onPermissionDenied = {
                    showPermissionHandler = false
                }
            )
        }
    }
}

// ─── Reusable Components ─────────────────────────────────────

/** Autocomplete predictions dropdown */
@Composable
fun PredictionsDropdown(
    predictions: List<PlacePrediction>,
    onSelect: (PlacePrediction) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 4.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(4.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column {
            predictions.take(5).forEach { prediction ->
                TextButton(
                    onClick = { onSelect(prediction) },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = prediction.primaryText,
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.Start,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
                if (prediction != predictions.last()) {
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                }
            }
        }
    }
}

/** How it works section */
@Composable
fun HowItWorksSection() {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "How it works",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold
        )

        Spacer(modifier = Modifier.height(20.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            HowItWorksItem(Icons.Default.LocationOn, "1. Select", "locations")
            HowItWorksItem(Icons.Default.CompareArrows, "2. Compare", "provider fares")
            HowItWorksItem(Icons.Default.Wallet, "3. Book", "cheapest ride")
        }
    }
}

/** Single how it works item */
@Composable
fun HowItWorksItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    step: String,
    description: String
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.width(100.dp)
    ) {
        Box(
            modifier = Modifier
                .size(56.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primaryContainer),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = step,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(26.dp)
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = step,
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold,
            textAlign = TextAlign.Center
        )
        Text(
            text = description,
            fontSize = 11.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            lineHeight = 14.sp
        )
    }
}

/** Loading card */
@Composable
fun LoadingCard(message: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            CircularProgressIndicator(
                modifier = Modifier.size(20.dp),
                strokeWidth = 2.dp,
                color = MaterialTheme.colorScheme.primary
            )
            Text(message, style = MaterialTheme.typography.bodyMedium)
        }
    }
}

/** Info card types */
enum class InfoCardType { SUCCESS, ERROR, INFO }

/** Info card */
@Composable
fun InfoCard(message: String, type: InfoCardType) {
    val (containerColor, contentColor, icon) = when (type) {
        InfoCardType.SUCCESS -> Triple(
            MaterialTheme.colorScheme.primaryContainer,
            MaterialTheme.colorScheme.onPrimaryContainer,
            "✅"
        )
        InfoCardType.ERROR -> Triple(
            MaterialTheme.colorScheme.errorContainer,
            MaterialTheme.colorScheme.onErrorContainer,
            "❌"
        )
        InfoCardType.INFO -> Triple(
            MaterialTheme.colorScheme.secondaryContainer,
            MaterialTheme.colorScheme.onSecondaryContainer,
            "ℹ️"
        )
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = containerColor)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = icon, style = MaterialTheme.typography.titleLarge)
            Text(text = message, style = MaterialTheme.typography.bodyMedium, color = contentColor)
        }
    }
}

/** Enum to track which field is active */
enum class LocationField { FROM, TO }
