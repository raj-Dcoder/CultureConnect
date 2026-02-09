package com.rajveer.cultureconnect.features.travel

import android.content.Intent
import android.net.Uri
import android.util.Log
import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.EmojiTransportation
import androidx.compose.material.icons.filled.TwoWheeler
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

/** Display fare results from Cloud Function with enhanced UI */
@Composable
fun FareResultsSection(fareResponse: FareResponse, modifier: Modifier = Modifier) {
    var visible by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) { visible = true }

    AnimatedVisibility(
            visible = visible,
            enter =
                    fadeIn(animationSpec = tween(600)) +
                            slideInVertically(
                                    initialOffsetY = { it / 2 },
                                    animationSpec = tween(600, easing = EaseOutCubic)
                            )
    ) {
        Column(modifier = modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp)) {
            // Header with gradient background
            Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors =
                            CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.primaryContainer
                            )
            ) {
                Column(modifier = Modifier.fillMaxWidth().padding(20.dp)) {
                    Text(
                            text = "🚗 Fare Estimates",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Distance and Duration chips
                    Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        InfoChip(
                                icon = "📏",
                                label = fareResponse.distance.text,
                                modifier = Modifier.weight(1f)
                        )
                        InfoChip(
                                icon = "⏱️",
                                label = fareResponse.duration.text,
                                modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Provider Cards with staggered animation
            Column(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxWidth()
            ) {
                fareResponse.providers.forEachIndexed { index, estimate ->
                    var itemVisible by remember { mutableStateOf(false) }

                    LaunchedEffect(Unit) {
                        kotlinx.coroutines.delay(index * 80L)
                        itemVisible = true
                    }

                    AnimatedVisibility(
                            visible = itemVisible,
                            enter =
                                    fadeIn(animationSpec = tween(400)) +
                                            slideInHorizontally(
                                                    initialOffsetX = { it / 3 },
                                                    animationSpec =
                                                            tween(400, easing = EaseOutCubic)
                                            )
                    ) {
                        EnhancedFareCard(
                                estimate = estimate,
                                rank = index + 1,
                                isLowest = index == 0
                        )
                    }
                }
            }
        }
    }
}

/** Info chip for distance/duration */
@Composable
fun InfoChip(icon: String, label: String, modifier: Modifier = Modifier) {
    Surface(
            modifier = modifier,
            shape = RoundedCornerShape(12.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 2.dp
    ) {
        Row(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = icon, style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                    text = label,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

/** Enhanced card displaying a single provider's fare estimate */
@Composable
fun EnhancedFareCard(
        estimate: FareEstimate,
        rank: Int,
        isLowest: Boolean,
        modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val icon = getProviderIcon(estimate.category)
    val categoryColor = getCategoryColor(estimate.category)

    Card(
            modifier = modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = if (isLowest) 4.dp else 2.dp),
            colors =
                    CardDefaults.cardColors(
                            containerColor =
                                    if (isLowest) MaterialTheme.colorScheme.tertiaryContainer
                                    else MaterialTheme.colorScheme.surface
                    )
    ) {
        Row(
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
        ) {
            // Left side: Rank badge + Provider info
            Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Rank badge
                Box(
                        modifier =
                                Modifier.size(40.dp)
                                        .clip(CircleShape)
                                        .background(
                                                if (isLowest) MaterialTheme.colorScheme.primary
                                                else MaterialTheme.colorScheme.surfaceVariant
                                        ),
                        contentAlignment = Alignment.Center
                ) {
                    Text(
                            text = "#$rank",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold,
                            color =
                                    if (isLowest) MaterialTheme.colorScheme.onPrimary
                                    else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // Provider info
                Column {
                    Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                                imageVector = icon,
                                contentDescription = estimate.category,
                                tint = categoryColor,
                                modifier = Modifier.size(20.dp)
                        )
                        Text(
                                text = estimate.provider,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                        )
                    }

                    Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = categoryColor.copy(alpha = 0.15f)
                    ) {
                        Text(
                                text = estimate.category,
                                style = MaterialTheme.typography.labelSmall,
                                color = categoryColor,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                fontWeight = FontWeight.Medium
                        )
                    }
                }
            }

            // Right side: Fare + Action button
            Column(
                    horizontalAlignment = Alignment.End,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Fare amount
                Row(
                        verticalAlignment = Alignment.Bottom,
                        horizontalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    Text(
                            text = "₹",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold
                    )
                    Text(
                            text = estimate.estimatedFare.toString(),
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                    )
                }

                // Open App Button
                if (estimate.deepLink.isNotEmpty()) {
                    Button(
                            onClick = {
                                Log.d("FareCard", "📱 Attempting to open: ${estimate.provider}")
                                Log.d("FareCard", "Deep link: ${estimate.deepLink}")
                                
                                try {
                                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(estimate.deepLink))
                                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                    context.startActivity(intent)
                                    Log.d("FareCard", "✅ Intent started")
                                } catch (e: Exception) {
                                    Log.e("FareCard", "❌ Failed to open ${estimate.provider}", e)
                                    
                                    // Fallback: Try to open Play Store
                                    val playStoreUrl = when {
                                        estimate.provider.contains("Rapido", ignoreCase = true) -> 
                                            "https://play.google.com/store/apps/details?id=com.rapido.passenger"
                                        estimate.provider.contains("Uber", ignoreCase = true) -> 
                                            "https://play.google.com/store/apps/details?id=com.ubercab"
                                        estimate.provider.contains("Ola", ignoreCase = true) -> 
                                            "https://play.google.com/store/apps/details?id=com.olacabs.customer"
                                        else -> null
                                    }
                                    
                                    if (playStoreUrl != null) {
                                        try {
                                            Log.d("FareCard", "🛒 Opening Play Store for ${estimate.provider}")
                                            val playStoreIntent = Intent(Intent.ACTION_VIEW, Uri.parse(playStoreUrl))
                                            context.startActivity(playStoreIntent)
                                        } catch (ex: Exception) {
                                            Log.e("FareCard", "❌ Failed to open Play Store", ex)
                                            Toast.makeText(
                                                context,
                                                "${estimate.provider} not available",
                                                Toast.LENGTH_SHORT
                                            ).show()
                                        }
                                    } else {
                                        Toast.makeText(
                                            context,
                                            "${estimate.provider} app not installed",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    }
                                }
                                },
                            modifier = Modifier.height(36.dp),
                            shape = RoundedCornerShape(10.dp),
                            colors =
                                    ButtonDefaults.buttonColors(
                                            containerColor =
                                                    if (isLowest) MaterialTheme.colorScheme.primary
                                                    else MaterialTheme.colorScheme.secondary
                                    ),
                            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
                    ) {
                        Icon(
                                imageVector = Icons.Default.DirectionsCar,
                                contentDescription = "Open app",
                                modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                                text = "Book",
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        }

        // Best price indicator
        if (isLowest) {
            Box(
                    modifier =
                            Modifier.fillMaxWidth()
                                    .background(
                                            Brush.horizontalGradient(
                                                    colors =
                                                            listOf(
                                                                    MaterialTheme.colorScheme
                                                                            .primary.copy(
                                                                            alpha = 0.2f
                                                                    ),
                                                                    MaterialTheme.colorScheme
                                                                            .tertiary.copy(
                                                                            alpha = 0.2f
                                                                    )
                                                            )
                                            )
                                    )
                                    .padding(vertical = 6.dp),
                    contentAlignment = Alignment.Center
            ) {
                Text(
                        text = "🏆 Best Price",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

/** Get icon for provider category */
fun getProviderIcon(category: String): ImageVector {
    return when (category.lowercase()) {
        "bike" -> Icons.Default.TwoWheeler
        "auto" -> Icons.Default.EmojiTransportation
        else -> Icons.Default.DirectionsCar
    }
}

/** Get color for provider category */
@Composable
fun getCategoryColor(category: String): Color {
    return when (category.lowercase()) {
        "economy" -> Color(0xFF4CAF50) // Green
        "premium" -> Color(0xFFFF9800) // Orange
        "bike" -> Color(0xFF2196F3) // Blue
        "auto" -> Color(0xFF9C27B0) // Purple
        else -> MaterialTheme.colorScheme.primary
    }
}

/** Loading indicator for fare calculation with animation */
@Composable
fun FareLoadingIndicator(modifier: Modifier = Modifier) {
    val infiniteTransition = rememberInfiniteTransition(label = "loading")
    val scale by
            infiniteTransition.animateFloat(
                    initialValue = 0.8f,
                    targetValue = 1.2f,
                    animationSpec =
                            infiniteRepeatable(
                                    animation = tween(1000, easing = EaseInOutCubic),
                                    repeatMode = RepeatMode.Reverse
                            ),
                    label = "scale"
            )

    Column(
            modifier = modifier.fillMaxWidth().padding(48.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        Box(
                modifier =
                        Modifier.size(80.dp)
                                .background(
                                        Brush.radialGradient(
                                                colors =
                                                        listOf(
                                                                MaterialTheme.colorScheme.primary
                                                                        .copy(alpha = 0.3f),
                                                                MaterialTheme.colorScheme.primary
                                                                        .copy(alpha = 0.1f)
                                                        )
                                        ),
                                        shape = CircleShape
                                ),
                contentAlignment = Alignment.Center
        ) { CircularProgressIndicator(modifier = Modifier.size(60.dp * scale), strokeWidth = 4.dp) }

        Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                    text = "Calculating fares...",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.SemiBold
            )
            Text(
                    text = "Comparing 7 providers",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}