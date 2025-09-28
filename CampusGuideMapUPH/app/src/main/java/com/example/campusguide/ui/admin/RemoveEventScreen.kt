package com.example.campusguide.ui.admin

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.campusguide.data.Event

@Composable
fun RemoveEventScreen(
    vm: EventsViewModel,
    onBack: () -> Unit
) {
    val state by vm.state.collectAsState()
    LaunchedEffect(Unit) { vm.refresh() }

    var targetId by remember { mutableStateOf<String?>(null) }
    var error by remember { mutableStateOf<String?>(null) }
    var success by remember { mutableStateOf(false) }

    val events = remember(state.events) {
        state.events.sortedWith(
            compareBy<Event>({ it.date.toDate() }, { it.startTimeMinutes })
        )
    }

    Scaffold(
        bottomBar = {
            BottomAppBar {
                Spacer(Modifier.weight(1f))
                OutlinedButton(onClick = onBack) { Text("Back") }
            }
        }
    ) { pad ->
        Column(Modifier.padding(pad).padding(16.dp)) {
            Text("Remove Event", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(12.dp))

            Row(Modifier.fillMaxWidth().padding(horizontal = 8.dp)) {
                Text("No.", modifier = Modifier.width(44.dp), fontWeight = FontWeight.SemiBold)
                Text("Event Name", modifier = Modifier.weight(2f), fontWeight = FontWeight.SemiBold)
                Text("Held By", modifier = Modifier.weight(1.4f), fontWeight = FontWeight.SemiBold)
                Text("Time", modifier = Modifier.weight(1.2f), fontWeight = FontWeight.SemiBold)
                Text("Date", modifier = Modifier.weight(1.4f), fontWeight = FontWeight.SemiBold)
                Spacer(Modifier.width(40.dp))
            }
            Divider()
            Spacer(Modifier.height(6.dp))

            LazyColumn(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                itemsIndexed(events) { idx, ev ->
                    ElevatedCard {
                        Row(
                            Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 12.dp, vertical = 10.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text("${idx + 1}", modifier = Modifier.width(44.dp))
                            Text(ev.name, modifier = Modifier.weight(2f))
                            Text(ev.heldBy, modifier = Modifier.weight(1.4f))
                            Text("${fmtTime(ev.startTimeMinutes)}–${fmtTime(ev.endTimeMinutes)}",
                                modifier = Modifier.weight(1.2f))
                            Text(fmtDate(ev), modifier = Modifier.weight(1.4f))

                            IconButton(onClick = { targetId = ev.id }) {
                                Icon(Icons.Default.Delete, contentDescription = "Delete")
                            }
                        }
                    }
                }
            }

            if (state.loading) {
                Spacer(Modifier.height(8.dp))
                LinearProgressIndicator(Modifier.fillMaxWidth())
            }
        }
    }

    if (targetId != null) {
        AlertDialog(
            onDismissRequest = { targetId = null },
            title = { Text("Are you sure?") },
            text = { Text("This will permanently delete the event.") },
            confirmButton = {
                TextButton(onClick = {
                    val id = targetId!!
                    targetId = null
                    vm.delete(
                        id,
                        onDone = { success = true },
                        onError = { msg -> error = msg }
                    )
                }) { Text("Yes, delete") }
            },
            dismissButton = {
                TextButton(onClick = { targetId = null }) { Text("Cancel") }
            }
        )
    }

    if (success) {
        AlertDialog(
            onDismissRequest = { success = false },
            title = { Text("Event deleted") },
            confirmButton = { TextButton(onClick = { success = false }) { Text("OK") } }
        )
    }
    error?.let { msg ->
        AlertDialog(
            onDismissRequest = { error = null },
            title = { Text("Failed") },
            text = { Text(msg) },
            confirmButton = { TextButton(onClick = { error = null }) { Text("OK") } }
        )
    }
}

private fun fmtTime(min: Int) = "%02d:%02d".format(min / 60, min % 60)
private fun fmtDate(ev: Event): String {
    val d = ev.date.toDate()
    val y = d.year + 1900
    val m = d.month + 1
    val day = d.date
    return "%04d-%02d-%02d".format(y, m, day)
}
