package com.smarttrip.app.ui.triplist

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Place
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.smarttrip.app.model.Trip
import com.smarttrip.app.model.TransportMode
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TripListScreen(
    viewModel: TripListViewModel,
    onNavigateToEdit: (String) -> Unit,
    onNavigateToNavigation: (String) -> Unit,
    onNavigateToCreate: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    var tripToDelete by remember { mutableStateOf<Trip?>(null) }

    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                is TripListEvent.NavigateToEdit -> onNavigateToEdit(event.tripId)
                is TripListEvent.NavigateToNavigation -> onNavigateToNavigation(event.tripId)
                TripListEvent.NavigateToCreate -> onNavigateToCreate()
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("智行规划") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { viewModel.onCreateTrip() },
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(Icons.Default.Add, contentDescription = "创建行程")
            }
        }
    ) { paddingValues ->
        if (uiState.trips.isEmpty()) {
            EmptyTripList(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            )
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(uiState.trips, key = { it.id }) { trip ->
                    TripCard(
                        trip = trip,
                        onClick = { viewModel.onTripClick(trip) },
                        onNavigateClick = { viewModel.onTripNavigateClick(trip) },
                        onLongClick = { tripToDelete = trip }
                    )
                }
            }
        }
    }

    tripToDelete?.let { trip ->
        AlertDialog(
            onDismissRequest = { tripToDelete = null },
            title = { Text("删除行程") },
            text = { Text("确定要删除行程「${trip.name}」吗？") },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.onDeleteTrip(trip.id)
                        tripToDelete = null
                    }
                ) {
                    Text("删除", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { tripToDelete = null }) {
                    Text("取消")
                }
            }
        )
    }
}

@Composable
private fun EmptyTripList(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.Place,
            contentDescription = null,
            modifier = Modifier.size(80.dp),
            tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "暂无行程",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "点击右下角按钮创建新行程",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
        )
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun TripCard(
    trip: Trip,
    onClick: () -> Unit,
    onNavigateClick: () -> Unit,
    onLongClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .combinedClickable(
                onClick = onClick,
                onLongClick = onLongClick
            ),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = trip.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "${trip.destinations.size}个目的地",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                    if (trip.destinations.isNotEmpty()) {
                        Text(
                            text = " · ",
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                        val transportSummary = trip.destinations
                            .take(3)
                            .joinToString("") { it.transportMode.emoji }
                        Text(
                            text = transportSummary,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = formatDate(trip.createdAt),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                )
            }
            Row {
                IconButton(onClick = onNavigateClick) {
                    Icon(
                        imageVector = Icons.Default.ChevronRight,
                        contentDescription = "导航",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }
}

private fun formatDate(timestamp: Long): String {
    val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
    return sdf.format(Date(timestamp))
}
