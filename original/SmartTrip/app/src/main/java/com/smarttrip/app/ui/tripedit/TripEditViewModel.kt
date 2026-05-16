package com.smarttrip.app.ui.tripedit

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.smarttrip.app.model.Destination
import com.smarttrip.app.model.Trip
import com.smarttrip.app.model.TransportMode
import com.smarttrip.app.repository.TripRepository
import com.smarttrip.app.util.PlaceNameParser
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class TripEditUiState(
    val trip: Trip? = null,
    val tripName: String = "",
    val destinations: List<Destination> = emptyList(),
    val inputText: String = "",
    val isLoading: Boolean = false
)

sealed class TripEditEvent {
    object NavigateBack : TripEditEvent()
    data class ShowToast(val message: String) : TripEditEvent()
    data class NavigateToNavigation(val tripId: String) : TripEditEvent()
}

class TripEditViewModel(savedStateHandle: SavedStateHandle) : ViewModel() {
    private val tripId: String = savedStateHandle.get<String>("tripId") ?: ""

    private val _uiState = MutableStateFlow(TripEditUiState())
    val uiState: StateFlow<TripEditUiState> = _uiState.asStateFlow()

    private val _events = MutableSharedFlow<TripEditEvent>()
    val events: SharedFlow<TripEditEvent> = _events.asSharedFlow()

    init {
        loadTrip()
    }

    private fun loadTrip() {
        val trip = TripRepository.getTripById(tripId)
        if (trip != null) {
            _uiState.value = _uiState.value.copy(
                trip = trip,
                tripName = trip.name,
                destinations = trip.destinations
            )
        }
    }

    fun onTripNameChange(name: String) {
        _uiState.value = _uiState.value.copy(tripName = name)
    }

    fun onInputTextChange(text: String) {
        _uiState.value = _uiState.value.copy(inputText = text)
    }

    fun onParseInput() {
        val text = _uiState.value.inputText
        if (text.isBlank()) return

        val parsedDestinations = PlaceNameParser.parse(text)
        if (parsedDestinations.isNotEmpty()) {
            val currentDestinations = _uiState.value.destinations.toMutableList()
            currentDestinations.addAll(parsedDestinations)
            _uiState.value = _uiState.value.copy(
                destinations = currentDestinations,
                inputText = ""
            )
            viewModelScope.launch {
                _events.emit(TripEditEvent.ShowToast("已添加${parsedDestinations.size}个目的地"))
            }
        } else {
            viewModelScope.launch {
                _events.emit(TripEditEvent.ShowToast("未能识别目的地"))
            }
        }
    }

    fun onDestinationNameChange(index: Int, name: String) {
        val destinations = _uiState.value.destinations.toMutableList()
        if (index in destinations.indices) {
            destinations[index] = destinations[index].copy(name = name)
            _uiState.value = _uiState.value.copy(destinations = destinations)
        }
    }

    fun onTransportModeChange(index: Int, mode: TransportMode) {
        val destinations = _uiState.value.destinations.toMutableList()
        if (index in destinations.indices) {
            destinations[index] = destinations[index].copy(transportMode = mode)
            _uiState.value = _uiState.value.copy(destinations = destinations)
        }
    }

    fun onStayMinutesChange(index: Int, minutes: Int) {
        val destinations = _uiState.value.destinations.toMutableList()
        if (index in destinations.indices) {
            destinations[index] = destinations[index].copy(stayMinutes = minutes)
            _uiState.value = _uiState.value.copy(destinations = destinations)
        }
    }

    fun onMoveUp(index: Int) {
        if (index <= 0) return
        val destinations = _uiState.value.destinations.toMutableList()
        val temp = destinations[index]
        destinations[index] = destinations[index - 1]
        destinations[index - 1] = temp
        _uiState.value = _uiState.value.copy(destinations = destinations)
    }

    fun onMoveDown(index: Int) {
        val destinations = _uiState.value.destinations.toMutableList()
        if (index >= destinations.size - 1) return
        val temp = destinations[index]
        destinations[index] = destinations[index + 1]
        destinations[index + 1] = temp
        _uiState.value = _uiState.value.copy(destinations = destinations)
    }

    fun onDeleteDestination(index: Int) {
        val destinations = _uiState.value.destinations.toMutableList()
        if (index in destinations.indices) {
            destinations.removeAt(index)
            _uiState.value = _uiState.value.copy(destinations = destinations)
        }
    }

    fun onSaveTrip() {
        val currentState = _uiState.value
        val trip = currentState.trip ?: return
        val updatedTrip = trip.copy(
            name = currentState.tripName.ifBlank { "未命名行程" },
            destinations = currentState.destinations
        )
        TripRepository.updateTrip(updatedTrip)
        viewModelScope.launch {
            _events.emit(TripEditEvent.ShowToast("保存成功"))
            _events.emit(TripEditEvent.NavigateBack)
        }
    }

    fun onStartNavigation() {
        if (_uiState.value.destinations.isEmpty()) {
            viewModelScope.launch {
                _events.emit(TripEditEvent.ShowToast("请先添加目的地"))
            }
            return
        }
        val trip = _uiState.value.trip
        if (trip != null) {
            val updatedTrip = trip.copy(
                name = _uiState.value.tripName.ifBlank { "未命名行程" },
                destinations = _uiState.value.destinations
            )
            TripRepository.updateTrip(updatedTrip)
        }
        viewModelScope.launch {
            _events.emit(TripEditEvent.NavigateToNavigation(tripId))
        }
    }

    fun onNavigateBack() {
        viewModelScope.launch {
            _events.emit(TripEditEvent.NavigateBack)
        }
    }
}
