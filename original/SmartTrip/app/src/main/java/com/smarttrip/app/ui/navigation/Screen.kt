package com.smarttrip.app.ui.navigation

sealed class Screen(val route: String) {
    object TripList : Screen("trip_list")
    object TripEdit : Screen("trip_edit/{tripId}") {
        fun createRoute(tripId: String) = "trip_edit/$tripId"
    }
    object Navigation : Screen("navigation/{tripId}") {
        fun createRoute(tripId: String) = "navigation/$tripId"
    }
}
