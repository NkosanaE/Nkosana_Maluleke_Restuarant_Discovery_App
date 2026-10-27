package com.example.nkosana_maluleke_restuarant_discovery.ui.details

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.example.nkosana_maluleke_restuarant_discovery.ui.common.formatDeliveryFee
import com.example.nkosana_maluleke_restuarant_discovery.ui.common.formatEta
import com.example.nkosana_maluleke_restuarant_discovery.ui.common.formatRating

/**
 * Lifecycle-aware route wrapper for the details screen.
 *
 * @param viewModel details ViewModel exposing state and actions.
 * @param onBackClick callback for top-app-bar back navigation.
 */
@Composable
fun RestaurantDetailsRoute(
    viewModel: RestaurantDetailsViewModel,
    onBackClick: () -> Unit,
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    RestaurantDetailsScreen(
        state = state,
        onBackClick = onBackClick,
        onToggleFavorite = viewModel::toggleFavorite,
    )
}

/**
 * Displays detailed restaurant information and favorite action.
 *
 * @param state full details state to render.
 * @param onBackClick callback for top-app-bar back navigation.
 * @param onToggleFavorite callback when favorite state changes.
 */
@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun RestaurantDetailsScreen(
    state: RestaurantDetailsUiState,
    onBackClick: () -> Unit,
    onToggleFavorite: (String, Boolean) -> Unit,
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Restaurant details") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                        )
                    }
                },
            )
        },
    ) { padding ->
        when {
            state.isLoading -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                ) {
                    CircularProgressIndicator()
                }
            }

            state.isMissing -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                ) {
                    Text("Restaurant not found.")
                }
            }

            else -> {
                val restaurant = checkNotNull(state.restaurant)
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding),
                ) {
                    AsyncImage(
                        model = restaurant.imageUrl,
                        contentDescription = restaurant.name,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(220.dp),
                        contentScale = ContentScale.Crop,
                    )
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Text(
                                text = restaurant.name,
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.weight(1f),
                            )
                            IconButton(
                                onClick = {
                                    onToggleFavorite(restaurant.id, !restaurant.isFavorite)
                                },
                            ) {
                                Icon(
                                    imageVector = if (restaurant.isFavorite) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                                    contentDescription = if (restaurant.isFavorite) "Remove favorite" else "Add favorite",
                                    tint = MaterialTheme.colorScheme.primary,
                                )
                            }
                        }
                        Text(restaurant.cuisines.joinToString(" • ").ifBlank { "Cuisine unavailable" })
                        Text("⭐ ${formatRating(restaurant.rating)}")
                        Text("ETA: ${formatEta(restaurant.etaMinutes)}")
                        Text("Delivery fee: ${formatDeliveryFee(restaurant.deliveryFeeCents)}")
                        Text(if (restaurant.isOpen) "Open now" else "Currently closed")
                    }
                }
            }
        }
    }
}
