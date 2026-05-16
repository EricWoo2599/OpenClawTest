package com.smarttrip.app.model

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DirectionsBike
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.DirectionsWalk
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector

enum class TransportMode(
    val label: String,
    val emoji: String,
    val color: Color,
    val icon: ImageVector
) {
    DRIVING("驾车", "🚗", Color(0xFF1565C0), Icons.Filled.DirectionsCar),
    CYCLING("骑行", "🚲", Color(0xFF2E7D32), Icons.Filled.DirectionsBike),
    WALKING("步行", "🚶", Color(0xFF6A1B9A), Icons.Filled.DirectionsWalk),
    MOTORCYCLE("摩托车", "🏍️", Color(0xFFE65100), Icons.Filled.DirectionsBike);

    fun toBaiduMapMode(): String {
        return when (this) {
            DRIVING -> "driving"
            CYCLING -> "riding"
            WALKING -> "walking"
            MOTORCYCLE -> "driving"
        }
    }
}
