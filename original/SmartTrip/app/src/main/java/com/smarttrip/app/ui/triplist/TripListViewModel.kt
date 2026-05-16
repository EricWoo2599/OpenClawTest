package com.smarttrip.app.ui.triplist

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.smarttrip.app.model.Trip
import com.smarttrip.app.repository.TripRepository
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class TripListUiState(
    val trips: List<Trip> = emptyList(),
    val isLoading: Boolean = false
)

sealed class TripListEvent {
    data class NavigateToEdit(val tripId: String) : TripListEvent()
    data class NavigateToNavigation(val tripId: String) : TripListEvent()
    object NavigateToCreate : TripListEvent()
}

class TripListViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(TripListUiState())
    val uiState: StateFlow<TripListUiState> = _uiState.asStateFlow()

    private val _events = MutableSharedFlow<TripListEvent>()
    val events: SharedFlow<TripListEvent> = _events.asSharedFlow()

    init {
        viewModelScope.launch {
            TripRepository.trips.collect { trips ->
                _uiState.value = _uiState.value.copy(trips = trips)
            }
        }
    }

    fun onTripClick(trip: Trip) {
        viewModelScope.launch {
            _events.emit(TripListEvent.NavigateToEdit(trip.id))
        }
    }

    fun onTripNavigateClick(trip: Trip) {
        viewModelScope.launch {
            _events.emit(TripListEvent.NavigateToNavigation(trip.id))
        }
    }

    fun onDeleteTrip(tripId: String) {
        TripRepository.deleteTrip(tripId)
    }

    fun onCreateTrip() {
        viewModelScope.launch {
            val trip = TripRepository.createTrip("新行程")
            _events.emit(TripListEvent.NavigateToEdit(trip.id))
        }
    }
}
