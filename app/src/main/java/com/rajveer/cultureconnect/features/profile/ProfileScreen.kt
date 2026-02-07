package com.rajveer.cultureconnect.features.profile

import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel

@Composable
fun ProfileScreen(onLogoutClick: () -> Unit = {}, onEventClick: (String) -> Unit, viewModel: ProfileViewModel = hiltViewModel()) {
    // Collect states from ViewModel
    val mode by viewModel.mode.collectAsState()
    val detectedCity by viewModel.detectedCity.collectAsState()
    val isDetecting by viewModel.isDetecting.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()
    val hasLocationPermission by viewModel.hasLocationPermission.collectAsState()
    val savedIds by viewModel.savedIds.collectAsState()

    // Permission launcher
    val permissionLauncher =
            rememberLauncherForActivityResult(
                    contract = ActivityResultContracts.RequestPermission()
            ) { isGranted ->
                if (isGranted) {
                    // Permission granted, update permission state and detect mode
                    viewModel.checkLocationPermission()
                    viewModel.detectUserMode()
                }
            }

    // Check permission on first load
    LaunchedEffect(Unit) {
        viewModel.checkLocationPermission()
        if (hasLocationPermission) {
            viewModel.detectUserMode()
        }
        viewModel.loadSavedEvents()
    }

    Scaffold { paddingValues ->
        Column(
                modifier = Modifier.fillMaxSize().padding(paddingValues).padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Title
            Text(
                    text = "Profile",
                    style = MaterialTheme.typography.headlineMedium,
                    modifier = Modifier.padding(top = 16.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Mode Display Card
            Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors =
                            CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.primaryContainer
                            )
            ) {
                Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(text = "Current Mode", style = MaterialTheme.typography.titleSmall)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                            text = mode.uppercase(),
                            style = MaterialTheme.typography.headlineLarge,
                            color = MaterialTheme.colorScheme.primary
                    )
                    if (detectedCity.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(text = "📍 $detectedCity", style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }

            // Loading Indicator
            if (isDetecting) {
                CircularProgressIndicator()
                Text(
                        text = "Detecting your location...",
                        style = MaterialTheme.typography.bodyMedium
                )
            }

            // Error Message
            errorMessage?.let { error ->
                Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors =
                                CardDefaults.cardColors(
                                        containerColor = MaterialTheme.colorScheme.errorContainer
                                )
                ) {
                    Row(
                            modifier = Modifier.padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                                text = error,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onErrorContainer,
                                modifier = Modifier.weight(1f)
                        )
                        TextButton(onClick = { viewModel.clearError() }) { Text("Dismiss") }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Permission/Detect Button
            if (!hasLocationPermission) {
                Button(
                        onClick = {
                            permissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
                        },
                        modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(
                            imageVector = Icons.Default.LocationOn,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Enable Location")
                }
            } else {
                OutlinedButton(
                        onClick = { viewModel.detectUserMode() },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = !isDetecting
                ) { Text("Refresh Location") }
            }

            Spacer(modifier = Modifier.weight(1f))

            Text("Saved Events", Modifier.padding(top = 24.dp))

            LazyColumn {
                items(savedIds.size) { id ->
                    Text(
                        text = "Event: $id",
                        modifier = Modifier
                            .padding(vertical = 4.dp)
                            .clickable { onEventClick(id.toString()) }
                    )
                }
            }
            // Logout Button
            OutlinedButton(onClick = onLogoutClick, modifier = Modifier.fillMaxWidth()) {
                Text("Logout")
            }
        }
    }
}
