package com.example.campusguide.ui.admin

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.campusguide.data.Event
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.sp
import java.util.Calendar

private val UPH_Navy   = Color(0xFF16224C)
private val UPH_Red    = Color(0xFFB33A2E)
private val UPH_Ivory  = Color(0xFFF6F3FA)
private val ChipBlue   = Color(0xFFDDEBFF)
private val ChipGreen  = Color(0xFFE6F7E9)
private val ChipSand   = Color(0xFFFAEEDF)

private val UPH_Orange = Color(0xFFF58A0A)

// Admin dashboard
@Composable
fun DashboardScreen(
    vm: EventsViewModel,
    onAdd: ()->Unit,
    onEdit: (String)->Unit,
    onRemove: (String)->Unit,
    onHistory: ()->Unit
) {
    val state by vm.state.collectAsState()
    LaunchedEffect(Unit) { vm.refresh() }

    val events = remember(state.events) {
        state.events.sortedWith(
            compareBy<Event>({ it.date.toDate().time }).thenBy { it.startTimeMinutes }
        )
    }

    var selectedId by remember { mutableStateOf<String?>(null) }
    LaunchedEffect(events) {
        if (selectedId != null && events.none { it.id == selectedId }) selectedId = null
    }

    Scaffold(
        bottomBar = {
            Surface(color = UPH_Ivory) {
                Row(
                    Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 5.dp, vertical = 5.dp),
                    horizontalArrangement = Arrangement.spacedBy(5.dp),
                    verticalAlignment = Alignment.CenterVertically
                )  {
                    ActionButton(
                        text = "Add",
                        container = UPH_Navy,
                        content = Color.White,
                        enabled = true,
                        onClick = onAdd
                    )
                    ActionButton(
                        text = "Edit",
                        container = UPH_Orange,
                        content = Color.White,
                        enabled = selectedId != null,
                        onClick = { selectedId?.let(onEdit) }
                    )
                    ActionButton(
                        text = "Remove",
                        container = UPH_Red,
                        content = Color.White,
                        enabled = selectedId != null,
                        onClick = { selectedId?.let(onRemove) }
                    )
                    ActionButton(
                        text = "History",
                        container = Color(0xFF6C757D),
                        content = Color.White,
                        enabled = true,
                        onClick = { onHistory() }
                    )
                }
            }
        }
    ) { pad ->
        Column(
            Modifier
                .padding(pad)
                .fillMaxSize()
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            Text(
                "Dashboard",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.height(12.dp))

            Surface(
                color = UPH_Navy,
                shape = MaterialTheme.shapes.large,
                tonalElevation = 2.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(Modifier.padding(16.dp)) {
                    Text(
                        "Active Events",
                        color = Color.White,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(Modifier.height(12.dp))

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(18.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        CountChip("Ongoing", state.countOngoing, ChipBlue)
                        CountChip("Upcoming", state.countUpcoming, ChipGreen)
                        CountChip("Coming Soon", state.countComingSoon, ChipSand)
                    }
                }
            }

            Spacer(Modifier.height(16.dp))

            EventTable(
                events = events,
                selectedId = selectedId,
                onSelect = { id -> selectedId = id }
            )
        }
    }
}

