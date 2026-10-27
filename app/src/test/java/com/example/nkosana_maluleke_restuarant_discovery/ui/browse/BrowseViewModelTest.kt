package com.example.nkosana_maluleke_restuarant_discovery.ui.browse

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
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class BrowseViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val seedRestaurants = listOf(
        Restaurant(
            id = "r-1",
            name = "Sushi Spot",
            cuisines = listOf("Sushi"),
            rating = 4.5,
            deliveryFeeCents = 1500,
            etaMinutes = 25,
            isOpen = true,
            imageUrl = null,
            isFavorite = false,
        ),
        Restaurant(
            id = "r-2",
            name = "Sushi Closed",
            cuisines = listOf("Sushi"),
            rating = 4.0,
            deliveryFeeCents = 1500,
            etaMinutes = 30,
            isOpen = false,
            imageUrl = null,
            isFavorite = false,
        ),
        Restaurant(
            id = "r-3",
            name = "Burger Hub",
            cuisines = listOf("Burgers"),
            rating = 4.2,
            deliveryFeeCents = 0,
            etaMinutes = 20,
            isOpen = true,
            imageUrl = null,
            isFavorite = false,
        ),
    )

    @Test
    fun updatesListWhenSearchAndOpenFiltersChange() = runTest {
        val repository = mockk<RestaurantRepository>()
        every { repository.observeRestaurants(any(), any()) } answers {
            val query = firstArg<String>().trim()
            val openNowOnly = secondArg<Boolean>()
            val filtered = seedRestaurants.filter { restaurant ->
                (query.isBlank() || restaurant.name.contains(query, ignoreCase = true)) &&
                    (!openNowOnly || restaurant.isOpen)
            }
            flowOf(filtered)
        }
        coEvery { repository.refreshRestaurants() } returns Result.success(Unit)

        val viewModel = BrowseViewModel(repository)
        val collectJob = backgroundScope.launch { viewModel.uiState.collect {} }

        advanceUntilIdle()
        assertEquals(3, viewModel.uiState.value.restaurants.size)

        viewModel.onSearchQueryChanged("sushi")
        advanceUntilIdle()
        assertEquals(2, viewModel.uiState.value.restaurants.size)

        viewModel.onOpenNowOnlyChanged(true)
        advanceUntilIdle()
        assertEquals(1, viewModel.uiState.value.restaurants.size)
        assertEquals("Sushi Spot", viewModel.uiState.value.restaurants.first().name)

        collectJob.cancel()
    }

    @Test
    fun refreshFailureSetsErrorMessage() = runTest {
        val repository = mockk<RestaurantRepository>()
        every { repository.observeRestaurants(any(), any()) } returns flowOf(emptyList())
        coEvery { repository.refreshRestaurants() } returns Result.failure(IllegalStateException("boom"))

        val viewModel = BrowseViewModel(repository)
        val collectJob = backgroundScope.launch { viewModel.uiState.collect {} }

        advanceUntilIdle()

        assertNotNull(viewModel.uiState.value.errorMessage)
        assertTrue(viewModel.uiState.value.isProblem)

        collectJob.cancel()
    }

    @Test
    fun toggleFavoriteDelegatesToRepository() = runTest {
        val repository = mockk<RestaurantRepository>()
        every { repository.observeRestaurants(any(), any()) } returns flowOf(emptyList())
        coEvery { repository.refreshRestaurants() } returns Result.success(Unit)
        coJustRun { repository.updateFavorite(any(), any()) }

        val viewModel = BrowseViewModel(repository)
        val collectJob = backgroundScope.launch { viewModel.uiState.collect {} }
        advanceUntilIdle()

        viewModel.toggleFavorite("r-1", true)
        advanceUntilIdle()

        coVerify { repository.updateFavorite("r-1", true) }
        collectJob.cancel()
    }
}
