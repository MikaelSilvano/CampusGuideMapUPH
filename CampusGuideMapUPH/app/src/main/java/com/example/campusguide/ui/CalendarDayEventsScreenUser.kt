package com.example.campusguide.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.campusguide.data.Event
import com.example.campusguide.ui.admin.EventsViewModel
import com.example.campusguide.ui.common.VerticalScrollbar
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import com.example.campusguide.data.CampusEvent
import java.time.ZoneId

private val UPH_Navy = Color(0xFF16224C)
private val UPH_White = Color(0xFFFFFFFF)
private val CardColor = Color(0xFFF2F3F7)

private val fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd")

private fun fmtMinutes(x: Int): String = "%02d:%02d".format(x / 60, x % 60)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EventsCalendarUserDayScreen(
    dateStr: String,
    onBack: () -> Unit,
    onEventClick: (String) -> Unit,
    vm: EventsViewModel = viewModel()
) {
    val state by vm.state.collectAsState()
    var loading by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        loading = true
        vm.refresh()
        loading = false
    }

    val events = state.events.filter { it.published }
    val date = LocalDate.parse(dateStr)
    val dayEvents = events
        .filter { ev ->
            ev.published &&
                    ev.date.toDate().toInstant().atZone(ZoneId.systemDefault()).toLocalDate() == date
        }
        .map { ev ->
            CampusEvent(
                id = ev.id,
                name = ev.name,
                heldBy = ev.heldBy,
                buildingId = ev.building,
                room = ev.room,
                start = ev.date.toDate()
                    .toInstant()
                    .atZone(ZoneId.systemDefault())
                    .toLocalDate()
                    .atTime(ev.startTimeMinutes / 60, ev.startTimeMinutes % 60),
                end = ev.date.toDate()
                    .toInstant()
                    .atZone(ZoneId.systemDefault())
                    .toLocalDate()
                    .atTime(ev.endTimeMinutes / 60, ev.endTimeMinutes % 60),
                category = "General"
            )
        }
        .sortedBy { it.start }

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
            ) },
        containerColor = Color.Transparent
    ) { pad ->
        val scroll = rememberScrollState()

        Box(
            Modifier
                .padding(pad)
                .fillMaxSize()
        ) {
            if (loading) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = UPH_Navy)
                }
            } else if (dayEvents.isEmpty()) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("No events found.", fontSize = 16.sp, color = UPH_Navy)
                }
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(scroll)
                        .padding(16.dp)
                ) {
                    dayEvents.forEach { ev ->
                        UserEventCard(
                            ev = ev,
                            onClick = { onEventClick(ev.id) }
                        )
                        Spacer(Modifier.height(12.dp))
                    }
                }

                VerticalScrollbar(
                    scroll = scroll,
                    modifier = Modifier.align(Alignment.CenterEnd).padding(end = 4.dp)
                )
            }
        }
    }
}

@Composable
private fun UserEventCard(
    ev: CampusEvent,
    onClick: () -> Unit
) {
    val (label, bg) = StatusBadge(ev)

    Surface(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
        shape = RoundedCornerShape(20.dp),
        color = CardColor,
        shadowElevation = 2.dp
    ) {
        Column(Modifier.padding(16.dp)) {

            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(ev.name, fontSize = 18.sp, fontWeight = FontWeight.Bold)

                Box(
                    Modifier.background(bg, RoundedCornerShape(12.dp)).padding(6.dp)
                ) { Text(label, fontSize = 12.sp) }
            }

            Spacer(Modifier.height(6.dp))
            Text("Held By: ${ev.heldBy}")
            Text("Date: ${ev.start.toLocalDate()}")
            Text("Time: ${ev.start.toLocalTime()} – ${ev.end.toLocalTime()}")
            Text("Room: ${ev.room}")
        }
    }
}


