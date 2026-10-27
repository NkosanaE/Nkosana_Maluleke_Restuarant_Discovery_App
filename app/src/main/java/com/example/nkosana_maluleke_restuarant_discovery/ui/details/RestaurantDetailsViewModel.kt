package com.example.nkosana_maluleke_restuarant_discovery.ui.details

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.nkosana_maluleke_restuarant_discovery.data.RestaurantRepository
import com.example.nkosana_maluleke_restuarant_discovery.model.Restaurant
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/** Navigation argument key for the selected restaurant ID. */
const val RESTAURANT_ID_ARG = "restaurantId"

/**
 * UI state model for the restaurant details screen.
 *
 * @property restaurant currently loaded restaurant, if available.
 * @property isLoading whether details are still loading.
 */
data class RestaurantDetailsUiState(
    val restaurant: Restaurant? = null,
    val isLoading: Boolean = true,
) {
    val isMissing: Boolean = !isLoading && restaurant == null
}

/**
 * ViewModel for restaurant details and favorite updates.
 *
 * @param savedStateHandle holds the [RESTAURANT_ID_ARG] navigation argument.
 * @param repository data source and persistence coordinator.
 */
@HiltViewModel
class RestaurantDetailsViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val repository: RestaurantRepository,
) : ViewModel() {

    private val restaurantId: String = savedStateHandle[RESTAURANT_ID_ARG] ?: ""

    /** Stream of details state derived from the selected restaurant ID. */
    val uiState: StateFlow<RestaurantDetailsUiState> =
        repository.observeRestaurantById(restaurantId)
            .map { restaurant ->
                RestaurantDetailsUiState(
                    restaurant = restaurant,
                    isLoading = false,
                )
            }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5_000),
                initialValue = RestaurantDetailsUiState(isLoading = true),
            )

    /**
     * Toggles favorite state for the displayed restaurant.
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
