package com.smarttrip.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.lifecycle.SavedStateHandle
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.smarttrip.app.ui.navigation.NavigationScreen
import com.smarttrip.app.ui.navigation.NavigationViewModel
import com.smarttrip.app.ui.navigation.Screen
import com.smarttrip.app.ui.theme.SmartTripTheme
import com.smarttrip.app.ui.tripedit.TripEditScreen
import com.smarttrip.app.ui.tripedit.TripEditViewModel
import com.smarttrip.app.ui.triplist.TripListScreen
import com.smarttrip.app.ui.triplist.TripListViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            SmartTripTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    SmartTripApp()
                }
            }
        }
    }
}

@Composable
fun SmartTripApp() {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = Screen.TripList.route
    ) {
        composable(Screen.TripList.route) {
            val viewModel = TripListViewModel()
            TripListScreen(
                viewModel = viewModel,
                onNavigateToEdit = { tripId ->
                    navController.navigate(Screen.TripEdit.createRoute(tripId))
                },
                onNavigateToNavigation = { tripId ->
                    navController.navigate(Screen.Navigation.createRoute(tripId))
                },
                onNavigateToCreate = {
                    val tripId = com.smarttrip.app.repository.TripRepository.createTrip("新行程").id
                    navController.navigate(Screen.TripEdit.createRoute(tripId))
                }
            )
        }

        composable(
            route = Screen.TripEdit.route,
            arguments = listOf(navArgument("tripId") { type = NavType.StringType })
        ) { backStackEntry ->
            val savedStateHandle = SavedStateHandle().apply {
                set("tripId", backStackEntry.arguments?.getString("tripId") ?: "")
            }
            val viewModel = TripEditViewModel(savedStateHandle)
            TripEditScreen(
                viewModel = viewModel,
                onNavigateBack = { navController.popBackStack() },
                onNavigateToNavigation = { tripId ->
                    navController.navigate(Screen.Navigation.createRoute(tripId))
                }
            )
        }

        composable(
            route = Screen.Navigation.route,
            arguments = listOf(navArgument("tripId") { type = NavType.StringType })
        ) { backStackEntry ->
            val savedStateHandle = SavedStateHandle().apply {
                set("tripId", backStackEntry.arguments?.getString("tripId") ?: "")
            }
            val viewModel = NavigationViewModel(savedStateHandle)
            NavigationScreen(
                viewModel = viewModel,
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
}
