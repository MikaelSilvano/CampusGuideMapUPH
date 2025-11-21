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
import com.example.campusguide.ui.common.UPHPrimaryButton
import com.example.campusguide.ui.common.UPHSecondaryButton
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.ButtonDefaults
import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.BorderStroke
import androidx.compose.material3.DatePickerDefaults
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlin.math.max
import android.content.Context
import android.provider.OpenableColumns
import android.webkit.MimeTypeMap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.window.Dialog
import androidx.compose.material3.Surface
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.shape.RoundedCornerShape
import java.net.URLDecoder
import java.nio.charset.StandardCharsets
import androidx.activity.compose.BackHandler
import com.example.campusguide.ui.common.VerticalScrollbar
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import java.io.File
import java.io.FileOutputStream
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

// Mode form, yaitu tambah baru atau edit

private val UPH_Navy = Color(0xFF16224C)
private val UPH_Red  = Color(0xFFE31E2E)
private val UPH_White = Color(0xFFFFFFFF)
private val UPH_Orange = Color(0xFFF58A0A)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun uphDatePickerColors() = DatePickerDefaults.colors(
    containerColor = Color(0xFFF8F9FD),

    titleContentColor = UPH_Navy,
    headlineContentColor = UPH_Navy,

    weekdayContentColor = UPH_Navy,
    subheadContentColor = UPH_Navy,
    dayContentColor = UPH_Navy,
    disabledDayContentColor = UPH_Navy.copy(alpha = 0.35f),

    todayDateBorderColor = UPH_Navy,
    todayContentColor = UPH_Navy,

    selectedDayContainerColor = UPH_Orange,
    selectedDayContentColor = UPH_White,

    dayInSelectionRangeContainerColor = UPH_Orange.copy(alpha = 0.18f),
    dayInSelectionRangeContentColor = UPH_Navy,

    yearContentColor = UPH_Navy,
    currentYearContentColor = UPH_Navy,
    selectedYearContainerColor = UPH_Orange,
    selectedYearContentColor = UPH_White,

    navigationContentColor = UPH_Navy
)

@Composable
private fun uphTextBtnColors() =
    ButtonDefaults.textButtonColors(contentColor = UPH_Navy)

@Composable
private fun uphTextFieldColors() = TextFieldDefaults.colors(
    focusedIndicatorColor   = UPH_Navy,
    unfocusedIndicatorColor = UPH_Navy,
    disabledIndicatorColor  = UPH_Navy.copy(alpha = 0.30f),
    focusedLabelColor       = UPH_Navy,
    cursorColor             = UPH_Navy,
    focusedTrailingIconColor = UPH_Navy,
    focusedContainerColor   = Color.Transparent,
    unfocusedContainerColor = Color.Transparent,
)

private fun resolveDisplayName(ctx: Context, uri: Uri): String {
    ctx.contentResolver.query(uri, arrayOf(OpenableColumns.DISPLAY_NAME), null, null, null)
        ?.use { c ->
            val idx = c.getColumnIndex(OpenableColumns.DISPLAY_NAME)
            if (idx >= 0 && c.moveToFirst()) {
                val name = c.getString(idx)
                if (!name.isNullOrBlank()) return name
            }
        }

    val last = uri.lastPathSegment?.substringAfterLast('/')?.takeIf { it.isNotBlank() }
    if (!last.isNullOrBlank()) return last

    val ext = MimeTypeMap.getSingleton()
        .getExtensionFromMimeType(ctx.contentResolver.getType(uri))
        ?.let { ".$it" } ?: ""
    return "poster$ext"
}

private fun filenameFromUrl(url: String?): String? {
    if (url.isNullOrBlank()) return null
    return try {
        val u = Uri.parse(url)
        val asString = u.toString()

        val oIdx = asString.indexOf("/o/")
        if (oIdx >= 0) {
            val afterO = asString.substring(oIdx + 3)
            val pathEncoded = afterO.substringBefore('?')
            val decoded = URLDecoder.decode(pathEncoded, StandardCharsets.UTF_8.name())
            return decoded.substringAfterLast('/').takeIf { it.isNotBlank() }
        }

        u.lastPathSegment?.substringAfterLast('/')?.takeIf { it.isNotBlank() }
    } catch (_: Exception) {
        null
    }
}

private fun isValidPosterUrl(u: String?): Boolean =
    !u.isNullOrBlank() && (u.startsWith("http://") || u.startsWith("https://"))

