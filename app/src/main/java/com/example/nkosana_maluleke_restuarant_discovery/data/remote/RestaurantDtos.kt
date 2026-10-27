package com.example.nkosana_maluleke_restuarant_discovery.data.remote

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Top-level response model for the restaurants endpoint.
 *
 * @property restaurants restaurants included in the payload.
 */
@Serializable
data class RestaurantFeedDto(
    val restaurants: List<RestaurantDto> = emptyList(),
)

/**
 * Raw restaurant payload model received from JSON.
 *
 * @property id unique restaurant identifier.
 * @property name restaurant display name.
 * @property cuisines cuisine labels.
 * @property rating rating value.
 * @property deliveryFeeCents delivery fee in cents.
 * @property etaMinutes ETA in minutes.
 * @property isOpen true when open.
 * @property imageUrl optional hero image URL.
 */
@Serializable
data class RestaurantDto(
    val id: String? = null,
    val name: String? = null,
    val cuisines: List<String?>? = null,
    val rating: Double? = null,
    @SerialName("delivery_fee_cents")
    val deliveryFeeCents: Int? = null,
    @SerialName("eta_minutes")
    val etaMinutes: Int? = null,
    @SerialName("is_open")
    val isOpen: Boolean? = null,
    @SerialName("image_url")
    val imageUrl: String? = null,
)
