package com.example.campusguide.ui.admin

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.campusguide.data.Event
import com.example.campusguide.ui.common.VerticalScrollbar
import com.example.campusguide.ui.toLocalDate
import java.time.LocalDate
import java.time.format.DateTimeFormatter

private val UPH_Navy   = Color(0xFF16224C)
private val UPH_Orange = Color(0xFFF58A0A)
private val UPH_White  = Color(0xFFFFFFFF)

private val CardLilac  = Color(0xFFF5F3FF)
private val BadgeSand  = Color(0xFFFAEEDF)

private fun fmtMinutes(total: Int): String =
    "%02d:%02d".format(total / 60, total % 60)

private val dayFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalendarDayEventsScreen(
    dateStr: String,
    vm: EventsViewModel,
    onBack: () -> Unit,
    onEventClick: (String) -> Unit,
) {
    val state by vm.state.collectAsState()
    val events = state.events
    var isLoading by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        isLoading = true
        vm.refresh()
        isLoading = false
    }

    val date = LocalDate.parse(dateStr)

    val dailyEvents = events
        .filter { ev -> ev.date.toLocalDate() == date }
        .sortedBy { it.startTimeMinutes }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Events on $dateStr", color = UPH_White) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, null, tint = UPH_White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = UPH_Navy)
            )
        }
    ) { pad ->

        val scrollState = rememberScrollState()

        Box(
            modifier = Modifier
                .padding(pad)
                .fillMaxSize()
        ) {
            if (isLoading) {
                Box(
                    Modifier
                        .fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(
                        color = UPH_Navy,
                        strokeWidth = 4.dp
                    )
                }
                return@Box
            }
            if (dailyEvents.isEmpty()) {
                Box(
                    Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text("No events found.", fontSize = 16.sp, color = UPH_Navy)
                }
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(scrollState)
                        .padding(horizontal = 16.dp, vertical = 12.dp)
                ) {
                    dailyEvents.forEach { ev ->
                        AdminEventCard(
                            event = ev,
                            onClick = { onEventClick(ev.id) }
                        )
                        Spacer(Modifier.height(12.dp))
                    }
                }

                VerticalScrollbar(
                    modifier = Modifier
                        .align(Alignment.CenterEnd)
                        .padding(end = 4.dp),
                    scroll = scrollState
                )
            }
        }
    }
}

@Composable
private fun AdminEventCard(
    event: Event,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(24.dp),
        color = CardLilac,
        shadowElevation = 2.dp
    ) {
        Column(Modifier.padding(16.dp)) {
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    event.name,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = UPH_Navy,
                    maxLines = 2
                )

                StatusBadgeForAdmin(event)
            }

            Spacer(Modifier.height(8.dp))

            Text(
                "Held By: ${event.heldBy}",
                fontSize = 14.sp,
                color = UPH_Navy
            )
            Text(
                "Date: ${event.date.toLocalDate().format(dayFormatter)}",
                fontSize = 14.sp,
                color = UPH_Navy
            )
            Text(
                "Time: ${fmtMinutes(event.startTimeMinutes)} – ${fmtMinutes(event.endTimeMinutes)}",
                fontSize = 14.sp,
                color = UPH_Navy
            )
            Text(
                "Room: ${event.room}",
                fontSize = 14.sp,
                color = UPH_Navy
            )
        }
    }
}

@Composable
private fun StatusBadgeForAdmin(event: Event) {
    val today = LocalDate.now()
    val eventDate = event.date.toLocalDate()

    val label = when {
        eventDate.isAfter(today) -> "Coming Soon"
        eventDate.isEqual(today) -> "Coming Soon"
        else -> "Past"
    }

    Box(
        modifier = Modifier
            .background(
                color = BadgeSand,
                shape = RoundedCornerShape(50)
            )
            .padding(horizontal = 12.dp, vertical = 4.dp)
    ) {
        Text(
            text = label,
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium,
            color = UPH_Navy
        )
    }
}
