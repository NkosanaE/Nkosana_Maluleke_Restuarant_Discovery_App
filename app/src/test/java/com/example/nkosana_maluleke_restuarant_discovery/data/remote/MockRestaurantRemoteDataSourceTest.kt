package com.example.nkosana_maluleke_restuarant_discovery.data.remote

import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class MockRestaurantRemoteDataSourceTest {

    private val json = Json {
        ignoreUnknownKeys = true
        explicitNulls = false
    }

    @Test
    fun fetchRestaurantFeedJson_returnsExpectedScaleAndShape() = runBlocking {
        val dataSource = MockRestaurantRemoteDataSource(json = json)

        val payload = json.decodeFromString<RestaurantFeedDto>(dataSource.fetchRestaurantFeedJson())

        assertEquals(240, payload.restaurants.size)
        assertTrue(payload.restaurants.all { !it.id.isNullOrBlank() })
        assertTrue(payload.restaurants.all { !it.name.isNullOrBlank() })
    }

    @Test
    fun fetchRestaurantFeedJson_generatesUniqueNamesWithoutNumberSuffixes() = runBlocking {
        val dataSource = MockRestaurantRemoteDataSource(json = json)

        val payload = json.decodeFromString<RestaurantFeedDto>(dataSource.fetchRestaurantFeedJson())
        val names = payload.restaurants.mapNotNull { it.name }

        assertEquals(names.size, names.toSet().size)
        assertFalse(names.any { it.matches(Regex(""".*\s\d+$""")) })
    }
}