private val ALLOWED_MIME_TYPES = setOf(
    "image/jpeg",
    "image/jpg",
    "image/png"
)

private val ALLOWED_EXTENSIONS = setOf("jpg", "jpeg", "png")

private fun isAllowedImage(ctx: Context, uri: Uri, displayName: String? = null): Boolean {
    val type = ctx.contentResolver.getType(uri)?.lowercase()
    if (type != null && type in ALLOWED_MIME_TYPES) return true

    val name = (displayName ?: resolveDisplayName(ctx, uri)).lowercase()
    val dot = name.lastIndexOf('.')
    if (dot != -1 && dot + 1 < name.length) {
        val ext = name.substring(dot + 1)
        if (ext in ALLOWED_EXTENSIONS) return true
    }
    return false
}

// Kompres ke JPEG di cache dir
private suspend fun compressImageToTempFile(
    ctx: Context,
    uri: Uri,
    maxSizePx: Int = 1280,
    quality: Int = 80
): Uri? = withContext(Dispatchers.IO) {
    val resolver = ctx.contentResolver

    val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
    resolver.openInputStream(uri)?.use { input ->
        BitmapFactory.decodeStream(input, null, bounds)
    }

    if (bounds.outWidth <= 0 || bounds.outHeight <= 0) return@withContext null

    var sampleSize = 1
    while ((bounds.outWidth / sampleSize) > maxSizePx ||
        (bounds.outHeight / sampleSize) > maxSizePx
    ) {
        sampleSize *= 2
    }

    val opts = BitmapFactory.Options().apply { inSampleSize = sampleSize }
    val bitmap: Bitmap = resolver.openInputStream(uri)?.use { input ->
        BitmapFactory.decodeStream(input, null, opts)
    } ?: return@withContext null

    try {
        val outFile = File.createTempFile("poster_", ".jpg", ctx.cacheDir)
        FileOutputStream(outFile).use { out ->
            bitmap.compress(Bitmap.CompressFormat.JPEG, quality, out)
        }
        Uri.fromFile(outFile)
    } finally {
        bitmap.recycle()
    }
}

@Composable
private fun WorkingDialog(message: String) {
    Dialog(onDismissRequest = {}) {
        Surface(shape = RoundedCornerShape(16.dp), color = Color.White, tonalElevation = 6.dp) {
            Row(
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                CircularProgressIndicator(
                    color = UPH_Navy,
                    trackColor = UPH_Navy.copy(alpha = 0.15f)
                )
                Spacer(Modifier.width(16.dp))
                Text(message, color = UPH_Navy, style = MaterialTheme.typography.titleMedium)
            }
        }
    }
}

