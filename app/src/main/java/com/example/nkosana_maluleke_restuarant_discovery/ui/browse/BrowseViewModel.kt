package com.example.nkosana_maluleke_restuarant_discovery.ui.browse

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.nkosana_maluleke_restuarant_discovery.data.RestaurantRepository
import com.example.nkosana_maluleke_restuarant_discovery.model.Restaurant
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/**
 * UI state model for the browse screen.
 *
 * @property restaurants currently visible restaurants after filtering.
 * @property searchQuery active text query.
 * @property openNowOnly whether open-now filtering is enabled.
 * @property isRefreshing whether a remote refresh is in progress.
 * @property errorMessage refresh error message when available.
 */
data class BrowseUiState(
    val restaurants: List<Restaurant> = emptyList(),
    val searchQuery: String = "",
    val openNowOnly: Boolean = false,
    val isRefreshing: Boolean = false,
    val errorMessage: String? = null,
) {
    val isInitialLoading: Boolean = isRefreshing && restaurants.isEmpty()
    val isEmpty: Boolean = !isRefreshing && errorMessage == null && restaurants.isEmpty()
    val isProblem: Boolean = errorMessage != null && restaurants.isEmpty()
}

/**
 * ViewModel for browse interactions and feed refresh lifecycle.
 *
 * @param repository data source and persistence coordinator.
 */
@HiltViewModel
class BrowseViewModel @Inject constructor(
    private val repository: RestaurantRepository,
) : ViewModel() {

    private val isRefreshing = MutableStateFlow(false)
    private val errorMessage = MutableStateFlow<String?>(null)

    private val searchQuery = MutableStateFlow("")
    private val openNowOnly = MutableStateFlow(false)

    @OptIn(ExperimentalCoroutinesApi::class)
    private val restaurants = combine(searchQuery, openNowOnly) { query, openNow ->
        query to openNow
    }.flatMapLatest { (query, openNow) ->
        repository.observeRestaurants(searchQuery = query, openNowOnly = openNow)
    }

    /** Combined state consumed by the browse composable. */
    val uiState: StateFlow<BrowseUiState> = combine(
        restaurants,
        searchQuery,
        openNowOnly,
        isRefreshing,
        errorMessage,
    ) { list, query, openNow, refreshing, error ->
        BrowseUiState(
            restaurants = list,
            searchQuery = query,
            openNowOnly = openNow,
            isRefreshing = refreshing,
            errorMessage = error,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = BrowseUiState(isRefreshing = true),
    )

    init {
        refreshRestaurants()
    }

    /**
     * Updates the search query used by list filtering.
     *
     * @param value new query text.
     */
    fun onSearchQueryChanged(value: String) {
        searchQuery.value = value
    }

    /**
     * Updates the open-now filter used by list filtering.
     *
     * @param value true to show only open restaurants.
     */
    fun onOpenNowOnlyChanged(value: Boolean) {
        openNowOnly.value = value
    }

    /** Triggers a feed refresh from the remote source. */
    fun refreshRestaurants() {
        viewModelScope.launch {
            isRefreshing.value = true
            errorMessage.value = null
            val refreshResult = repository.refreshRestaurants()
            if (refreshResult.isFailure) {
                errorMessage.value = "Couldn’t refresh restaurants. Check your connection and try again."
            }
            isRefreshing.value = false
        }
    }

    /** Clears the currently shown error message. */
    fun dismissError() {
        errorMessage.value = null
    }

    /**
     * Toggles favorite state for a single restaurant.
     *
     * @param restaurantId unique restaurant identifier.
     * @param isFavorite true to favorite, false to unfavorite.
     */
    fun toggleFavorite(restaurantId: String, isFavorite: Boolean) {
        viewModelScope.launch {
            repository.updateFavorite(restaurantId = restaurantId, isFavorite = isFavorite)
        }
    }
}
