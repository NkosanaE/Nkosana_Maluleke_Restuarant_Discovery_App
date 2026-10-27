package com.example.nkosana_maluleke_restuarant_discovery.ui.common

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class FormattersTest {

    @Test
    fun formatDeliveryFee_handlesNullAndFree() {
        assertEquals("Fee unavailable", formatDeliveryFee(null))
        assertEquals("Free delivery", formatDeliveryFee(0))
    }

    @Test
    fun formatDeliveryFee_formatsCurrencyForPaidDelivery() {
        val value = formatDeliveryFee(1500)
        assertTrue(value.contains("15"))
    }

    @Test
    fun formatEta_formatsEtaOrFallback() {
        assertEquals("32 min", formatEta(32))
        assertEquals("ETA unavailable", formatEta(null))
    }

    @Test
    fun formatRating_formatsRatingOrFallback() {
        assertEquals("4.6", formatRating(4.64))
        assertEquals("N/A", formatRating(null))
    }
}
