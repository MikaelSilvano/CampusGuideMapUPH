package com.example.campusguide.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.example.campusguide.data.CampusEvent
import com.example.campusguide.data.Event
import com.example.campusguide.ui.admin.EventsViewModel
import java.time.LocalDate
import java.time.YearMonth
import java.time.ZoneId

private val UPH_Navy = Color(0xFF16224C)
private val UPH_Orange = Color(0xFFF58A0A)
private val UPH_White = Color(0xFFFFFFFF)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EventsCalendarUserScreen(
    nav: NavHostController,
    vm: EventsViewModel = androidx.lifecycle.viewmodel.compose.viewModel()
) {
    val state by vm.state.collectAsState()
    val events = state.events
        .filter { it.published }
        .map { ev ->
            CampusEvent(
                id = ev.id,
                name = ev.name,
                heldBy = ev.heldBy,
                buildingId = ev.building,
                room = ev.room,
                start = ev.date.toDate()
                    .toInstant().atZone(ZoneId.systemDefault())
                    .toLocalDate()
                    .atTime(ev.startTimeMinutes / 60, ev.startTimeMinutes % 60),
                end = ev.date.toDate()
                    .toInstant().atZone(ZoneId.systemDefault())
                    .toLocalDate()
                    .atTime(ev.endTimeMinutes / 60, ev.endTimeMinutes % 60),
                category = "General"
            )
        }

    LaunchedEffect(Unit) { vm.refresh() }

    var currentMonth by remember { mutableStateOf(YearMonth.now()) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Calendar", color = UPH_White) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = UPH_Navy)
            )
        }
    ) { pad ->
        Column(
            Modifier
                .padding(pad)
                .padding(16.dp)
        ) {
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { currentMonth = currentMonth.minusMonths(1) }) {
                    Icon(Icons.Default.ChevronLeft, null, tint = UPH_Navy)
                }

                Text(
                    text = "${currentMonth.month.name.lowercase().replaceFirstChar { it.uppercase() }} ${currentMonth.year}",
                    fontSize = 20.sp,
                    color = UPH_Navy,
                    fontWeight = FontWeight.Bold
                )

                IconButton(onClick = { currentMonth = currentMonth.plusMonths(1) }) {
                    Icon(Icons.Default.ChevronRight, null, tint = UPH_Navy)
                }
            }

            Spacer(Modifier.height(12.dp))

            val weekdayLabels = listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")
            Row(Modifier.fillMaxWidth()) {
                weekdayLabels.forEach {
                    Box(Modifier.weight(1f), contentAlignment = Alignment.Center) {
                        Text(it, color = UPH_Navy, fontWeight = FontWeight.Bold)
                    }
                }
            }

            Spacer(Modifier.height(8.dp))

            val firstDay = currentMonth.atDay(1)
            val firstDow = firstDay.dayOfWeek.value - 1
            val daysInMonth = currentMonth.lengthOfMonth()

            val totalCells = 42
            val cells = List(totalCells) { idx ->
                val num = idx - firstDow + 1
                if (num in 1..daysInMonth) currentMonth.atDay(num) else null
            }

            EventsCalendarGridUser(
                dayCells = cells,
                events = events,
                onSelectDate = { date ->
                    nav.navigate("user_calendar_day/$date")
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            )
        }
    }
}

@Composable
private fun EventsCalendarGridUser(
    dayCells: List<LocalDate?>,
    events: List<CampusEvent>,
    onSelectDate: (LocalDate) -> Unit,
    modifier: Modifier
) {
    Column(modifier = modifier.fillMaxHeight()) {
        for (row in 0 until 6) {
            Row(Modifier.fillMaxWidth().weight(1f)) {
                for (col in 0 until 7) {
                    val date = dayCells[row * 7 + col]
                    EventsCalendarDateCellUser(
                        date = date,
                        events = events,
                        onSelectDate = onSelectDate
                    )
                }
            }
        }
    }
}

@Composable
private fun RowScope.EventsCalendarDateCellUser(
    date: LocalDate?,
    events: List<CampusEvent>,
    onSelectDate: (LocalDate) -> Unit
) {
    val cellEvents =
        if (date != null)
            events.filter { ev ->
                ev.start.toLocalDate() == date
            }
        else emptyList()

    Box(
        modifier = Modifier
            .weight(1f)
            .aspectRatio(1f)
            .padding(2.dp)
            .clickable(enabled = date != null) { date?.let(onSelectDate) }
            .background(
                if (cellEvents.isNotEmpty()) UPH_Navy.copy(alpha = 0.12f) else Color.Transparent,
                RoundedCornerShape(12.dp)
            )
            .padding(4.dp)
    ) {
        Column(Modifier.fillMaxSize()) {
            Text(
                text = date?.dayOfMonth?.toString() ?: "",
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = UPH_Navy
            )

            Spacer(Modifier.height(4.dp))

            Column(Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                cellEvents.take(2).forEach { ev ->
                    Box(
                        Modifier
                            .fillMaxWidth()
                            .background(UPH_Orange, RoundedCornerShape(12.dp))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(ev.name, fontSize = 11.sp, color = UPH_White, maxLines = 1)
                    }
                }

                if (cellEvents.size > 2) {
                    Text("+${cellEvents.size - 2} more", fontSize = 10.sp, color = UPH_Navy)
                }
            }
        }
    }
}