@Composable
fun LogoutConfirmDialog(
    visible: Boolean,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    if (!visible) return
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Are you sure you want to log out?") },
        confirmButton = {
            TextButton(
                onClick = onConfirm,
                colors = ButtonDefaults.textButtonColors(contentColor = Color(0xFF16224C)) // UPH_Navy
            ) { Text("Yes") }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                colors = ButtonDefaults.textButtonColors(contentColor = Color(0xFF16224C))
            ) { Text("No") }
        }
    )
}


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
    val ctx = LocalContext.current
    val repo = vm.repo

    var fullEvents by remember { mutableStateOf<List<Event>>(emptyList()) }

    LaunchedEffect(Unit) {
        fullEvents = repo.listAllAdmin()
    }

    val scope = rememberCoroutineScope()
    val eventId = (mode as? FormMode.Edit)?.id
    val editing = eventId?.let { id -> state.events.firstOrNull { it.id == id } }

    LaunchedEffect(eventId) {
        if (eventId != null && editing == null) vm.refresh()
    }

    if (eventId != null && editing == null) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(
                color = UPH_Navy,
                trackColor = UPH_Navy.copy(alpha = 0.15f),
                strokeWidth = 4.dp
            )
        }
        return
    }

    val eventKey = eventId ?: "ADD"

    val initialName     = editing?.name ?: ""
    val initialHeldBy   = editing?.heldBy ?: ""
    val initialStart    = editing?.startTimeMinutes
    val initialEnd      = editing?.endTimeMinutes
    val initialBuilding = editing?.building
    val initialFloor    = editing?.floor
    val initialRoom     = editing?.room?.takeIf { it.isNotBlank() }

    val initialDateMillis = remember(eventKey) {
        editing?.date?.toDate()?.let { d ->
            Calendar.getInstance().apply {
                time = d
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }.timeInMillis
        }
    }

    var name   by remember(eventKey) { mutableStateOf(TextFieldValue(initialName)) }
    var heldBy by remember(eventKey) { mutableStateOf(TextFieldValue(initialHeldBy)) }

    var date by remember(eventKey) {
        mutableStateOf<Calendar?>(
            editing?.date?.toDate()?.let { Calendar.getInstance().apply { time = it } }
        )
    }
    var startMin by remember(eventKey) { mutableStateOf<Int?>(initialStart) }
    var endMin   by remember(eventKey) { mutableStateOf<Int?>(initialEnd) }

    var building by remember(eventKey) { mutableStateOf<String?>(initialBuilding) }
    var floor    by remember(eventKey) { mutableStateOf<Int?>(initialFloor) }
    var room     by remember(eventKey) { mutableStateOf<String?>(initialRoom) }

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
            campusRepo.rooms.filter { it.buildingId == building && it.floor == floor }.map { it.code }
        } else emptyList()
    }

    LaunchedEffect(building) {
        if (building == null) { floor = null; room = null }
        else if (floor !in floors) { floor = null; room = null }
    }
    LaunchedEffect(building, floor) {
        if (building == null || floor == null || room !in rooms) room = null
    }

    val rawPosterUrl = editing?.posterUrl?.takeIf { isValidPosterUrl(it) }
    val initialPosterName = remember(eventKey) { rawPosterUrl?.let { filenameFromUrl(it) } }
    var posterName by remember(eventKey) { mutableStateOf<String?>(initialPosterName) }
    var poster: Uri? by remember { mutableStateOf(null) }

    var askConfirm by remember { mutableStateOf(false) }
    var success    by remember { mutableStateOf(false) }
    var error      by remember { mutableStateOf<String?>(null) }
    var isWorking  by remember { mutableStateOf(false) }
    var workingMsg by remember { mutableStateOf("") }
    var askExit    by remember { mutableStateOf(false) }

    val pickImage = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        if (uri != null) {
            val displayName = resolveDisplayName(ctx, uri)
            if (!isAllowedImage(ctx, uri, displayName)) {
                error = "Only .jpg, .jpeg or .png images are allowed as posters."
                poster = null
                posterName = null
            } else {
                error = null
                poster = uri
                posterName = displayName
            }
        }
    }

    val currentDateMillis = date?.timeInMillis

    val isDirty by remember(
        name.text, heldBy.text, currentDateMillis, startMin, endMin,
        building, floor, room, posterName, poster
    ) {
        mutableStateOf(
            name.text.trim()           != initialName ||
                    heldBy.text.trim()         != initialHeldBy ||
                    currentDateMillis          != initialDateMillis ||
                    startMin                   != initialStart ||
                    endMin                     != initialEnd ||
                    building                   != initialBuilding ||
                    floor                      != initialFloor ||
                    room                       != initialRoom ||
                    poster != null || (posterName ?: "") != (initialPosterName ?: "")
        )
    }

    val eventInPast by remember(date, startMin) {
        mutableStateOf(isEventInPast(date, startMin))
    }

    BackHandler {
        if (isDirty) askExit = true else onCancel()
    }

    val isValid = name.text.isNotBlank() &&
            heldBy.text.isNotBlank() &&
            date != null &&
            startMin != null &&
            endMin != null &&
            endMin!! > startMin!! &&
            building != null &&
            floor != null &&
            room != null &&
            !eventInPast

    Scaffold(
        bottomBar = {
            BottomAppBar {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    UPHPrimaryButton(
                        onClick = { askConfirm = true },
                        enabled = isValid
                    ) { Text("Confirm") }
                    Spacer(Modifier.width(12.dp))
                    UPHSecondaryButton(
                        onClick = { if (isDirty) askExit = true else onCancel() }
                    ) { Text("Cancel") }
                }
            }
        }
    ) { pad ->
        val scroll = rememberScrollState()
        Box(
            Modifier
                .padding(pad)
                .consumeWindowInsets(pad)
                .imePadding()
                .navigationBarsPadding()
                .fillMaxSize()
        ) {
            Column(
                Modifier
                    .fillMaxSize()
                    .verticalScroll(scroll)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(if (eventId == null) "Add Event" else "Edit Event", style = MaterialTheme.typography.titleLarge)

                OutlinedTextField(
                    value = name, onValueChange = { name = it },
                    label = { Text("Event Name") }, placeholder = { Text("Enter event name") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = uphTextFieldColors()
                )
                OutlinedTextField(
                    value = heldBy, onValueChange = { heldBy = it },
                    label = { Text("Held By") }, placeholder = { Text("Organizer / Faculty") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = uphTextFieldColors()
                )

                DateTimePickers(
                    date = date, onDateChange = { date = it },
                    startMinutes = startMin, onStartChange = { startMin = it },
                    endMinutes = endMin, onEndChange = { endMin = it }
                )

                if (eventInPast) {
                    Text(
                        "Event time cannot be in the past!",
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall
                    )
                }

                LocationPickers(
                    building = building, onBuilding = { building = it },
                    floor = floor, onFloor = { floor = it },
                    room = room, onRoom = { room = it },
                    buildings = buildingIds,
                    floors = floors,
                    rooms = rooms
                )

                Row(verticalAlignment = Alignment.CenterVertically) {
                    val hasAnyPoster = (poster != null) || (rawPosterUrl != null)
                    OutlinedButton(
                        onClick = { pickImage.launch("image/*") },
                        border = BorderStroke(1.dp, UPH_Navy),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = UPH_Navy)
                    ) {
                        Text(if (hasAnyPoster) "Change Poster" else "Choose Poster")
                    }
                    Spacer(Modifier.width(12.dp))
                    val rightText = posterName ?: "No poster yet!"
                    Text(text = rightText, maxLines = 1, overflow = TextOverflow.Ellipsis)
                }

                error?.let { Text(it, color = MaterialTheme.colorScheme.error) }
                Spacer(Modifier.height(80.dp))
            }

            VerticalScrollbar(
                scroll = scroll,
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .padding(end = 6.dp)
            )
        }
    }

    if (isWorking) {
        WorkingDialog(workingMsg)
    }

    // Confirm save
    if (askConfirm) {
        AlertDialog(
            onDismissRequest = { askConfirm = false },
            title = { Text("Are you sure?") },
            confirmButton = {
                UPHPrimaryButton(onClick = {
                    if (isEventInPast(date, startMin)) {
                        error = "Event time cannot be in the past!"
                        askConfirm = false
                        return@UPHPrimaryButton
                    }

                    val conflict = findRoomConflict(
                        all        = fullEvents,
                        currentId  = eventId,
                        date       = date,
                        startMinutes = startMin,
                        endMinutes   = endMin,
                        building   = building,
                        floor      = floor,
                        room       = room
                    )

                    if (conflict != null) {
                        val roomLabel = formatRoomLabel(building!!, conflict.room)
                        val msg = "Room $roomLabel is being used for \"${conflict.name}\" " +
                                "event from ${fmtMinutes(conflict.startTimeMinutes)} until ${fmtMinutes(conflict.endTimeMinutes)}."
                        error = msg
                        askConfirm = false
                        return@UPHPrimaryButton
                    }

                    askConfirm = false

                    val normalized = (date!!.clone() as Calendar).apply {
                        set(Calendar.HOUR_OF_DAY, 0)
                        set(Calendar.MINUTE, 0)
                        set(Calendar.SECOND, 0)
                        set(Calendar.MILLISECOND, 0)
                    }
                    val ts = Timestamp(normalized.time)

                    scope.launch {
                        workingMsg = if (eventId == null) "Adding..." else "Editing..."
                        isWorking = true

                        val compressedPoster: Uri? = if (poster != null) {
                            compressImageToTempFile(ctx, poster!!) ?: run {
                                isWorking = false
                                error = "Failed to compress poster image. Please try another file."
                                return@launch
                            }
                        } else {
                            null
                        }

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
                                posterUrl = null
                            )
                            vm.create(
                                toCreate,
                                compressedPoster,
                                onDone  = { isWorking = false; success = true },
                                onError = { e -> isWorking = false; error = e }
                            )
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
                            vm.update(
                                updated,
                                compressedPoster,
                                onDone  = { isWorking = false; success = true },
                                onError = { e -> isWorking = false; error = e }
                            )
                        }
                    }
                }) { Text("Yes") }
            },
            dismissButton = {
                UPHSecondaryButton(onClick = { askConfirm = false }) { Text("No") }
            }
        )
    }

    if (askExit) {
        AlertDialog(
            onDismissRequest = { askExit = false },
            title = { Text("Are you sure want to exit?") },
            text  = { Text("You have unsaved changes. They will be lost if you leave this page.") },
            confirmButton = {
                TextButton(
                    onClick = { askExit = false; onCancel() },
                    colors = ButtonDefaults.textButtonColors(contentColor = UPH_Navy)
                ) { Text("Yes") }
            },
            dismissButton = {
                TextButton(
                    onClick = { askExit = false },
                    colors = ButtonDefaults.textButtonColors(contentColor = UPH_Navy)
                ) { Text("No") }
            }
        )
    }

    if (success) {
        AlertDialog(
            onDismissRequest = { success = false; onDone() },
            title = { Text(if (eventId == null) "Event has been added" else "Event has been edited") },
            confirmButton = {
                TextButton(
                    onClick = { success = false; onDone() },
                    colors = ButtonDefaults.textButtonColors(contentColor = UPH_Navy)
                ) { Text("OK") }
            }
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
            modifier = Modifier.fillMaxWidth(),
            colors = uphTextFieldColors()
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
                TextButton(
                    onClick = {
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
                    },
                    colors = uphTextBtnColors()
                ) { Text("OK") }
            },
            dismissButton = {
                TextButton(
                    onClick = { showDateDialog = false },
                    colors = uphTextBtnColors()
                ) { Text("Cancel") }
            }
        ) {
            DatePicker(
                state = dpState,
                colors = uphDatePickerColors()
            )
        }
    }

    Spacer(Modifier.height(8.dp))

    val START_MIN = 6 * 60
    val END_MIN = 21 * 60
    val startOptions = remember { timeOptionsBetween(START_MIN, END_MIN - 30, 30) }
    val baseEndOptions = remember { timeOptionsBetween(START_MIN + 30, END_MIN, 30) }
    val endOptions = remember(startMinutes) {
        if (startMinutes == null) baseEndOptions else baseEndOptions.filter { it.second > startMinutes }
    }
    val timeInvalid = startMinutes != null && endMinutes != null && endMinutes <= startMinutes

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
                modifier = Modifier.menuAnchor().fillMaxWidth(),
                colors = uphTextFieldColors()
            )
            ExposedDropdownMenu(expanded = fromExpanded, onDismissRequest = { fromExpanded = false }) {
                startOptions.forEach { (label, minutes) ->
                    DropdownMenuItem(
                        text = { Text(label) },
                        onClick = {
                            onStartChange(minutes)
                            fromExpanded = false
                        }
                    )
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
                isError = timeInvalid,
                supportingText = { if (timeInvalid) Text("The end time must be later than the start time.") },
                modifier = Modifier.menuAnchor().fillMaxWidth(),
                colors = uphTextFieldColors()
            )
            ExposedDropdownMenu(expanded = toExpanded, onDismissRequest = { toExpanded = false }) {
                if (startMinutes == null || endOptions.isEmpty()) {
                    DropdownMenuItem(text = { Text("Select the start time first.") }, onClick = {}, enabled = false)
                } else {
                    endOptions.forEach { (label, minutes) ->
                        DropdownMenuItem(
                            text = { Text(label) },
                            onClick = { onEndChange(minutes); toExpanded = false }
                        )
                    }
                }
            }
        }
    }
}

