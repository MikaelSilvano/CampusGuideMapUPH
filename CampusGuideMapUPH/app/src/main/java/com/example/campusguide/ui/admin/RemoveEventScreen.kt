package com.example.campusguide.ui.admin

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.campusguide.data.Event

private val NO_COL_WIDTH = 28.dp
private val ACTIONS_COL_WIDTH = 84.dp

// Layar admin untuk melihat daftar event dan melakukan aksi
@Composable
fun RemoveEventScreen(
    vm: EventsViewModel,
    onBack: () -> Unit
) {
    val state by vm.state.collectAsState()
    LaunchedEffect(Unit) { vm.refresh() } // Memuat ulang data saat layar dibuka

    var targetId by remember { mutableStateOf<String?>(null) }
    var error by remember { mutableStateOf<String?>(null) }
    var success by remember { mutableStateOf(false) }

    // Urutkan event berdasarkan tanggal dan waktu mulai
    val events = remember(state.events) {
        state.events.sortedWith(
            compareBy<Event>({ it.date.toDate() }, { it.startTimeMinutes })
        )
    }

    Scaffold(
        bottomBar = {
            BottomAppBar {
                Spacer(Modifier.weight(1f))
                OutlinedButton(onClick = onBack) { Text("Back") } // Kembali ke layar sebelumnya
            }
        }
    ) { pad ->
        Column(Modifier.padding(pad).padding(16.dp)) {
            Text(
                "Remove / Unpublish Event",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.height(10.dp))

            Row(
                Modifier.fillMaxWidth().padding(horizontal = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("No.", modifier = Modifier.width(NO_COL_WIDTH), fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                Text("Event Name", modifier = Modifier.weight(1.7f), fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                Text("Held By", modifier = Modifier.weight(1.2f), fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                Text("Schedule", modifier = Modifier.weight(1.9f), fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                Box(Modifier.width(ACTIONS_COL_WIDTH)) {
                    Text("Actions", fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                }
            }
            Divider()
            Spacer(Modifier.height(4.dp))

            // Daftar event
            LazyColumn(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                itemsIndexed(events) { idx, ev ->
                    ElevatedCard {
                        Row(
                            Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 10.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("${idx + 1}", modifier = Modifier.width(NO_COL_WIDTH), fontSize = 12.sp)

                            Text(
                                ev.name,
                                modifier = Modifier.weight(1.7f),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                fontSize = 12.sp
                            )

                            Text(
                                ev.heldBy,
                                modifier = Modifier.weight(1.2f),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                fontSize = 12.sp
                            )

                            Column(
                                modifier = Modifier.weight(1.9f),
                                verticalArrangement = Arrangement.spacedBy(2.dp)
                            ) {
                                Text(
                                    fmtDate(ev),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    fontSize = 12.sp
                                )
                                Text(
                                    "${fmtTime(ev.startTimeMinutes)}–${fmtTime(ev.endTimeMinutes)}",
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    fontSize = 12.sp
                                )
                            }

                            // Publish/unpublish dan delete
                            Row(
                                modifier = Modifier.width(ACTIONS_COL_WIDTH),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceEvenly
                            ) {
                                IconButton(onClick = {
                                    vm.setPublished(
                                        ev.id, !ev.published,
                                        onDone = {},
                                        onError = { error = it }
                                    )
                                }) {
                                    Icon(
                                        if (ev.published) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                        contentDescription = "Toggle visibility"
                                    )
                                }
                                IconButton(onClick = { targetId = ev.id }) {
                                    Icon(Icons.Default.Delete, contentDescription = "Delete")
                                }
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

    // Dialog konfirmasi hapus
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

    // Dialog sukses hapus
    if (success) {
        AlertDialog(
            onDismissRequest = { success = false },
            title = { Text("Event deleted") },
            confirmButton = { TextButton(onClick = { success = false }) { Text("OK") } }
        )
    }

    // Dialog error
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
