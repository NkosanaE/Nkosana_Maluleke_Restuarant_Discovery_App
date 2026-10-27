package com.example.nkosana_maluleke_restuarant_discovery.ui.common

import java.text.NumberFormat
import java.util.Locale

private val currencyFormatter by lazy {
    NumberFormat.getCurrencyInstance(Locale.forLanguageTag("en-ZA"))
}

/**
 * Formats delivery fee cents into a user-facing currency string.
 *
 * @param deliveryFeeCents fee value in cents, nullable when unknown.
 */
fun formatDeliveryFee(deliveryFeeCents: Int?): String {
    if (deliveryFeeCents == null) return "Fee unavailable"
    if (deliveryFeeCents == 0) return "Free delivery"
    return currencyFormatter.format(deliveryFeeCents / 100.0)
}

/**
 * Formats ETA in minutes for display.
 *
 * @param etaMinutes ETA in minutes, nullable when unknown.
 */
fun formatEta(etaMinutes: Int?): String {
    return etaMinutes?.let { "$it min" } ?: "ETA unavailable"
}

/**
 * Formats rating to 1 decimal place, with a fallback for missing values.
 *
 * @param rating restaurant rating, nullable when unavailable.
 */
fun formatRating(rating: Double?): String {
    return rating?.let { String.format(Locale.US, "%.1f", it) } ?: "N/A"
}