// Utility function
private fun timeOptionsBetween(
    startMin: Int,
    endMin: Int,
    stepMinutes: Int = 30
): List<Pair<String, Int>> {
    val out = mutableListOf<Pair<String, Int>>()
    var m = startMin
    while (m <= endMin) {
        out += "%02d:%02d".format(m / 60, m % 60) to m
        m += stepMinutes
    }
    return out
}

private fun fmtMinutes(total: Int): String = "%02d:%02d".format(total / 60, total % 60)

private fun formatBuildingLabel(id: String): String = when (id) {
    "H" -> "Building HOPE"
    else -> "Building $id"
}

private fun formatRoomLabel(buildingId: String?, roomCode: String): String {
    return if (buildingId == "H" && roomCode.startsWith("H")) {
        "HP" + roomCode.removePrefix("H")
    } else {
        roomCode
    }
}

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

    ExposedDropdownMenuBox(expanded = bExpanded, onExpandedChange = { bExpanded = it }) {
        OutlinedTextField(
            value = building?.let { formatBuildingLabel(it) } ?: "",
            onValueChange = {},
            readOnly = true,
            label = { Text("Event Location: Building") },
            placeholder = { Text("Select building") },
            modifier = Modifier
                .menuAnchor()
                .fillMaxWidth(),
            colors = uphTextFieldColors()
        )
        ExposedDropdownMenu(expanded = bExpanded, onDismissRequest = { bExpanded = false }) {
            buildings.forEach { b ->
                DropdownMenuItem(
                    text = { Text(formatBuildingLabel(b)) },
                    onClick = { onBuilding(b); bExpanded = false }
                )
            }
        }
    }
    Spacer(Modifier.height(8.dp))

    val floorEnabled = building != null
    ExposedDropdownMenuBox(expanded = fExpanded && floorEnabled, onExpandedChange = { if (floorEnabled) fExpanded = it }) {
        OutlinedTextField(
            value = floor?.let { "Floor $it" } ?: "",
            onValueChange = {},
            readOnly = true,
            enabled = floorEnabled,
            label = { Text("Floor") },
            placeholder = { Text("Select floor") },
            modifier = Modifier
                .menuAnchor()
                .fillMaxWidth(),
            colors = uphTextFieldColors()
        )
        if (floorEnabled) {
            ExposedDropdownMenu(expanded = fExpanded, onDismissRequest = { fExpanded = false }) {
                floors.forEach { fl ->
                    DropdownMenuItem(
                        text = { Text("Floor $fl") },
                        onClick = { onFloor(fl); fExpanded = false }
                    )
                }
            }
        }
    }
    Spacer(Modifier.height(8.dp))

    val roomEnabled = building != null && floor != null
    ExposedDropdownMenuBox(expanded = rExpanded && roomEnabled, onExpandedChange = { if (roomEnabled) rExpanded = it }) {
        OutlinedTextField(
            value = room?.let { formatRoomLabel(building, it) } ?: "",
            onValueChange = {},
            readOnly = true,
            enabled = roomEnabled,
            label = { Text("Room") },
            placeholder = { Text("Select room") },
            modifier = Modifier
                .menuAnchor()
                .fillMaxWidth(),
            colors = uphTextFieldColors()
        )
        if (roomEnabled) {
            ExposedDropdownMenu(expanded = rExpanded, onDismissRequest = { rExpanded = false }) {
                rooms.forEach { r ->
                    DropdownMenuItem(
                        text = { Text(formatRoomLabel(building, r)) },
                        onClick = { onRoom(r); rExpanded = false }
                    )
                }
            }
        }
    }
}

