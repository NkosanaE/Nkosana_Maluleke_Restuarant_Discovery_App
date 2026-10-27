package com.example.nkosana_maluleke_restuarant_discovery.ui.browse

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.nkosana_maluleke_restuarant_discovery.ui.common.RestaurantCard

/**
 * Lifecycle-aware route wrapper that collects and forwards browse UI state.
 *
 * @param viewModel browse ViewModel exposing state and actions.
 * @param onRestaurantClick callback when a restaurant card is selected.
 * @param modifier optional root modifier.
 */
@Composable
fun BrowseRoute(
    viewModel: BrowseViewModel,
    onRestaurantClick: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    BrowseScreen(
        state = state,
        onSearchQueryChanged = viewModel::onSearchQueryChanged,
        onOpenNowOnlyChanged = viewModel::onOpenNowOnlyChanged,
        onRetry = viewModel::refreshRestaurants,
        onRestaurantClick = onRestaurantClick,
        onToggleFavorite = viewModel::toggleFavorite,
        onDismissError = viewModel::dismissError,
        modifier = modifier,
    )
}

/**
 * Renders browse UI including filters, list content, and failure states.
 *
 * @param state full browse state to render.
 * @param onSearchQueryChanged callback when search text changes.
 * @param onOpenNowOnlyChanged callback when open-now filter changes.
 * @param onRetry callback to retry loading.
 * @param onRestaurantClick callback when a restaurant is tapped.
 * @param onToggleFavorite callback when favorite is toggled.
 * @param onDismissError callback to clear transient errors.
 * @param modifier optional root modifier.
 */
@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun BrowseScreen(
    state: BrowseUiState,
    onSearchQueryChanged: (String) -> Unit,
    onOpenNowOnlyChanged: (Boolean) -> Unit,
    onRetry: () -> Unit,
    onRestaurantClick: (String) -> Unit,
    onToggleFavorite: (String, Boolean) -> Unit,
    onDismissError: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var showTransientError by remember { mutableStateOf(false) }
    LaunchedEffect(state.errorMessage, state.restaurants.size) {
        showTransientError = state.errorMessage != null && state.restaurants.isNotEmpty()
    }

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text("Mr D Restaurant Discovery") },
                actions = {
                    IconButton(onClick = onRetry) {
                        Icon(Icons.Filled.Refresh, contentDescription = "Refresh")
                    }
                },
            )
        },
    ) { scaffoldPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(scaffoldPadding),
        ) {
            when {
                state.isInitialLoading -> {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                }

                state.isProblem -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center,
                    ) {
                        Text(
                            text = state.errorMessage ?: "Something went wrong",
                            style = MaterialTheme.typography.bodyLarge,
                        )
                        Button(onClick = onRetry, modifier = Modifier.padding(top = 16.dp)) {
                            Text("Try again")
                        }
                    }
                }

                else -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(12.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        item {
                            OutlinedTextField(
                                value = state.searchQuery,
                                onValueChange = onSearchQueryChanged,
                                label = { Text("Search by name") },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth(),
                            )
                        }
                        item {
                            FilterChip(
                                selected = state.openNowOnly,
                                onClick = { onOpenNowOnlyChanged(!state.openNowOnly) },
                                label = { Text("Open now") },
                            )
                        }
                        if (state.isEmpty) {
                            item {
                                Text(
                                    text = "No restaurants match your search or filter.",
                                    style = MaterialTheme.typography.bodyLarge,
                                    modifier = Modifier.padding(top = 12.dp),
                                )
                            }
                        } else {
                            items(state.restaurants, key = { it.id }) { restaurant ->
                                RestaurantCard(
                                    restaurant = restaurant,
                                    onClick = onRestaurantClick,
                                    onToggleFavorite = onToggleFavorite,
                                )
                            }
                        }
                    }
                }
            }

            if (showTransientError) {
                Snackbar(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(12.dp),
                    action = {
                        Button(onClick = {
                            onDismissError()
                            onRetry()
                        }) {
                            Text("Retry")
                        }
                    },
                ) {
                    Text(state.errorMessage.orEmpty())
                }
            }
        }
    }
}