// Komponen tabel daftar event
@Composable
private fun EventTable(
    events: List<Event>,
    selectedId: String?,
    onSelect: (String) -> Unit
) {
    Surface(
        shape = MaterialTheme.shapes.large,
        tonalElevation = 1.dp,
        shadowElevation = 2.dp,
        color = Color.White
    ) {
        Column {
            Row(
                Modifier
                    .fillMaxWidth()
                    .background(UPH_Navy)
                    .padding(horizontal = 12.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                HeadCell("No.", 0.8f)
                HeadCell("Event Name", 1.8f)
                HeadCell("Held By", 1.2f)
                HeadCell("Time", 2.0f)
                Spacer(Modifier.width(5.dp))
                HeadCell("Date", 1.2f)
            }

            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(0.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                itemsIndexed(events, key = { _, e -> e.id }) { idx, e ->
                    val selected = selectedId == e.id
                    TableRow(
                        index = idx + 1,
                        e = e,
                        selected = selected,
                        onClick = { onSelect(e.id) }
                    )
                    if (idx < events.lastIndex) {
                        Divider(thickness = 0.5.dp, color = Color(0xFFE5E6EC))
                    }
                }
                item { Spacer(Modifier.height(2.dp)) }
            }
        }
    }
}

// Tombol aksi di bar bawah
@Composable
private fun RowScope.ActionButton(
    text: String,
    container: Color,
    content: Color,
    enabled: Boolean,
    onClick: ()->Unit
) {
    val shape = MaterialTheme.shapes.large
    Button(
        onClick = onClick,
        enabled = enabled,
        shape = shape,
        colors = ButtonDefaults.buttonColors(
            containerColor = container,
            contentColor = content,
            disabledContainerColor = container.copy(alpha = 0.5f),
            disabledContentColor = content.copy(alpha = 0.6f)
        ),
        modifier = Modifier
            .height(52.dp)
            .weight(1f)
    ) {
        Text(
            text,
            fontWeight = FontWeight.SemiBold,
            fontSize = 12.sp,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

// Menampilkan jumlah event per kategori
@Composable
private fun CountChip(label: String, value: Int, bg: Color) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.widthIn(min = 96.dp)
    ) {
        Surface(color = bg, shape = MaterialTheme.shapes.large) {
            Text(
                text = label,
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                fontWeight = FontWeight.SemiBold
            )
        }
        Spacer(Modifier.height(8.dp))
        Text(
            "$value",
            color = Color.White,
            fontWeight = FontWeight.Bold,
            style = MaterialTheme.typography.headlineMedium
        )
    }
}

@Composable
private fun TableHeader() {
    Row(
        Modifier
            .fillMaxWidth()
            .background(UPH_Navy)
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        HeadCell("No.", 0.8f)
        HeadCell("Event Name", 2.2f)
        HeadCell("Held By", 1.6f)
        HeadCell("Time", 1.4f)
        Spacer(Modifier.width(12.dp))
        HeadCell("Date", 1.4f)
    }
}

@Composable
private fun RowScope.HeadCell(text: String, weight: Float) {
    Text(
        text = text,
        color = Color.White,
        modifier = Modifier.weight(weight),
        fontWeight = FontWeight.SemiBold,
        fontSize = 12.sp,
        maxLines = 1
    )
}

@Composable
private fun TableRow(index: Int, e: Event, selected: Boolean, onClick: () -> Unit) {
    val bg = if (selected) Color(0xFFECE8FF) else Color.Transparent
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(bg)
            .clickable(
                indication = null,
                interactionSource = remember { MutableInteractionSource() }
            ) { onClick() }
            .padding(horizontal = 12.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        BodyCell(index.toString(), 0.8f, FontWeight.SemiBold)
        BodyCell(e.name, 1.8f)
        BodyCell(e.heldBy, 1.2f)
        BodyCell("${fmtTime(e.startTimeMinutes)}–${fmtTime(e.endTimeMinutes)}", 2.0f)
        Spacer(Modifier.width(5.dp))
        BodyCell(fmtDate(e.date), 1.2f)
    }
}

@Composable
private fun RowScope.BodyCell(text: String, weight: Float, weightBold: FontWeight? = null) {
    Text(
        text = text,
        modifier = Modifier.weight(weight),
        fontWeight = weightBold ?: FontWeight.Normal,
        fontSize = 12.sp,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis
    )
}

// Utility function
private fun fmtTime(min: Int): String = "%02d:%02d".format(min / 60, min % 60)

private fun fmtDate(ts: com.google.firebase.Timestamp): String {
    val c = Calendar.getInstance().apply { time = ts.toDate() }
    return "%04d-%02d-%02d".format(
        c.get(Calendar.YEAR),
        c.get(Calendar.MONTH) + 1,
        c.get(Calendar.DAY_OF_MONTH)
    )
}
