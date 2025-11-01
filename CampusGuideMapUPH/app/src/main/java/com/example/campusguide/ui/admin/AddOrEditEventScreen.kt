package com.example.campusguide.ui.admin

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import com.example.campusguide.data.Event
import com.google.firebase.Timestamp
import java.util.Calendar
import androidx.compose.runtime.produceState

// Mode form, yaitu tambah baru atau edit
sealed interface FormMode { data object Add : FormMode; data class Edit(val id: String): FormMode }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddOrEditEventScreen(
    mode: FormMode,
    vm: EventsViewModel,
    onDone: () -> Unit,
    onCancel: () -> Unit
) {
    val state by vm.state.collectAsState()
    val eventId = (mode as? FormMode.Edit)?.id
    val editing = eventId?.let { id -> state.events.firstOrNull { it.id == id } }

    LaunchedEffect(eventId) {
        if (eventId != null && editing == null) vm.refresh()
    }

    if (eventId != null && editing == null) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }

    val eventKey = eventId ?: "ADD"

    var name   by remember(eventKey) { mutableStateOf(TextFieldValue(editing?.name ?: "")) }
    var heldBy by remember(eventKey) { mutableStateOf(TextFieldValue(editing?.heldBy ?: "")) }

    var date by remember(eventKey) {
        mutableStateOf<Calendar?>(
            editing?.date?.toDate()?.let { Calendar.getInstance().apply { time = it } }
        )
    }
    var startMin by remember(eventKey) { mutableStateOf<Int?>(editing?.startTimeMinutes) }
    var endMin   by remember(eventKey) { mutableStateOf<Int?>(editing?.endTimeMinutes) }

    var building by remember(eventKey) { mutableStateOf<String?>(editing?.building) }
    var floor    by remember(eventKey) { mutableStateOf<Int?>(editing?.floor) }
    var room     by remember(eventKey) { mutableStateOf<String?>(editing?.room?.takeIf { it.isNotBlank() }) }

    val campusRepo = com.example.campusguide.data.InMemoryCampusRepository

    val buildingItems by produceState(initialValue = emptyList<com.example.campusguide.data.Building>()) {
        value = campusRepo.getBuildings()
    }
    val buildingIds = remember(buildingItems) { buildingItems.map { it.id } }

    val floors by produceState(initialValue = emptyList<Int>(), key1 = building) {
        value = building?.let { campusRepo.getFloors(it) } ?: emptyList()
    }

    val rooms by produceState(initialValue = emptyList<String>(), key1 = building, key2 = floor) {
        value = if (building != null && floor != null) {
            campusRepo.rooms
                .filter { it.buildingId == building && it.floor == floor }
                .map { it.code }
        } else emptyList()
    }

    LaunchedEffect(building) {
        if (building == null) {
            floor = null
            room = null
        } else if (floor !in floors) {
            floor = null
            room = null
        }
    }
    LaunchedEffect(building, floor) {
        if (building == null || floor == null || room !in rooms) room = null
    }

    var poster: Uri? by remember { mutableStateOf(null) }
    val pickImage = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        if (uri != null) poster = uri
    }

    var askConfirm by remember { mutableStateOf(false) }
    var success    by remember { mutableStateOf(false) }
    var error      by remember { mutableStateOf<String?>(null) }

    val isValid = name.text.isNotBlank() &&
            heldBy.text.isNotBlank() &&
            date != null &&
            startMin != null &&
            endMin != null &&
            building != null &&
            floor != null &&
            room != null

    Scaffold(
        bottomBar = {
            BottomAppBar {
                Spacer(Modifier.weight(1f))
                Button(onClick = { askConfirm = true }, enabled = isValid) { Text("Confirm") }
                Spacer(Modifier.width(8.dp))
                OutlinedButton(onClick = onCancel) { Text("Cancel") }
            }
        }
    ) { pad ->
        val scroll = rememberScrollState()
        Column(
            Modifier
                .padding(pad)
                .consumeWindowInsets(pad)
                .verticalScroll(scroll)
                .imePadding()
                .navigationBarsPadding()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(if (eventId == null) "Add Event" else "Edit Event", style = MaterialTheme.typography.titleLarge)

            OutlinedTextField(
                value = name, onValueChange = { name = it },
                label = { Text("Event Name") }, placeholder = { Text("Enter event name") },
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = heldBy, onValueChange = { heldBy = it },
                label = { Text("Held By") }, placeholder = { Text("Organizer / Faculty") },
                modifier = Modifier.fillMaxWidth()
            )

            DateTimePickers(
                date = date, onDateChange = { date = it },
                startMinutes = startMin, onStartChange = { startMin = it },
                endMinutes = endMin, onEndChange = { endMin = it }
            )

            LocationPickers(
                building = building, onBuilding = { building = it },
                floor = floor, onFloor = { floor = it },
                room = room, onRoom = { room = it },
                buildings = buildingIds,
                floors = floors,
                rooms = rooms
            )

            Row {
                OutlinedButton(onClick = { pickImage.launch("image/*") }) {
                    Text(if (poster != null) "Change Poster" else "Choose Poster")
                }
                Spacer(Modifier.width(12.dp))
                Text(text = if (poster != null) "Selected ✓" else (editing?.posterUrl?.let { "Already uploaded" } ?: "No poster"))
            }

            error?.let { Text(it, color = MaterialTheme.colorScheme.error) }
            Spacer(Modifier.height(80.dp))
        }
    }

    if (askConfirm) {
        AlertDialog(
            onDismissRequest = { askConfirm = false },
            title = { Text("Are you sure?") },
            confirmButton = {
                TextButton(onClick = {
                    askConfirm = false
                    val normalized = (date!!.clone() as Calendar).apply {
                        set(Calendar.HOUR_OF_DAY, 0)
                        set(Calendar.MINUTE, 0)
                        set(Calendar.SECOND, 0)
                        set(Calendar.MILLISECOND, 0)
                    }
                    val ts = Timestamp(normalized.time)

                    if (eventId == null) {
                        val toCreate = Event(
                            id = "",
                            name = name.text.trim(),
                            heldBy = heldBy.text.trim(),
                            date = ts,
                            startTimeMinutes = startMin!!,
                            endTimeMinutes   = endMin!!,
                            building = building!!,
                            floor    = floor!!,
                            room     = room!!,
                            posterUrl = editing?.posterUrl
                        )
                        vm.create(toCreate, poster, onDone = { success = true }, onError = { error = it })
                    } else {
                        val original = editing!!
                        val updated = original.copy(
                            name = name.text.trim(),
                            heldBy = heldBy.text.trim(),
                            date = ts,
                            startTimeMinutes = startMin!!,
                            endTimeMinutes   = endMin!!,
                            building = building!!,
                            floor    = floor!!,
                            room     = room!!,
                            posterUrl = original.posterUrl
                        )
                        vm.update(updated, poster, onDone = { success = true }, onError = { error = it })
                    }
                }) { Text("Yes") }
            },
            dismissButton = { TextButton(onClick = { askConfirm = false }) { Text("No") } }
        )
    }

    if (success) {
        AlertDialog(
            onDismissRequest = { success = false; onDone() },
            title = { Text(if (eventId == null) "Event has been added" else "Event has been edited") },
            confirmButton = { TextButton(onClick = { success = false; onDone() }) { Text("OK") } }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DateTimePickers(
    date: Calendar?,
    onDateChange: (Calendar) -> Unit,
    startMinutes: Int?,
    onStartChange: (Int) -> Unit,
    endMinutes: Int?,
    onEndChange: (Int) -> Unit
) {
    var showDateDialog by remember { mutableStateOf(false) }
    val dateLabel = date?.let {
        "%02d/%02d/%04d".format(
            it.get(Calendar.DAY_OF_MONTH),
            it.get(Calendar.MONTH) + 1,
            it.get(Calendar.YEAR)
        )
    } ?: ""

    Box(modifier = Modifier.fillMaxWidth()) {
        OutlinedTextField(
            value = dateLabel,
            onValueChange = {},
            readOnly = true,
            label = { Text("Event Date") },
            placeholder = { Text("Select date") },
            modifier = Modifier.fillMaxWidth()
        )
        Box(
            Modifier
                .matchParentSize()
                .clickable { showDateDialog = true }
        )
    }

    if (showDateDialog) {
        val initialMillis = date?.timeInMillis
        val dpState = rememberDatePickerState(initialSelectedDateMillis = initialMillis)
        DatePickerDialog(
            onDismissRequest = { showDateDialog = false },
            confirmButton = {
                TextButton(onClick = {
                    dpState.selectedDateMillis?.let { millis ->
                        val newCal = Calendar.getInstance().apply {
                            timeInMillis = millis
                            set(Calendar.HOUR_OF_DAY, 0)
                            set(Calendar.MINUTE, 0)
                            set(Calendar.SECOND, 0)
                            set(Calendar.MILLISECOND, 0)
                        }
                        onDateChange(newCal)
                    }
                    showDateDialog = false
                }) { Text("OK") }
            },
            dismissButton = { TextButton(onClick = { showDateDialog = false }) { Text("Cancel") } }
        ) {
            DatePicker(state = dpState)
        }
    }

    Spacer(Modifier.height(8.dp))

    val times = remember { timeOptions(stepMinutes = 30) }
    var fromExpanded by remember { mutableStateOf(false) }
    var toExpanded by remember { mutableStateOf(false) }

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        ExposedDropdownMenuBox(
            expanded = fromExpanded,
            onExpandedChange = { fromExpanded = it },
            modifier = Modifier.weight(1f)
        ) {
            OutlinedTextField(
                value = startMinutes?.let { fmtMinutes(it) } ?: "",
                onValueChange = {},
                readOnly = true,
                label = { Text("Event Time (From)") },
                placeholder = { Text("Select time") },
                modifier = Modifier
                    .menuAnchor()
                    .fillMaxWidth()
            )
            ExposedDropdownMenu(expanded = fromExpanded, onDismissRequest = { fromExpanded = false }) {
                times.forEach { (label, minutes) ->
                    DropdownMenuItem(text = { Text(label) },
                        onClick = { onStartChange(minutes); fromExpanded = false })
                }
            }
        }

        ExposedDropdownMenuBox(
            expanded = toExpanded,
            onExpandedChange = { toExpanded = it },
            modifier = Modifier.weight(1f)
        ) {
            OutlinedTextField(
                value = endMinutes?.let { fmtMinutes(it) } ?: "",
                onValueChange = {},
                readOnly = true,
                label = { Text("To") },
                placeholder = { Text("Select time") },
                modifier = Modifier
                    .menuAnchor()
                    .fillMaxWidth()
            )
            ExposedDropdownMenu(expanded = toExpanded, onDismissRequest = { toExpanded = false }) {
                times.forEach { (label, minutes) ->
                    DropdownMenuItem(text = { Text(label) },
                        onClick = { onEndChange(minutes); toExpanded = false })
                }
            }
        }
    }
}

// Utility function
private fun timeOptions(stepMinutes: Int = 30): List<Pair<String, Int>> {
    val list = mutableListOf<Pair<String, Int>>()
    var m = 0
    val end = 24 * 60
    while (m < end) {
        list += fmtMinutes(m) to m
        m += stepMinutes
    }
    return list
}

private fun fmtMinutes(total: Int): String = "%02d:%02d".format(total / 60, total % 60)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun LocationPickers(
    building: String?, onBuilding: (String)->Unit,
    floor: Int?, onFloor: (Int)->Unit,
    room: String?, onRoom: (String)->Unit,
    buildings: List<String>, floors: List<Int>, rooms: List<String>
) {
    var bExpanded by remember { mutableStateOf(false) }
    var fExpanded by remember { mutableStateOf(false) }
    var rExpanded by remember { mutableStateOf(false) }

    // Pilih Building
    ExposedDropdownMenuBox(expanded = bExpanded, onExpandedChange = { bExpanded = it }) {
        OutlinedTextField(
            value = building?.let { "Building $it" } ?: "",
            onValueChange = {},
            readOnly = true,
            label = { Text("Event Location: Building") },
            placeholder = { Text("Select building") },
            modifier = Modifier.menuAnchor().fillMaxWidth()
        )
        ExposedDropdownMenu(expanded = bExpanded, onDismissRequest = { bExpanded = false }) {
            buildings.forEach { b ->
                DropdownMenuItem(text = { Text("Building $b") }, onClick = { onBuilding(b); bExpanded = false })
            }
        }
    }
    Spacer(Modifier.height(8.dp))

    // Pilih Floor
    val floorEnabled = building != null
    ExposedDropdownMenuBox(expanded = fExpanded && floorEnabled, onExpandedChange = { if (floorEnabled) fExpanded = it }) {
        OutlinedTextField(
            value = floor?.let { "Floor $it" } ?: "",
            onValueChange = {},
            readOnly = true,
            enabled = floorEnabled,
            label = { Text("Floor") },
            placeholder = { Text("Select floor") },
            modifier = Modifier.menuAnchor().fillMaxWidth()
        )
        if (floorEnabled) {
            ExposedDropdownMenu(expanded = fExpanded, onDismissRequest = { fExpanded = false }) {
                floors.forEach { fl ->
                    DropdownMenuItem(text = { Text("Floor $fl") }, onClick = { onFloor(fl); fExpanded = false })
                }
            }
        }
    }
    Spacer(Modifier.height(8.dp))

    // Pilih Room
    val roomEnabled = building != null && floor != null
    ExposedDropdownMenuBox(expanded = rExpanded && roomEnabled, onExpandedChange = { if (roomEnabled) rExpanded = it }) {
        OutlinedTextField(
            value = room ?: "",
            onValueChange = {},
            readOnly = true,
            enabled = roomEnabled,
            label = { Text("Room") },
            placeholder = { Text("Select room") },
            modifier = Modifier.menuAnchor().fillMaxWidth()
        )
        if (roomEnabled) {
            ExposedDropdownMenu(expanded = rExpanded, onDismissRequest = { rExpanded = false }) {
                rooms.forEach { r ->
                    DropdownMenuItem(text = { Text(r) }, onClick = { onRoom(r); rExpanded = false })
                }
            }
        }
    }
}
