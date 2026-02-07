package com.rajveer.cultureconnect.features.travel

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel

/** Travel Screen - Minimalist design matching reference */
@Composable
fun TravelScreen(viewModel: TravelViewModel = hiltViewModel()) {
    val uiState by viewModel.uiState.collectAsState()
    val fromLocation by viewModel.fromLocation.collectAsState()
    val toLocation by viewModel.toLocation.collectAsState()
    val predictions by viewModel.predictions.collectAsState()
    val isPredictionsLoading by viewModel.isPredictionsLoading.collectAsState()

    var activeField by remember { mutableStateOf<LocationField?>(null) }
    var showPermissionHandler by remember { mutableStateOf(false) }
    
    // Local state for text input
    var fromQuery by remember { mutableStateOf("") }
    var toQuery by remember { mutableStateOf("") }
    
    // Update queries when locations are selected
    LaunchedEffect(fromLocation) {
        if (fromLocation != null) {
            fromQuery = fromLocation.name
        }
    }
    
    LaunchedEffect(toLocation) {
        if (toLocation != null) {
            toQuery = toLocation.name
        }
    }

    val scrollState = rememberScrollState()

    Box(modifier = Modifier.fillMaxSize().background(Color(0xFFF5F5F5))) {
        Column(
                modifier =
                        Modifier.fillMaxSize()
                                .verticalScroll(scrollState)
                                .padding(horizontal = 20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(40.dp))

            // Header
            Text(
                    text = "Compare Ride Fares",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black,
                    textAlign = TextAlign.Center
            )

            Text(
                    text = "Find the cheapest ride from multiple providers.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.Gray,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(top = 8.dp)
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Main Card
            Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.fillMaxWidth().padding(20.dp)) {
                    // Use Current Location Button
                    Button(
                            onClick = { showPermissionHandler = true },
                            modifier = Modifier.align(Alignment.End),
                            colors =
                                    ButtonDefaults.buttonColors(containerColor = Color(0xFF5B7FFF)),
                            shape = RoundedCornerShape(20.dp),
                            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
                    ) {
                        Icon(
                                imageVector = Icons.Default.MyLocation,
                                contentDescription = "Current location",
                                modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(text = "Use Current Location", fontSize = 12.sp)
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Pickup Location Field
                    OutlinedTextField(
                            value = fromQuery,
                            onValueChange = { query ->
                                fromQuery = query
                                activeField = LocationField.FROM
                                viewModel.onSearchQueryChanged(query)
                            },
                            modifier = Modifier.fillMaxWidth(),
                            placeholder = { Text("Pickup Location", color = Color.Gray) },
                            leadingIcon = {
                                Icon(
                                        imageVector = Icons.Default.Search,
                                        contentDescription = "Search",
                                        tint = Color.Gray
                                )
                            },
                            trailingIcon =
                                    if (fromQuery.isNotEmpty()) {
                                        {
                                            IconButton(
                                                    onClick = {
                                                        fromQuery = ""
                                                        viewModel.clearLocation(
                                                                isFromLocation = true
                                                        )
                                                        activeField = null
                                                    }
                                            ) {
                                                Icon(
                                                        imageVector = Icons.Default.Close,
                                                        contentDescription = "Clear",
                                                        tint = Color.Gray
                                                )
                                            }
                                        }
                                    } else null,
                            shape = RoundedCornerShape(12.dp),
                            colors =
                                    OutlinedTextFieldDefaults.colors(
                                            unfocusedBorderColor = Color(0xFFE0E0E0),
                                            focusedBorderColor = Color(0xFF5B7FFF),
                                            unfocusedContainerColor = Color(0xFFFAFAFA),
                                            focusedContainerColor = Color(0xFFFAFAFA)
                                    ),
                            singleLine = true
                    )

                    // Predictions dropdown for FROM
                    if (activeField == LocationField.FROM && predictions.isNotEmpty()) {
                        Card(
                                modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
                                colors = CardDefaults.cardColors(containerColor = Color.White),
                                elevation = CardDefaults.cardElevation(4.dp)
                        ) {
                            Column {
                                predictions.take(5).forEach { prediction ->
                                    TextButton(
                                            onClick = {
                                                viewModel.onPlaceSelected(
                                                        prediction,
                                                        isFromLocation = true
                                                )
                                                activeField = null
                                            },
                                            modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Text(
                                                text = prediction.primaryText,
                                                modifier = Modifier.fillMaxWidth(),
                                                textAlign = TextAlign.Start,
                                                color = Color.Black
                                        )
                                    }
                                    if (prediction != predictions.last()) {
                                        Divider()
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Swap Button
                    Box(
                            modifier = Modifier.fillMaxWidth(),
                            contentAlignment = Alignment.CenterEnd
                    ) {
                        Surface(
                                onClick = { /* TODO: Swap */},
                                modifier = Modifier.size(40.dp),
                                shape = CircleShape,
                                color = Color(0xFFF5F5F5)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                        imageVector = Icons.Default.SwapVert,
                                        contentDescription = "Swap",
                                        tint = Color.Gray,
                                        modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Drop Location Field
                    OutlinedTextField(
                            value = toQuery,
                            onValueChange = { query ->
                                toQuery = query
                                activeField = LocationField.TO
                                viewModel.onSearchQueryChanged(query)
                            },
                            modifier = Modifier.fillMaxWidth(),
                            placeholder = { Text("Drop Location", color = Color.Gray) },
                            leadingIcon = {
                                Icon(
                                        imageVector = Icons.Default.Search,
                                        contentDescription = "Search",
                                        tint = Color.Gray
                                )
                            },
                            trailingIcon =
                                    if (toQuery.isNotEmpty()) {
                                        {
                                            IconButton(
                                                    onClick = {
                                                        toQuery = ""
                                                        viewModel.clearLocation(
                                                                isFromLocation = false
                                                        )
                                                        activeField = null
                                                    }
                                            ) {
                                                Icon(
                                                        imageVector = Icons.Default.Close,
                                                        contentDescription = "Clear",
                                                        tint = Color.Gray
                                                )
                                            }
                                        }
                                    } else null,
                            shape = RoundedCornerShape(12.dp),
                            colors =
                                    OutlinedTextFieldDefaults.colors(
                                            unfocusedBorderColor = Color(0xFFE0E0E0),
                                            focusedBorderColor = Color(0xFF5B7FFF),
                                            unfocusedContainerColor = Color(0xFFFAFAFA),
                                            focusedContainerColor = Color(0xFFFAFAFA)
                                    ),
                            singleLine = true
                    )

                    // Predictions dropdown for TO
                    if (activeField == LocationField.TO && predictions.isNotEmpty()) {
                        Card(
                                modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
                                colors = CardDefaults.cardColors(containerColor = Color.White),
                                elevation = CardDefaults.cardElevation(4.dp)
                        ) {
                            Column {
                                predictions.take(5).forEach { prediction ->
                                    TextButton(
                                            onClick = {
                                                viewModel.onPlaceSelected(
                                                        prediction,
                                                        isFromLocation = false
                                                )
                                                activeField = null
                                            },
                                            modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Text(
                                                text = prediction.primaryText,
                                                modifier = Modifier.fillMaxWidth(),
                                                textAlign = TextAlign.Start,
                                                color = Color.Black
                                        )
                                    }
                                    if (prediction != predictions.last()) {
                                        Divider()
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    // Compare Fares Button
                    Button(
                            onClick = { viewModel.compareFares() },
                            enabled = fromLocation != null && toLocation != null,
                            modifier = Modifier.fillMaxWidth().height(50.dp),
                            shape = RoundedCornerShape(25.dp),
                            colors =
                                    ButtonDefaults.buttonColors(
                                            containerColor = Color(0xFF5B7FFF),
                                            disabledContainerColor = Color(0xFFE0E0E0)
                                    )
                    ) {
                        Text(
                                text = "Compare Fares",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Status messages and results
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

            Spacer(modifier = Modifier.height(100.dp))
        }
    }
}

/** How it works section with icons */
@Composable
fun HowItWorksSection() {
    Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
                text = "How it works",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = Color.Black
        )

        Spacer(modifier = Modifier.height(24.dp))

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
            HowItWorksItem(
                    icon = Icons.Default.LocationOn,
                    step = "1. Select",
                    description = "locations"
            )

            HowItWorksItem(
                    icon = Icons.Default.CompareArrows,
                    step = "2. Compare",
                    description = "provider fares"
            )

            HowItWorksItem(
                    icon = Icons.Default.Wallet,
                    step = "3. Book the",
                    description = "cheapest\noption"
            )
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
    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.width(100.dp)) {
        Box(
                modifier = Modifier.size(60.dp).clip(CircleShape).background(Color(0xFFF0F0F0)),
                contentAlignment = Alignment.Center
        ) {
            Icon(
                    imageVector = icon,
                    contentDescription = step,
                    tint = Color(0xFF5B7FFF),
                    modifier = Modifier.size(30.dp)
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        Text(
                text = step,
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color.Black,
                textAlign = TextAlign.Center
        )

        Text(
                text = description,
                fontSize = 11.sp,
                color = Color.Gray,
                textAlign = TextAlign.Center,
                lineHeight = 14.sp
        )
    }
}

/** Loading card component */
@Composable
fun LoadingCard(message: String) {
    Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Row(
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
        ) {
            CircularProgressIndicator(
                    modifier = Modifier.size(20.dp),
                    strokeWidth = 2.dp,
                    color = Color(0xFF5B7FFF)
            )
            Text(text = message, style = MaterialTheme.typography.bodyMedium, color = Color.Black)
        }
    }
}

/** Info card types */
enum class InfoCardType {
    SUCCESS,
    ERROR,
    INFO
}

/** Info card component */
@Composable
fun InfoCard(message: String, type: InfoCardType) {
    val (containerColor, contentColor, icon) =
            when (type) {
                InfoCardType.SUCCESS -> Triple(Color(0xFFE8F5E9), Color(0xFF2E7D32), "✅")
                InfoCardType.ERROR -> Triple(Color(0xFFFFEBEE), Color(0xFFC62828), "❌")
                InfoCardType.INFO -> Triple(Color(0xFFE3F2FD), Color(0xFF1565C0), "ℹ️")
            }

    Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = containerColor)
    ) {
        Row(
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = icon, style = MaterialTheme.typography.titleLarge)
            Text(text = message, style = MaterialTheme.typography.bodyMedium, color = contentColor)
        }
    }
}

/** Enum to track which field is active */
enum class LocationField {
    FROM,
    TO
}
