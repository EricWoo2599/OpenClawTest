package com.smarttrip.app.ui.navigation

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.smarttrip.app.model.Destination
import com.smarttrip.app.model.Trip
import com.smarttrip.app.repository.TripRepository
import com.smarttrip.app.util.BaiduMapHelper
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class NavigationUiState(
    val trip: Trip? = null,
    val segments: List<NavigationSegment> = emptyList(),
    val totalStayMinutes: Int = 0
)

data class NavigationSegment(
    val fromDestination: Destination,
    val toDestination: Destination
)

sealed class NavigationEvent {
    data class OpenBaiduMap(val fromName: String, val toName: String, val index: Int) : NavigationEvent()
    object NavigateBack : NavigationEvent()
}

class NavigationViewModel(savedStateHandle: SavedStateHandle) : ViewModel() {
    private val tripId: String = savedStateHandle.get<String>("tripId") ?: ""

    private val _uiState = MutableStateFlow(NavigationUiState())
    val uiState: StateFlow<NavigationUiState> = _uiState.asStateFlow()

    private val _events = MutableSharedFlow<NavigationEvent>()
    val events: SharedFlow<NavigationEvent> = _events.asSharedFlow()

    init {
        loadTrip()
    }

    private fun loadTrip() {
        val trip = TripRepository.getTripById(tripId)
        if (trip != null) {
            val segments = mutableListOf<NavigationSegment>()
            val destinations = trip.destinations
            for (i in 0 until destinations.size - 1) {
                segments.add(NavigationSegment(
                    fromDestination = destinations[i],
                    toDestination = destinations[i + 1]
                ))
            }

            val totalStay = destinations.sumOf { it.stayMinutes }

            _uiState.value = _uiState.value.copy(
                trip = trip,
                segments = segments,
                totalStayMinutes = totalStay
            )
        }
    }

    fun onSegmentClick(index: Int) {
        val segments = _uiState.value.segments
        if (index in segments.indices) {
            val segment = segments[index]
            viewModelScope.launch {
                _events.emit(NavigationEvent.OpenBaiduMap(
                    fromName = segment.fromDestination.name,
                    toName = segment.toDestination.name,
                    index = index
                ))
            }
        }
    }

    fun onNavigateBack() {
        viewModelScope.launch {
            _events.emit(NavigationEvent.NavigateBack)
        }
    }
}
