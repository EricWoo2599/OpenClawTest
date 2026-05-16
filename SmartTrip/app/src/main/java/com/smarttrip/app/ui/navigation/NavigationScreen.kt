package com.smarttrip.app.ui.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.smarttrip.app.model.Destination
import com.smarttrip.app.model.TransportMode
import com.smarttrip.app.ui.theme.BicycleColor
import com.smarttrip.app.ui.theme.BluePrimary
import com.smarttrip.app.util.BaiduMapHelper

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NavigationScreen(
    viewModel: NavigationViewModel,
    onNavigateBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                is NavigationEvent.OpenBaiduMap -> {
                    val trip = uiState.trip
                    if (trip != null && event.index < trip.destinations.size - 1) {
                        val fromDest = trip.destinations[event.index]
                        val toDest = trip.destinations[event.index + 1]
                        BaiduMapHelper.openNavigation(
                            context = context,
                            fromName = fromDest.name,
                            toName = toDest.name,
                            transportMode = fromDest.transportMode
                        )
                    }
                }
                NavigationEvent.NavigateBack -> onNavigateBack()
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("导航概览") },
                navigationIcon = {
                    IconButton(onClick = { viewModel.onNavigateBack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "返回")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            uiState.trip?.let { trip ->
                item {
                    TripOverviewCard(trip = trip, totalStayMinutes = uiState.totalStayMinutes)
                }

                item {
                    Text(
                        text = "行程时间轴",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                }

                itemsIndexed(trip.destinations) { index, destination ->
                    val segmentIndex = if (index < trip.destinations.size - 1) index else null
                    DestinationTimelineItem(
                        destination = destination,
                        index = index,
                        isFirst = index == 0,
                        isLast = index == trip.destinations.size - 1,
                        segmentIndex = segmentIndex,
                        onSegmentClick = {
                            if (segmentIndex != null) {
                                viewModel.onSegmentClick(segmentIndex)
                            }
                        }
                    )
                }

                item {
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = {
                            if (trip.destinations.size >= 2) {
                                val firstDest = trip.destinations[0]
                                val secondDest = trip.destinations[1]
                                BaiduMapHelper.openNavigation(
                                    context = context,
                                    fromName = firstDest.name,
                                    toName = secondDest.name,
                                    transportMode = firstDest.transportMode
                                )
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(8.dp),
                        enabled = trip.destinations.size >= 2
                    ) {
                        Icon(Icons.Default.Navigation, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("从起点开始导航")
                    }
                }
            }
        }
    }
}

@Composable
private fun TripOverviewCard(
    trip: com.smarttrip.app.model.Trip,
    totalStayMinutes: Int
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = BluePrimary
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = trip.name,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                horizontalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                Column {
                    Text(
                        text = "${trip.destinations.size}",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Text(
                        text = "目的地",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.White.copy(alpha = 0.8f)
                    )
                }
                Column {
                    Text(
                        text = "${trip.destinations.size - 1}",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Text(
                        text = "路段",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.White.copy(alpha = 0.8f)
                    )
                }
                Column {
                    Text(
                        text = "${totalStayMinutes}",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Text(
                        text = "停留(分钟)",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.White.copy(alpha = 0.8f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
            // 行程预览图 - 简化版路线
            if (trip.destinations.isNotEmpty()) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = Color.White.copy(alpha = 0.2f)
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp)
                    ) {
                        Text(
                            text = "路线预览",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceAround,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            trip.destinations.take(5).forEachIndexed { i, dest ->
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(24.dp)
                                            .clip(CircleShape)
                                            .background(if (i == 0) Color.Green else if (i == trip.destinations.size - 1) Color.Red else Color.White),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = "${i + 1}",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = if (i == 0 || i == trip.destinations.size - 1) Color.White else BluePrimary,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                    Text(
                                        text = dest.name.take(3),
                                        style = MaterialTheme.typography.bodySmall,
                                        color = Color.White.copy(alpha = 0.9f),
                                        maxLines = 1
                                    )
                                }
                                if (i < trip.destinations.take(5).size - 1) {
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Box(
                                        modifier = Modifier
                                            .width(20.dp)
                                            .height(2.dp)
                                            .background(Color.White.copy(alpha = 0.7f))
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun DestinationTimelineItem(
    destination: Destination,
    index: Int,
    isFirst: Boolean,
    isLast: Boolean,
    segmentIndex: Int?,
    onSegmentClick: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Top
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.width(40.dp)
        ) {
            if (!isFirst) {
                Box(
                    modifier = Modifier
                        .width(2.dp)
                        .height(16.dp)
                        .background(BluePrimary)
                )
            } else {
                Spacer(modifier = Modifier.height(16.dp))
            }

            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(destination.transportMode.color),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "${index + 1}",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            if (!isLast) {
                Box(
                    modifier = Modifier
                        .width(2.dp)
                        .height(40.dp)
                        .background(BluePrimary.copy(alpha = 0.5f))
                )
            }
        }

        Spacer(modifier = Modifier.width(12.dp))

        Card(
            modifier = Modifier
                .weight(1f)
                .padding(bottom = if (!isLast) 8.dp else 0.dp)
                .then(
                    if (!isLast) Modifier.clickable(onClick = onSegmentClick) else Modifier
                ),
            shape = RoundedCornerShape(12.dp),
            colors = if (!isLast) CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            ) else CardDefaults.cardColors()
        ) {
            Column(
                modifier = Modifier.padding(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Place,
                            contentDescription = null,
                            tint = destination.transportMode.color,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = destination.name,
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Medium
                        )
                    }
                    Text(
                        text = "${destination.transportMode.emoji} ${destination.transportMode.label}",
                        style = MaterialTheme.typography.bodySmall,
                        color = destination.transportMode.color
                    )
                }

                if (destination.stayMinutes > 0) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Schedule,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                            tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "停留 ${destination.stayMinutes} 分钟",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                    }
                }

                if (!isLast) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        Button(
                            onClick = onSegmentClick,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary
                            ),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Navigation,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "从此处开始",
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }
                }
            }
        }
    }
}
