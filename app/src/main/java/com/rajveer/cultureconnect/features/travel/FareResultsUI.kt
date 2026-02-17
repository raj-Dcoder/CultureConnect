package com.rajveer.cultureconnect.features.travel

import android.content.Intent
import android.net.Uri
import android.util.Log
import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.OpenInNew
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/** Display fare results */
@Composable
fun FareResultsSection(fareResponse: FareResponse, modifier: Modifier = Modifier) {
    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { visible = true }

    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(tween(500)) + slideInVertically(
            initialOffsetY = { it / 3 },
            animationSpec = tween(500, easing = EaseOutCubic)
        )
    ) {
        Column(
            modifier = modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // ─── Route Summary Bar ───────────────────────
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                color = MaterialTheme.colorScheme.primaryContainer,
                tonalElevation = 1.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Distance
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            Icons.Default.Route,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp),
                            tint = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Text(
                            text = fareResponse.distance.text,
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }

                    // Divider dot
                    Box(
                        modifier = Modifier
                            .size(4.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.4f))
                    )

                    // Duration
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            Icons.Default.Schedule,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp),
                            tint = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Text(
                            text = fareResponse.duration.text,
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }

                    // Divider dot
                    Box(
                        modifier = Modifier
                            .size(4.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.4f))
                    )

                    // Provider count
                    Text(
                        text = "${fareResponse.providers.size} options",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }

            // ─── Provider Cards ──────────────────────────
            fareResponse.providers.forEachIndexed { index, estimate ->
                var itemVisible by remember { mutableStateOf(false) }
                LaunchedEffect(Unit) {
                    kotlinx.coroutines.delay(index * 100L)
                    itemVisible = true
                }

                AnimatedVisibility(
                    visible = itemVisible,
                    enter = fadeIn(tween(350)) + slideInVertically(
                        initialOffsetY = { it / 4 },
                        animationSpec = tween(350, easing = EaseOutCubic)
                    )
                ) {
                    PolishedFareCard(
                        estimate = estimate,
                        rank = index + 1,
                        isBest = index == 0
                    )
                }
            }
        }
    }
}

/** Polished fare card with clean layout */
@Composable
fun PolishedFareCard(
    estimate: FareEstimate,
    rank: Int,
    isBest: Boolean,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val icon = getProviderIcon(estimate.category)
    val categoryColor = getCategoryColor(estimate.category)

    Card(
        modifier = modifier
            .fillMaxWidth()
            .then(
                if (isBest) Modifier.border(
                    width = 1.5.dp,
                    brush = Brush.linearGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.primary,
                            MaterialTheme.colorScheme.tertiary
                        )
                    ),
                    shape = RoundedCornerShape(14.dp)
                ) else Modifier
            ),
        shape = RoundedCornerShape(14.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = if (isBest) 3.dp else 1.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column {
            // Best price banner
            if (isBest) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            Brush.horizontalGradient(
                                colors = listOf(
                                    MaterialTheme.colorScheme.primary,
                                    MaterialTheme.colorScheme.tertiary
                                )
                            )
                        )
                        .padding(vertical = 4.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "⚡ BEST PRICE",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimary,
                        letterSpacing = 1.sp
                    )
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Left: Icon + Provider name + Category
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Category icon in circle
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .clip(CircleShape)
                            .background(categoryColor.copy(alpha = 0.12f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = icon,
                            contentDescription = estimate.category,
                            tint = categoryColor,
                            modifier = Modifier.size(22.dp)
                        )
                    }

                    Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        Text(
                            text = estimate.provider,
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = estimate.category,
                            style = MaterialTheme.typography.bodySmall,
                            color = categoryColor,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                // Right: Price + Book button
                Column(
                    horizontalAlignment = Alignment.End,
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    // Price
                    Text(
                        text = "₹${estimate.estimatedFare}",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = if (isBest) MaterialTheme.colorScheme.primary
                               else MaterialTheme.colorScheme.onSurface
                    )

                    // Book button
                    if (estimate.deepLink.isNotEmpty()) {
                        FilledTonalButton(
                            onClick = { openProviderApp(context, estimate) },
                            modifier = Modifier.height(30.dp),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 0.dp),
                            colors = ButtonDefaults.filledTonalButtonColors(
                                containerColor = if (isBest) MaterialTheme.colorScheme.primary
                                                else MaterialTheme.colorScheme.secondaryContainer,
                                contentColor = if (isBest) MaterialTheme.colorScheme.onPrimary
                                              else MaterialTheme.colorScheme.onSecondaryContainer
                            )
                        ) {
                            Text(
                                text = "Book",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.SemiBold
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Icon(
                                Icons.Outlined.OpenInNew,
                                contentDescription = null,
                                modifier = Modifier.size(13.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

/** Handle opening provider app with fallback */
private fun openProviderApp(context: android.content.Context, estimate: FareEstimate) {
    Log.d("FareCard", "📱 Opening: ${estimate.provider}")
    try {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(estimate.deepLink))
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(intent)
    } catch (e: Exception) {
        Log.e("FareCard", "❌ Failed to open ${estimate.provider}", e)
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
                context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(playStoreUrl)))
            } catch (ex: Exception) {
                Toast.makeText(context, "${estimate.provider} not available", Toast.LENGTH_SHORT).show()
            }
        } else {
            Toast.makeText(context, "${estimate.provider} app not installed", Toast.LENGTH_SHORT).show()
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
        "economy" -> Color(0xFF2E7D32)   // Deep green
        "premium" -> Color(0xFFE65100)   // Deep orange
        "bike" -> Color(0xFF1565C0)      // Deep blue
        "auto" -> Color(0xFF7B1FA2)      // Deep purple
        else -> MaterialTheme.colorScheme.primary
    }
}

/** Loading indicator */
@Composable
fun FareLoadingIndicator(modifier: Modifier = Modifier) {
    val infiniteTransition = rememberInfiniteTransition(label = "loading")
    val dotAlpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = EaseInOutCubic),
            repeatMode = RepeatMode.Reverse
        ),
        label = "alpha"
    )

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(40.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        CircularProgressIndicator(
            modifier = Modifier.size(48.dp),
            strokeWidth = 3.dp,
            color = MaterialTheme.colorScheme.primary
        )
        Text(
            text = "Comparing fares...",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = dotAlpha)
        )
        Text(
            text = "Checking multiple providers",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}