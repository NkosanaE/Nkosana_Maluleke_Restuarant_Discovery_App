package com.example.nkosana_maluleke_restuarant_discovery.data

import android.content.Context
import com.example.nkosana_maluleke_restuarant_discovery.data.remote.RestaurantDto
import com.example.nkosana_maluleke_restuarant_discovery.data.remote.RestaurantFeedDto
import com.example.nkosana_maluleke_restuarant_discovery.data.remote.RestaurantRemoteDataSource
import com.example.nkosana_maluleke_restuarant_discovery.di.IoDispatcher
import com.example.nkosana_maluleke_restuarant_discovery.model.Restaurant
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

private const val RESTAURANTS_CACHE_FILE = "restaurants_cache.json"
private const val FAVORITES_CACHE_FILE = "favorite_restaurants.json"

/**
 * Repository coordinating restaurant data from a remote-like source and local disk cache.
 *
 * The repository keeps a hot in-memory flow for fast UI updates and persists the last
 * successful list plus favorites for offline usage.
 *
 * @param context app context used for file-backed persistence.
 * @param remoteDataSource source that provides feed JSON.
 * @param ioDispatcher dispatcher used for I/O and parsing work.
 * @param json serializer used for encoding/decoding cached payloads.
 */
@Singleton
class RestaurantRepository @Inject constructor(
    @ApplicationContext context: Context,
    private val remoteDataSource: RestaurantRemoteDataSource,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher,
    private val json: Json,
) {
    private val restaurantsCacheFile = File(context.filesDir, RESTAURANTS_CACHE_FILE)
    private val favoritesCacheFile = File(context.filesDir, FAVORITES_CACHE_FILE)
    private val restaurantsFlow = MutableStateFlow(loadRestaurantsFromDisk())

    /**
     * Observes the current restaurant list with search and "open now" filters applied.
     *
     * @param searchQuery text used to match restaurant names.
     * @param openNowOnly when true, only open restaurants are emitted.
     */
    fun observeRestaurants(searchQuery: String, openNowOnly: Boolean): Flow<List<Restaurant>> {
        return restaurantsFlow.map { restaurants ->
            val normalizedQuery = searchQuery.trim()
            restaurants.filter { restaurant ->
                val queryMatch = normalizedQuery.isBlank() ||
                    restaurant.name.contains(normalizedQuery, ignoreCase = true)
                val openMatch = !openNowOnly || restaurant.isOpen
                queryMatch && openMatch
            }.sortedWith(compareByDescending<Restaurant> { it.rating ?: 0.0 }.thenBy { it.name })
        }
    }

    /**
     * Observes a single restaurant by its ID from the current in-memory dataset.
     *
     * @param restaurantId unique restaurant identifier.
     */
    fun observeRestaurantById(restaurantId: String): Flow<Restaurant?> {
        return restaurantsFlow.map { restaurants -> restaurants.firstOrNull { it.id == restaurantId } }
    }

    /** Refreshes restaurants from the remote source and updates local cache on success. */
    suspend fun refreshRestaurants(): Result<Unit> = withContext(ioDispatcher) {
        runCatching {
            val rawJson = remoteDataSource.fetchRestaurantFeedJson()
            val payload = json.decodeFromString<RestaurantFeedDto>(rawJson)
            val favoriteIds = loadFavoriteIdsFromDisk()
            val restaurants = payload.restaurants.mapNotNull { dto -> dto.toDomain(favoriteIds) }
            restaurantsFlow.value = restaurants
            persistRestaurantsToDisk(restaurants)
        }
    }

    /**
     * Persists favorite state and updates the in-memory stream immediately.
     *
     * @param restaurantId unique restaurant identifier.
     * @param isFavorite true to favorite, false to remove favorite.
     */
    suspend fun updateFavorite(restaurantId: String, isFavorite: Boolean) = withContext(ioDispatcher) {
        val updatedFavoriteIds = loadFavoriteIdsFromDisk().toMutableSet().apply {
            if (isFavorite) add(restaurantId) else remove(restaurantId)
        }
        persistFavoriteIdsToDisk(updatedFavoriteIds)
        restaurantsFlow.value = restaurantsFlow.value.map { restaurant ->
            if (restaurant.id == restaurantId) restaurant.copy(isFavorite = isFavorite) else restaurant
        }
    }

    private fun loadRestaurantsFromDisk(): List<Restaurant> {
        if (!restaurantsCacheFile.exists()) return emptyList()
        return runCatching {
            val cachedJson = restaurantsCacheFile.readText()
            val payload = json.decodeFromString<CachedRestaurantFeed>(cachedJson)
            payload.restaurants.map { it.toDomain() }
        }.getOrElse { emptyList() }
    }

    private fun persistRestaurantsToDisk(restaurants: List<Restaurant>) {
        val payload = CachedRestaurantFeed(
            restaurants = restaurants.map { restaurant ->
                CachedRestaurant(
                    id = restaurant.id,
                    name = restaurant.name,
                    cuisines = restaurant.cuisines,
                    rating = restaurant.rating,
                    deliveryFeeCents = restaurant.deliveryFeeCents,
                    etaMinutes = restaurant.etaMinutes,
                    isOpen = restaurant.isOpen,
                    imageUrl = restaurant.imageUrl,
                    isFavorite = restaurant.isFavorite,
                )
            },
        )
        restaurantsCacheFile.writeText(json.encodeToString(payload))
    }

    private fun loadFavoriteIdsFromDisk(): Set<String> {
        if (!favoritesCacheFile.exists()) return emptySet()
        return runCatching {
            val raw = favoritesCacheFile.readText()
            json.decodeFromString<FavoriteIdsPayload>(raw).favoriteIds.toSet()
        }.getOrElse { emptySet() }
    }

    private fun persistFavoriteIdsToDisk(favoriteIds: Set<String>) {
        favoritesCacheFile.writeText(
            json.encodeToString(FavoriteIdsPayload(favoriteIds = favoriteIds.sorted())),
        )
    }
}

