package com.example.nkosana_maluleke_restuarant_discovery.data.remote

/** Contract for retrieving restaurant payload JSON from a remote-like source. */
interface RestaurantRemoteDataSource {
    /** Returns a raw JSON string that represents the restaurant feed. */
    suspend fun fetchRestaurantFeedJson(): String
}
