package com.rajveer.cultureconnect.features.travel

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import com.google.accompanist.permissions.*

/**
 * Handles location permission request with proper UI feedback
 * 
 * @param onPermissionGranted Called when user grants permission
 * @param onPermissionDenied Called when user denies permission
 */
@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun LocationPermissionHandler(
    onPermissionGranted: () -> Unit,
    onPermissionDenied: () -> Unit
) {
    // Request fine location permission (includes coarse automatically)
    val permissionState = rememberPermissionState(
        permission = android.Manifest.permission.ACCESS_FINE_LOCATION
    )
    
    // Track if we should show rationale dialog
    var showRationale by remember { mutableStateOf(false) }
    
    // Handle permission state changes
    LaunchedEffect(permissionState.status) {
        when {
            // Permission granted - proceed
            permissionState.status.isGranted -> {
                onPermissionGranted()
            }
            
            // Permission denied - check if we should show rationale
            permissionState.status.shouldShowRationale -> {
                showRationale = true
            }
            
            // Permission denied forever - guide to settings
            !permissionState.status.isGranted && 
            !permissionState.status.shouldShowRationale -> {
                onPermissionDenied()
            }
        }
    }
    
    // Show rationale dialog if needed
    if (showRationale) {
        PermissionRationaleDialog(
            onDismiss = {
                showRationale = false
                onPermissionDenied()
            },
            onConfirm = {
                showRationale = false
                permissionState.launchPermissionRequest()
            }
        )
    }
    
    // Auto-request permission on first composition
    LaunchedEffect(Unit) {
        if (!permissionState.status.isGranted) {
            permissionState.launchPermissionRequest()
        }
    }
}

/**
 * Dialog explaining why we need location permission
 */
@Composable
private fun PermissionRationaleDialog(
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Location Permission Required") },
        text = {
            Text(
                "We need your location to:\n" +
                "• Show your current location as pickup point\n" +
                "• Calculate accurate ride fares\n" +
                "• Find nearby places"
            )
        },
        confirmButton = {
            Button(onClick = onConfirm) {
                Text("Grant Permission")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}