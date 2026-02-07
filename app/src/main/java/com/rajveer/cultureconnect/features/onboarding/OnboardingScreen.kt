package com.rajveer.cultureconnect.features.onboarding

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.google.firebase.auth.FirebaseUser
import com.rajveer.cultureconnect.features.auth.AuthViewModel
import com.google.accompanist.flowlayout.FlowRow

/**
 * Onboarding screen for new users.
 * 
 * Collects:
 * - Home city (required for mode detection)
 * - Interests (optional but recommended)
 * 
 * Flow:
 * 1. User selects home city from dropdown
 * 2. User selects interests (multi-select chips)
 * 3. User clicks "Complete Setup"
 * 4. ViewModel creates profile in Firestore
 * 5. AuthState changes to Authenticated
 * 6. UI automatically shows main app
 * 
 * Why separate screen?
 * - Clean separation of concerns
 * - Can add more steps later (permissions, tutorial, etc.)
 * - Better UX than cramming everything in auth screen
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OnboardingScreen(
    viewModel: AuthViewModel,
    firebaseUser: FirebaseUser
) {
    // Form state
    var selectedCity by remember { mutableStateOf("") }
    var selectedInterests by remember { mutableStateOf(setOf<String>()) }
    var showCityDropdown by remember { mutableStateOf(false) }
    
    // Available options
    val cities = listOf(
        "Goa",
        "Mumbai",
        "Delhi",
        "Bangalore",
        "Jaipur",
        "Kolkata",
        "Chennai",
        "Hyderabad",
        "Pune",
        "Ahmedabad"
    )
    
    val interests = listOf(
        "Beach",
        "Party",
        "Culture",
        "Food",
        "History",
        "Nature",
        "Adventure",
        "Shopping",
        "Nightlife",
        "Art"
    )
    
    Scaffold { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(24.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            Spacer(modifier = Modifier.height(32.dp))
            
            // Welcome message
            Text(
                text = "Welcome, ${firebaseUser.displayName?.split(" ")?.firstOrNull() ?: "there"}! 👋",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )
            
            Text(
                text = "Let's personalize your experience",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Home City Selection
            Text(
                text = "Where are you from?",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.fillMaxWidth()
            )
            
            ExposedDropdownMenuBox(
                expanded = showCityDropdown,
                onExpandedChange = { showCityDropdown = it }
            ) {
                OutlinedTextField(
                    value = selectedCity,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Select your home city") },
                    trailingIcon = {
                        ExposedDropdownMenuDefaults.TrailingIcon(expanded = showCityDropdown)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .menuAnchor(),
                    colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors()
                )
                
                ExposedDropdownMenu(
                    expanded = showCityDropdown,
                    onDismissRequest = { showCityDropdown = false }
                ) {
                    cities.forEach { city ->
                        DropdownMenuItem(
                            text = { Text(city) },
                            onClick = {
                                selectedCity = city
                                showCityDropdown = false
                            }
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Interests Selection
            Text(
                text = "What are you interested in?",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.fillMaxWidth()
            )
            
            Text(
                text = "Select all that apply",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.fillMaxWidth()
            )
            
            // Interest chips (multi-select)
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                mainAxisSpacing = 8.dp,
                crossAxisSpacing = 8.dp
            ) {
                interests.forEach { interest ->
                    val isSelected = selectedInterests.contains(interest)
                    FilterChip(
                        selected = isSelected,
                        onClick = {
                            selectedInterests = if (isSelected) {
                                selectedInterests - interest
                            } else {
                                selectedInterests + interest
                            }
                        },
                        label = { Text(interest) }
                    )
                }
            }
            
            Spacer(modifier = Modifier.weight(1f))
            
            // Complete button
            Button(
                onClick = {
                    if (selectedCity.isNotEmpty()) {
                        viewModel.completeOnboarding(
                            homeCity = selectedCity,
                            interests = selectedInterests.toList()
                        )
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                enabled = selectedCity.isNotEmpty()
            ) {
                Text("Complete Setup")
            }
            
            // Skip button (optional - allows empty interests)
            if (selectedInterests.isEmpty()) {
                TextButton(
                    onClick = {
                        if (selectedCity.isNotEmpty()) {
                            viewModel.completeOnboarding(
                                homeCity = selectedCity,
                                interests = emptyList()
                            )
                        }
                    },
                    enabled = selectedCity.isNotEmpty()
                ) {
                    Text("Skip for now")
                }
            }
        }
    }
}