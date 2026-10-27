package com.example.nkosana_maluleke_restuarant_discovery.data.remote

import javax.inject.Inject
import kotlinx.coroutines.delay
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

private const val DEFAULT_RESTAURANT_IMAGE =
    "android.resource://com.example.nkosana_maluleke_restuarant_discovery/mipmap/ic_launcher"

/**
 * Mock remote source that returns a generated JSON feed with realistic list scale.
 *
 * @param json serializer used to encode the generated feed.
 */
class MockRestaurantRemoteDataSource @Inject constructor(
    private val json: Json,
) : RestaurantRemoteDataSource {

    /** Simulates network latency and returns encoded restaurant feed JSON. */
    override suspend fun fetchRestaurantFeedJson(): String {
        delay(700)
        return json.encodeToString(buildMockFeed())
    }

    /** Builds deterministic mock data used by the browse and details flows. */
    private fun buildMockFeed(): RestaurantFeedDto {
        val names = listOf(
            "Cape Town Grill",
            "Sushi on Sea Point",
            "Durban Spice House",
            "Pretoria Pasta Co",
            "Jozi Burger Joint",
            "Garden Route Bowls",
            "Vegan Veld Kitchen",
            "Braai Republic",
            "Ocean Fresh Kitchen",
            "Northern Noodle Bar",
            "Mzansi Tacos",
            "The Curry Cart",
        )
        val cuisineSets = listOf(
            listOf("Steakhouse", "Grill"),
            listOf("Sushi", "Japanese"),
            listOf("Indian", "Curry"),
            listOf("Italian", "Pasta"),
            listOf("Burgers", "American"),
            listOf("Healthy", "Salads"),
            listOf("Vegan", "Plant-based"),
            listOf("South African", "Braai"),
            listOf("Seafood", "Grill"),
            listOf("Asian", "Noodles"),
            listOf("Mexican", "Street Food"),
            listOf("Indian", "Street Food"),
        )
        val areas = listOf(
            "CBD",
            "Sea Point",
            "Claremont",
            "Woodstock",
            "Observatory",
            "Bellville",
            "Stellenbosch",
            "Somerset West",
            "Sandton",
            "Rosebank",
            "Fourways",
            "Midrand",
            "Centurion",
            "Menlyn",
            "Hatfield",
            "Umhlanga",
            "Morningside",
            "Westville",
            "North Beach",
            "Musgrave",
        )

        val restaurants = (1..240).map { index ->
            val seed = (index - 1) % names.size
            val area = areas[(index - 1) / names.size]
            val deliveryFee = when {
                index % 6 == 0 -> 0
                index % 5 == 0 -> 990
                else -> 1490 + (index % 4) * 200
            }
            RestaurantDto(
                id = "r-${1000 + index}",
                name = "$area ${names[seed]}",
                cuisines = cuisineSets[seed],
                rating = ((37 + (index % 14)) / 10.0),
                deliveryFeeCents = deliveryFee,
                etaMinutes = 18 + (index % 26),
                isOpen = index % 4 != 0,
                imageUrl = DEFAULT_RESTAURANT_IMAGE,
            )
        }
        return RestaurantFeedDto(restaurants = restaurants)
    }
}