/**
 * Maps nullable/partial DTO data into a domain model used by UI.
 *
 * @param favoriteIds IDs currently marked as favorite.
 */
private fun RestaurantDto.toDomain(favoriteIds: Set<String>): Restaurant? {
    val normalizedId = id?.trim().orEmpty()
    val normalizedName = name?.trim().orEmpty()
    if (normalizedId.isBlank() || normalizedName.isBlank()) return null

    return Restaurant(
        id = normalizedId,
        name = normalizedName,
        cuisines = cuisines.orEmpty().mapNotNull { it?.trim() }.filter { it.isNotBlank() },
        rating = rating,
        deliveryFeeCents = deliveryFeeCents,
        etaMinutes = etaMinutes,
        isOpen = isOpen == true,
        imageUrl = imageUrl?.trim(),
        isFavorite = favoriteIds.contains(normalizedId),
    )
}

@Serializable
private data class CachedRestaurantFeed(
    val restaurants: List<CachedRestaurant>,
)

@Serializable
private data class CachedRestaurant(
    val id: String,
    val name: String,
    val cuisines: List<String>,
    val rating: Double?,
    val deliveryFeeCents: Int?,
    val etaMinutes: Int?,
    val isOpen: Boolean,
    val imageUrl: String?,
    val isFavorite: Boolean,
) {
    /** Converts a persisted cache item back into the UI domain model. */
    fun toDomain(): Restaurant {
        return Restaurant(
            id = id,
            name = name,
            cuisines = cuisines,
            rating = rating,
            deliveryFeeCents = deliveryFeeCents,
            etaMinutes = etaMinutes,
            isOpen = isOpen,
            imageUrl = imageUrl,
            isFavorite = isFavorite,
        )
    }
}

@Serializable
private data class FavoriteIdsPayload(
    val favoriteIds: List<String>,
)
