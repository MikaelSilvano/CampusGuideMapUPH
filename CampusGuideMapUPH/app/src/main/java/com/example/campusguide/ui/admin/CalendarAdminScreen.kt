package com.example.campusguide.ui.admin

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
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
import com.example.campusguide.ui.toLocalDate
import java.time.LocalDate
import java.time.YearMonth

private val UPH_Navy   = Color(0xFF16224C)
private val UPH_Orange = Color(0xFFF58A0A)
private val UPH_White  = Color(0xFFFFFFFF)

private fun fmtMinutes(total: Int): String =
    "%02d:%02d".format(total / 60, total % 60)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalendarAdminScreen(
    vm: EventsViewModel,
    onBack: () -> Unit,
    onSelectDate: (LocalDate) -> Unit,
) {
    val state by vm.state.collectAsState()
    val events = state.events

    LaunchedEffect(Unit) {
        vm.refresh()
    }

    var currentMonth by remember { mutableStateOf(YearMonth.now()) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Admin Calendar", color = UPH_White) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            Icons.Default.ArrowBack,
                            contentDescription = null,
                            tint = UPH_White
                        )
                    }
                },
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
                    Icon(Icons.Default.ChevronLeft, contentDescription = null, tint = UPH_Navy)
                }

                Text(
                    text = "${currentMonth.month.name.lowercase().replaceFirstChar { it.uppercase() }} ${currentMonth.year}",
                    fontSize = 20.sp,
                    color = UPH_Navy,
                    fontWeight = FontWeight.Bold
                )

                IconButton(onClick = { currentMonth = currentMonth.plusMonths(1) }) {
                    Icon(Icons.Default.ChevronRight, contentDescription = null, tint = UPH_Navy)
                }
            }

            Spacer(Modifier.height(12.dp))

            val weekdayLabels = listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")
            Row(Modifier.fillMaxWidth()) {
                weekdayLabels.forEach { label ->
                    Box(
                        Modifier.weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(label, color = UPH_Navy, fontWeight = FontWeight.Bold)
                    }
                }
            }

            Spacer(Modifier.height(8.dp))

            val firstDay = currentMonth.atDay(1)
            val firstDowIndex = firstDay.dayOfWeek.value - 1
            val daysInMonth = currentMonth.lengthOfMonth()

            val totalCells = 42
            val dayCells = List(totalCells) { cellIndex ->
                val dayNumber = cellIndex - firstDowIndex + 1
                if (dayNumber in 1..daysInMonth) currentMonth.atDay(dayNumber) else null
            }

            CalendarGrid(
                dayCells = dayCells,
                events = events,
                onSelectDate = onSelectDate,
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            )
        }
    }
}

@Composable
private fun CalendarGrid(
    dayCells: List<LocalDate?>,
    events: List<Event>,
    onSelectDate: (LocalDate) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxHeight()
    ) {
        for (row in 0 until 6) {
            Row(
                Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) {
                for (col in 0 until 7) {
                    val date = dayCells[row * 7 + col]
                    CalendarDateCell(
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
private fun RowScope.CalendarDateCell(
    date: LocalDate?,
    events: List<Event>,
    onSelectDate: (LocalDate) -> Unit
) {
    val cellEvents =
        if (date != null) {
            events.filter { it.date.toLocalDate() == date }
        } else {
            emptyList()
        }

    Box(
        modifier = Modifier
            .weight(1f)
            .aspectRatio(1f)
            .padding(2.dp)
            .clickable(enabled = date != null) { date?.let(onSelectDate) }
            .background(
                if (cellEvents.isNotEmpty()) UPH_Navy.copy(alpha = 0.12f)
                else Color.Transparent,
                shape = RoundedCornerShape(12.dp)
            )
            .padding(4.dp)
    ) {
        Column(Modifier.fillMaxSize()) {
            Box(
                Modifier.fillMaxWidth(),
                contentAlignment = Alignment.TopStart
            ) {
                Text(
                    text = date?.dayOfMonth?.toString() ?: "",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = UPH_Navy
                )
            }

            Spacer(Modifier.height(4.dp))

            Column(
                Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                cellEvents.take(2).forEach { ev ->
                    ChipEvent(ev.name)
                }

                if (cellEvents.size > 2) {
                    Text(
                        "+${cellEvents.size - 2} more",
                        fontSize = 10.sp,
                        color = UPH_Navy
                    )
                }
            }
        }
    }
}

@Composable
private fun ChipEvent(text: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(UPH_Orange, RoundedCornerShape(12.dp))
            .padding(horizontal = 6.dp, vertical = 2.dp)
    ) {
        Text(
            text = text,
            fontSize = 11.sp,
            color = UPH_White,
            maxLines = 1
        )
    }
}
