package com.rajveer.cultureconnect.features.auth

import android.app.Activity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialException
import androidx.hilt.navigation.compose.hiltViewModel
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.rajveer.cultureconnect.BuildConfig
import com.rajveer.cultureconnect.R
import com.rajveer.cultureconnect.core.model.AuthState
import kotlinx.coroutines.launch
import android.util.Log

/**
 * Authentication screen with Google Sign-In.
 * 
 * Flow:
 * 1. User clicks "Sign in with Google"
 * 2. Credential Manager shows Google account picker
 * 3. User selects account
 * 4. We get ID token
 * 5. Pass token to ViewModel
 * 6. ViewModel handles Firebase auth
 * 7. Navigate based on auth state
 */
@Composable
fun AuthScreen(
    viewModel: AuthViewModel = hiltViewModel(),
    onAuthSuccess: () -> Unit,
    onNeedsOnboarding: () -> Unit
) {
    val authState by viewModel.authState.collectAsState()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    
    // Credential Manager for Google Sign-In
    val credentialManager = remember { CredentialManager.create(context) }
    
    // Handle auth state changes
    LaunchedEffect(authState) {
        when (authState) {
            is AuthState.Authenticated -> onAuthSuccess()
            is AuthState.NeedsOnboarding -> onNeedsOnboarding()
            else -> { /* Stay on login screen */ }
        }
    }
    
    // Main UI
    Scaffold { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            when (authState) {
                is AuthState.Loading -> {
                    CircularProgressIndicator()
                }
                
                is AuthState.Unauthenticated -> {
                    LoginContent(
                        onSignInClick = {
                            scope.launch {
                                handleGoogleSignIn(
                                    context = context,
                                    credentialManager = credentialManager,
                                    onSuccess = { idToken ->
                                        viewModel.signInWithGoogle(idToken)
                                    },
                                    onError = { error ->
                                        // Handle error (you can add error state to ViewModel)
                                        println("Sign-in error: $error")
                                    }
                                )
                            }
                        }
                    )
                }
                
                is AuthState.Error -> {
                    ErrorContent(
                        message = (authState as AuthState.Error).message,
                        onRetry = {
                            // Retry by resetting to Unauthenticated
                            // ViewModel will handle this
                        }
                    )
                }
                
                else -> { /* Handled by LaunchedEffect */ }
            }
        }
    }
}

/**
 * Login screen content.
 */
@Composable
private fun LoginContent(
    onSignInClick: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        // App logo/icon (you can add your own)
        Text(
            text = "🌍",
            style = MaterialTheme.typography.displayLarge
        )
        
        // App name
        Text(
            text = "Culture Connect",
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.Bold
        )
        
        // Tagline
        Text(
            text = "Experience cities like a local",
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        
        Spacer(modifier = Modifier.height(32.dp))
        
        // Google Sign-In Button
        Button(
            onClick = onSignInClick,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary
            )
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Google icon (you can add actual Google icon)
                Text(text = "G", fontWeight = FontWeight.Bold)
                Text(text = "Continue with Google")
            }
        }
    }
}

/**
 * Error screen content.
 */
@Composable
private fun ErrorContent(
    message: String,
    onRetry: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "⚠️",
            style = MaterialTheme.typography.displayMedium
        )
        
        Text(
            text = "Oops! Something went wrong",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )
        
        Text(
            text = message,
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.error
        )
        
        Button(onClick = onRetry) {
            Text("Try Again")
        }
    }
}

/**
 * Handle Google Sign-In using Credential Manager.
 * 
 * This is where the magic happens:
 * 1. Create GetGoogleIdOption with your Web Client ID
 * 2. Create GetCredentialRequest
 * 3. Launch Credential Manager
 * 4. Extract ID token from result
 * 5. Pass to callback
 */
private suspend fun handleGoogleSignIn(
    context: android.content.Context,
    credentialManager: CredentialManager,
    onSuccess: (String) -> Unit,
    onError: (String) -> Unit
) {
    try {
        Log.d("AuthScreen", "🔵 Starting handleGoogleSignIn...")
        
        // Get Web Client ID from BuildConfig
        val webClientId = BuildConfig.WEB_CLIENT_ID
        Log.d("AuthScreen", "Web Client ID: ${webClientId.take(20)}...") // Show first 20 chars
        
        if (webClientId.isEmpty()) {
            Log.e("AuthScreen", "❌ Web Client ID is EMPTY!")
            onError("Web Client ID not configured")
            return
        }
        
        // Create Google ID option
        Log.d("AuthScreen", "🔵 Creating Google ID option...")
        val googleIdOption = GetGoogleIdOption.Builder()
            .setFilterByAuthorizedAccounts(false)
            .setServerClientId(webClientId)
            .build()
        
        // Create credential request
        Log.d("AuthScreen", "🔵 Creating credential request...")
        val request = GetCredentialRequest.Builder()
            .addCredentialOption(googleIdOption)
            .build()
        
        // Launch Credential Manager
        Log.d("AuthScreen", "🔵 Launching Credential Manager...")
        val result = credentialManager.getCredential(
            request = request,
            context = context
        )
        
        Log.d("AuthScreen", "🔵 Got credential result, extracting token...")
        // Extract ID token
        val credential = result.credential

        // Handle both GoogleIdTokenCredential and CustomCredential
        when {
            credential is GoogleIdTokenCredential -> {
                val idToken = credential.idToken
                Log.d("AuthScreen", "✅ Got ID token from GoogleIdTokenCredential! Length: ${idToken.length}")
                onSuccess(idToken)
            }
            credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL -> {
                // This is the case you're hitting - CustomCredential with Google ID token
                val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credential.data)
                val idToken = googleIdTokenCredential.idToken
                Log.d("AuthScreen", "✅ Got ID token from CustomCredential! Length: ${idToken.length}")
                onSuccess(idToken)
            }
            else -> {
                Log.e("AuthScreen", "❌ Invalid credential type: ${credential::class.simpleName}, type: ${credential.type}")
                onError("Invalid credential type")
            }
        }
        
    } catch (e: GetCredentialException) {
        Log.e("AuthScreen", "❌ GetCredentialException: ${e.message}", e)
        onError(e.message ?: "Sign-in failed")
    } catch (e: Exception) {
        Log.e("AuthScreen", "❌ Exception: ${e.message}", e)
        onError(e.message ?: "Unknown error")
    }
}