package com.example.nkosana_maluleke_restuarant_discovery.model

/**
 * Domain model consumed by UI for restaurant listing and details.
 *
 * @property id unique restaurant identifier.
 * @property name display name.
 * @property cuisines cuisine labels for display.
 * @property rating rating value when available.
 * @property deliveryFeeCents delivery fee in cents when available.
 * @property etaMinutes ETA in minutes when available.
 * @property isOpen true when restaurant is currently open.
 * @property imageUrl remote image URL when available.
 * @property isFavorite true when user marked the restaurant as favorite.
 */
data class Restaurant(
    val id: String,
    val name: String,
    val cuisines: List<String>,
    val rating: Double?,
    val deliveryFeeCents: Int?,
    val etaMinutes: Int?,
    val isOpen: Boolean,
    val imageUrl: String?,
    val isFavorite: Boolean,
)
