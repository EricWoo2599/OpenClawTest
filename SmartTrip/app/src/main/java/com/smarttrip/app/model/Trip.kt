package com.smarttrip.app.model

import java.util.UUID

data class Destination(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val transportMode: TransportMode = TransportMode.DRIVING,
    val stayMinutes: Int = 0
)

data class Trip(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val destinations: List<Destination> = emptyList(),
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)
