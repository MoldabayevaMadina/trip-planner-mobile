package com.example.tripplanner.ui

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument

object Routes {
    const val TRIPS = "trips"
    const val TRIP = "trip/{tripId}"
    const val TRIP_FORM = "trip_form?tripId={tripId}"

    fun trip(id: Long) = "trip/$id"
    fun tripForm(id: Long? = null) = if (id == null) "trip_form" else "trip_form?tripId=$id"
}

@Composable
fun TripPlannerNavHost(navController: NavHostController = rememberNavController()) {
    NavHost(navController = navController, startDestination = Routes.TRIPS) {

        composable(Routes.TRIPS) {
            TripsListRoute(
                onTripClick = { id -> navController.navigate(Routes.trip(id)) },
                onAddTrip = { navController.navigate(Routes.tripForm()) },
            )
        }

        composable(
            route = Routes.TRIP,
            arguments = listOf(navArgument("tripId") { type = NavType.LongType }),
        ) {
            TripDetailRoute(
                onBack = { navController.navigateUp() },
                onEdit = { id -> navController.navigate(Routes.tripForm(id)) },
            )
        }

        composable(
            route = Routes.TRIP_FORM,
            arguments = listOf(navArgument("tripId") {
                type = NavType.LongType
                defaultValue = TripFormViewModel.NEW_TRIP
            }),
        ) {
            TripFormRoute(
                onBack = { navController.navigateUp() },
                onSaved = { id, isNew ->
                    if (isNew) {
                        navController.navigate(Routes.trip(id)) { popUpTo(Routes.TRIPS) }
                    } else {
                        navController.navigateUp()
                    }
                },
            )
        }
    }
}