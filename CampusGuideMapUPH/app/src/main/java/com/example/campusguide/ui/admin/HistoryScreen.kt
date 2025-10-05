// HistoryScreen.kt (atau file tempat kamu menaruh layar History)
package com.example.campusguide.ui.admin

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.campusguide.data.Event
import androidx.compose.ui.text.style.TextAlign

private val NO_COL_WIDTH = 28.dp

// Menampilkan past events
@Composable
fun HistoryScreen(
    vm: EventsViewModel,
    onBack: () -> Unit
) {
    var items by remember { mutableStateOf<List<Event>>(emptyList()) }
    var error by remember { mutableStateOf<String?>(null) }
    var loading by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        vm.loadPastEvents(
            onDone = {
                loading = false
                items = it.sortedWith(compareBy<Event>({ e -> e.date.toDate() }, { e -> e.startTimeMinutes }))
            },
            onError = { msg ->
                loading = false
                error = msg
            }
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
            Text(
                "History (Past Events)",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.height(10.dp))

            Row(
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("No.", modifier = Modifier.width(NO_COL_WIDTH), fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                Text("Event Name", modifier = Modifier.weight(1.6f), fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                Text("Held By", modifier = Modifier.weight(1.2f), fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                Text(
                    "Schedule",
                    modifier = Modifier.weight(2.2f),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
            Divider()
            Spacer(Modifier.height(4.dp))

            LazyColumn(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                itemsIndexed(items) { idx, ev ->
                    ElevatedCard {
                        Row(
                            Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 12.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("${idx + 1}", modifier = Modifier.width(NO_COL_WIDTH), fontSize = 12.sp)

                            Text(
                                ev.name,
                                modifier = Modifier.weight(1.6f),
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
                            Spacer(Modifier.height(4.dp))

                            Column(
                                modifier = Modifier.weight(2.2f),
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
                        }
                    }
                }
            }

            if (loading) {
                Spacer(Modifier.height(8.dp))
                LinearProgressIndicator(Modifier.fillMaxWidth())
            }
        }
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
