package com.smarttrip.app.repository

import android.content.Context
import android.content.SharedPreferences
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.smarttrip.app.model.Destination
import com.smarttrip.app.model.Trip
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.UUID

object TripRepository {
    private const val PREFS_NAME = "smarttrip_prefs"
    private const val KEY_TRIPS = "trips"
    
    private lateinit var prefs: SharedPreferences
    private val gson = Gson()
    
    private val _trips = MutableStateFlow<List<Trip>>(emptyList())
    val trips: StateFlow<List<Trip>> = _trips.asStateFlow()
    
    fun init(context: Context) {
        prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        loadTrips()
    }
    
    private fun loadTrips() {
        val json = prefs.getString(KEY_TRIPS, null)
        if (json != null) {
            val type = object : TypeToken<List<Trip>>() {}.type
            _trips.value = gson.fromJson(json, type) ?: emptyList()
        }
    }
    
    private fun saveTrips() {
        val json = gson.toJson(_trips.value)
        prefs.edit().putString(KEY_TRIPS, json).apply()
    }
    
    fun getTripById(tripId: String): Trip? {
        return _trips.value.find { it.id == tripId }
    }
    
    fun createTrip(name: String): Trip {
        val trip = Trip(
            id = UUID.randomUUID().toString(),
            name = name,
            destinations = emptyList()
        )
        _trips.value = listOf(trip) + _trips.value
        saveTrips()
        return trip
    }
    
    fun updateTrip(trip: Trip) {
        _trips.value = _trips.value.map {
            if (it.id == trip.id) {
                trip.copy(updatedAt = System.currentTimeMillis())
            } else {
                it
            }
        }
        saveTrips()
    }
    
    fun deleteTrip(tripId: String) {
        _trips.value = _trips.value.filter { it.id != tripId }
        saveTrips()
    }
    
    fun updateDestinations(tripId: String, destinations: List<Destination>) {
        _trips.value = _trips.value.map {
            if (it.id == tripId) {
                it.copy(
                    destinations = destinations,
                    updatedAt = System.currentTimeMillis()
                )
            } else {
                it
            }
        }
        saveTrips()
    }
}
