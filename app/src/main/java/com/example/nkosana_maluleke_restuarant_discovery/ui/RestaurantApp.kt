package com.example.nkosana_maluleke_restuarant_discovery.ui

import androidx.compose.runtime.Composable
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.nkosana_maluleke_restuarant_discovery.ui.browse.BrowseRoute
import com.example.nkosana_maluleke_restuarant_discovery.ui.browse.BrowseViewModel
import com.example.nkosana_maluleke_restuarant_discovery.ui.details.RESTAURANT_ID_ARG
import com.example.nkosana_maluleke_restuarant_discovery.ui.details.RestaurantDetailsRoute
import com.example.nkosana_maluleke_restuarant_discovery.ui.details.RestaurantDetailsViewModel

private const val ROUTE_BROWSE = "browse"
private const val ROUTE_DETAILS = "details/{$RESTAURANT_ID_ARG}"

/**
 * Main navigation graph for browse and details screens.
 *
 * ViewModels are provided by Hilt and scoped to their navigation back stack entries.
 */
@Composable
fun RestaurantApp() {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = ROUTE_BROWSE,
    ) {
        composable(ROUTE_BROWSE) {
            val viewModel = hiltViewModel<BrowseViewModel>()
            BrowseRoute(
                viewModel = viewModel,
                onRestaurantClick = { restaurantId ->
                    navController.navigate("details/$restaurantId")
                },
            )
        }

        composable(
            route = ROUTE_DETAILS,
            arguments = listOf(navArgument(RESTAURANT_ID_ARG) { type = NavType.StringType }),
        ) {
            val viewModel = hiltViewModel<RestaurantDetailsViewModel>()
            RestaurantDetailsRoute(
                viewModel = viewModel,
                onBackClick = { navController.popBackStack() },
            )
        }
    }
}
