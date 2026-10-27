package com.example.nkosana_maluleke_restuarant_discovery.ui.details

import androidx.lifecycle.SavedStateHandle
import com.example.nkosana_maluleke_restuarant_discovery.MainDispatcherRule
import com.example.nkosana_maluleke_restuarant_discovery.data.RestaurantRepository
import com.example.nkosana_maluleke_restuarant_discovery.model.Restaurant
import io.mockk.coEvery
import io.mockk.coJustRun
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class RestaurantDetailsViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun emitsRestaurantFromRepository() = runTest {
        val restaurant = Restaurant(
            id = "r-1001",
            name = "Cape Town Grill",
            cuisines = listOf("Steakhouse", "Grill"),
            rating = 4.6,
            deliveryFeeCents = 1500,
            etaMinutes = 32,
            isOpen = true,
            imageUrl = null,
            isFavorite = false,
        )
        val repository = mockk<RestaurantRepository>()
        every { repository.observeRestaurantById("r-1001") } returns flowOf(restaurant)

        val viewModel = RestaurantDetailsViewModel(
            SavedStateHandle(mapOf(RESTAURANT_ID_ARG to "r-1001")),
            repository,
        )
        val collectJob = backgroundScope.launch { viewModel.uiState.collect {} }

        advanceUntilIdle()

        assertFalse(viewModel.uiState.value.isLoading)
        assertEquals("r-1001", viewModel.uiState.value.restaurant?.id)
        assertEquals("Cape Town Grill", viewModel.uiState.value.restaurant?.name)

        collectJob.cancel()
    }

    @Test
    fun toggleFavoriteDelegatesToRepository() = runTest {
        val repository = mockk<RestaurantRepository>()
        every { repository.observeRestaurantById(any()) } returns flowOf(null)
        coJustRun { repository.updateFavorite(any(), any()) }

        val viewModel = RestaurantDetailsViewModel(
            SavedStateHandle(mapOf(RESTAURANT_ID_ARG to "r-1001")),
            repository,
        )
        val collectJob = backgroundScope.launch { viewModel.uiState.collect {} }
        advanceUntilIdle()

        viewModel.toggleFavorite("r-1001", true)
        advanceUntilIdle()

        coVerify { repository.updateFavorite("r-1001", true) }
        collectJob.cancel()
    }
}
