package com.smarttrip.app.repository

import com.smarttrip.app.model.Destination
import com.smarttrip.app.model.Trip
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

object TripRepository {
    private val _trips = MutableStateFlow<List<Trip>>(emptyList())
    val trips: StateFlow<List<Trip>> = _trips.asStateFlow()

    fun getTripById(tripId: String): Trip? {
        return _trips.value.find { it.id == tripId }
    }

    fun createTrip(name: String): Trip {
        val trip = Trip(name = name)
        _trips.update { currentTrips ->
            currentTrips + trip
        }
        return trip
    }

    fun updateTrip(trip: Trip) {
        _trips.update { currentTrips ->
            currentTrips.map {
                if (it.id == trip.id) {
                    trip.copy(updatedAt = System.currentTimeMillis())
                } else {
                    it
                }
            }
        }
    }

    fun deleteTrip(tripId: String) {
        _trips.update { currentTrips ->
            currentTrips.filter { it.id != tripId }
        }
    }

    fun updateDestinations(tripId: String, destinations: List<Destination>) {
        _trips.update { currentTrips ->
            currentTrips.map {
                if (it.id == tripId) {
                    it.copy(
                        destinations = destinations,
                        updatedAt = System.currentTimeMillis()
                    )
                } else {
                    it
                }
            }
        }
    }
}
