package com.example.nkosana_maluleke_restuarant_discovery

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.nkosana_maluleke_restuarant_discovery.ui.RestaurantApp
import com.example.nkosana_maluleke_restuarant_discovery.ui.theme.Nkosana_Maluleke_Restuarant_DiscoveryTheme
import dagger.hilt.android.AndroidEntryPoint

/** Hosts the Compose UI; annotated so Hilt can inject ViewModels into its destinations. */
@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    /**
     * Sets up edge-to-edge content and launches the app navigation graph.
     *
     * @param savedInstanceState activity state bundle supplied by Android.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            Nkosana_Maluleke_Restuarant_DiscoveryTheme {
                RestaurantApp()
            }
        }
    }
}