private fun isEventInPast(
    date: Calendar?,
    startMinutes: Int?
): Boolean {
    if (date == null || startMinutes == null) return false

    val eventCal = (date.clone() as Calendar).apply {
        val h = startMinutes / 60
        val m = startMinutes % 60
        set(Calendar.HOUR_OF_DAY, h)
        set(Calendar.MINUTE, m)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }

    return eventCal.timeInMillis < System.currentTimeMillis()
}

private fun dayKeyFromCalendar(c: Calendar): Int {
    return (c.get(Calendar.YEAR) * 10_000) +
            ((c.get(Calendar.MONTH) + 1) * 100) +
            c.get(Calendar.DAY_OF_MONTH)
}

private fun dayKeyFromTimestamp(ts: com.google.firebase.Timestamp): Int {
    val cal = Calendar.getInstance().apply { time = ts.toDate() }
    return dayKeyFromCalendar(cal)
}

private fun findRoomConflict(
    all: List<Event>,
    currentId: String?,
    date: Calendar?,
    startMinutes: Int?,
    endMinutes: Int?,
    building: String?,
    floor: Int?,
    room: String?
): Event? {
    if (
        date == null || startMinutes == null || endMinutes == null ||
        building == null || floor == null || room == null
    ) return null

    val keyNew = dayKeyFromCalendar(date)

    return all.firstOrNull { e ->
        if (currentId != null && e.id == currentId) return@firstOrNull false

        if (e.building != building || e.floor != floor || e.room != room) return@firstOrNull false

        val keyExist = dayKeyFromTimestamp(e.date)
        if (keyExist != keyNew) return@firstOrNull false

        val s2 = e.startTimeMinutes
        val e2 = e.endTimeMinutes

        startMinutes < e2 && s2 < endMinutes
    }
}


