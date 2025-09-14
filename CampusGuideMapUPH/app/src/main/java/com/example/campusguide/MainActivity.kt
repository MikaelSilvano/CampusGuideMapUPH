package com.example.campusguide

import android.content.Intent
import android.os.Bundle
import android.provider.CalendarContract
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.campusguide.data.*
import kotlinx.coroutines.launch
import java.time.ZoneId
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.ui.zIndex

private val UPH_Navy = Color(0xFF16224C)
private val UPH_Red  = Color(0xFFE31E2E)
private val UPH_White = Color(0xFFFFFFFF)
private val UPH_Orange = Color(0xFFF58A0A) // selected pill color

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { CampusGuideApp() }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CampusGuideApp() {
    val navController = rememberNavController()
    Scaffold(
        topBar = {
            AppTopBar(
                onSearch = { navController.navigate("search") }
            )
        },
        bottomBar = {
            val navItemColors = NavigationBarItemDefaults.colors(
                selectedIconColor   = UPH_White,
                selectedTextColor   = UPH_White,
                unselectedIconColor = UPH_White.copy(alpha = 0.75f),
                unselectedTextColor = UPH_White.copy(alpha = 0.75f),
                indicatorColor      = UPH_Orange
            )

            NavigationBar(containerColor = UPH_Navy) {
                NavigationBarItem(
                    selected = currentRoute(navController) == "home",
                    onClick = {
                        navController.navigate("home") {
                            popUpTo(navController.graph.startDestinationId) { saveState = true }
                            launchSingleTop = true; restoreState = true
                        }
                    },
                    icon = { Icon(Icons.Default.Home, contentDescription = "Home", tint = UPH_White) },
                    label = { Text("Map", color = UPH_White) },
                    colors = navItemColors,
                    alwaysShowLabel = true
                )
                NavigationBarItem(
                    selected = currentRoute(navController) == "events",
                    onClick = {
                        navController.navigate("events") {
                            popUpTo(navController.graph.startDestinationId) { saveState = true }
                            launchSingleTop = true; restoreState = true
                        }
                    },
                    icon = { Icon(Icons.Default.Event, contentDescription = "Events", tint = UPH_White) },
                    label = { Text("Events", color = UPH_White) },
                    colors = navItemColors,
                    alwaysShowLabel = true
                )
                NavigationBarItem(
                    selected = currentRoute(navController) == "search",
                    onClick = {
                        navController.navigate("search") {
                            popUpTo(navController.graph.startDestinationId) { saveState = true }
                            launchSingleTop = true; restoreState = true
                        }
                    },
                    icon = { Icon(Icons.Default.Search, contentDescription = "Search", tint = UPH_White) },
                    label = { Text("Search", color = UPH_White) },
                    colors = navItemColors,
                    alwaysShowLabel = true
                )
            }
        }
    ) { padding ->
        NavHost(navController, startDestination = "home", Modifier.padding(padding)) {
            composable("home") { HomeScreen(navController) }
            composable("events") { EventsScreen(navController) }
            composable("search") { SearchScreen(navController) }
            composable(
                "building/{id}",
                arguments = listOf(navArgument("id") { type = NavType.StringType })
            ) { backStackEntry ->
                val id = backStackEntry.arguments?.getString("id") ?: ""
                BuildingDetailScreen(id, navController)
            }
            composable(
                "floor/{b}/{f}",
                arguments = listOf(
                    navArgument("b") { type = NavType.StringType },
                    navArgument("f") { type = NavType.IntType }
                )
            ) { backStackEntry ->
                val b = backStackEntry.arguments?.getString("b") ?: ""
                val f = backStackEntry.arguments?.getInt("f") ?: 1
                FloorPlanScreen(b, f)
            }
            composable(
                "event/{eventId}",
                arguments = listOf(navArgument("eventId") { type = NavType.StringType })
            ) { backStackEntry ->
                val eventId = backStackEntry.arguments?.getString("eventId") ?: ""
                EventDetailScreen(eventId)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AppTopBar(onSearch: () -> Unit) {
    TopAppBar(
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = UPH_Navy,
            titleContentColor = UPH_White,
            actionIconContentColor = UPH_White,
            navigationIconContentColor = UPH_White
        ),
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Image(
                    painter = painterResource(id = R.drawable.uph_logo),
                    contentDescription = "UPH",
                    modifier = Modifier
                        .height(28.dp)
                        .padding(end = 8.dp)
                )
                Text("Campus Map")
            }
        },
        actions = {
            IconButton(onClick = onSearch) {
                Icon(Icons.Default.Search, contentDescription = "Search")
            }
        }
    )
}

data class Hotspot(val id: String, val x: Float, val y: Float)

@Composable
fun HomeScreen(
    navController: NavHostController,
    debugBorders: Boolean = false
) {
    Box(
        modifier = Modifier.fillMaxSize()
    ) {

        Image(
            painter = painterResource(R.drawable.uph_map),
            contentDescription = "UPH Map",
            contentScale = ContentScale.Fit,
            modifier = Modifier
                .fillMaxHeight()
                .align(Alignment.Center)
                .graphicsLayer { rotationZ = -90f }
        )

        Box(
            modifier = Modifier
                .matchParentSize()
                .zIndex(1f)
        ) {

            if (debugBorders) {
                Box(
                    Modifier
                        .matchParentSize()
                        .border(2.dp, Color.Red)
                )
            }

            BoxWithConstraints(Modifier.matchParentSize()) {
                val badge = 32.dp
                val hs = listOf(
                    Hotspot("B", 0.20f, 0.75f),
                    Hotspot("C", 0.18f, 0.85f),
                    Hotspot("D", 0.55f, 0.62f),
                    Hotspot("F", 0.35f, 0.55f),
                    Hotspot("G", 0.12f, 0.15f),
                )
                hs.forEach { h ->
                    Box(
                        Modifier
                            .size(badge)
                            .offset(
                                x = (maxWidth * h.x) - (badge / 2),
                                y = (maxHeight * h.y) - (badge / 2)
                            )
                            .background(UPH_Navy, RoundedCornerShape(8.dp))
                            .clickable { navController.navigate("building/${h.id}") },
                        contentAlignment = Alignment.Center
                    ) { Text(h.id, color = UPH_White, fontWeight = FontWeight.Bold) }
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun BuildingDetailScreen(buildingId: String, navController: NavHostController) {
    val ctx = LocalContext.current
    val repo = remember { CampusRepoProvider.provide(ctx) }
    var floors by remember { mutableStateOf(listOf<Int>()) }
    var building by remember { mutableStateOf<Building?>(null) }
    val scope = rememberCoroutineScope()

    LaunchedEffect(buildingId) {
        scope.launch {
            building = repo.getBuildings().firstOrNull { it.id == buildingId }
            floors = repo.getFloors(buildingId)
        }
    }

    val events by remember(buildingId) { repo.streamEventsForBuilding(buildingId) }
        .collectAsState(initial = emptyList())

    Column(Modifier.fillMaxSize().padding(12.dp)) {
        Text("Building ${building?.id ?: buildingId}", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(8.dp))
        Text("List of Faculties", style = MaterialTheme.typography.titleMedium)
        Spacer(Modifier.height(6.dp))
        Row(Modifier.horizontalScroll(rememberScrollState())) {
            val all = InMemoryCampusRepository.facultyInBuilding
            val faculties = all.filterValues { it == buildingId }.keys.ifEmpty { listOf("Faculty of ...") }
            faculties.forEach { f ->
                Surface(shape = RoundedCornerShape(12.dp), color = Color(0xFFF2F3F7), modifier = Modifier.padding(end = 8.dp)) {
                    Box(Modifier.size(width = 200.dp, height = 90.dp).padding(12.dp), contentAlignment = Alignment.Center) {
                        Text(f, fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        }
        Spacer(Modifier.height(8.dp))
        Divider()
        Spacer(Modifier.height(8.dp))
        Text("Floor Navigation", style = MaterialTheme.typography.titleMedium)
        Spacer(Modifier.height(6.dp))
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            floors.forEach { f ->
                OutlinedButton(onClick = { navController.navigate("floor/${buildingId}/$f") }) { Text("Floor $f") }
            }
        }
        Spacer(Modifier.height(12.dp))
        Divider()
        Spacer(Modifier.height(8.dp))
        Text("Events", style = MaterialTheme.typography.titleMedium)
        Spacer(Modifier.height(6.dp))
        LazyColumn(Modifier.fillMaxSize()) {
            items(events) { e ->
                EventCard(e)
                Spacer(Modifier.height(8.dp))
            }
        }
    }
}

@Composable
fun FloorPlanScreen(buildingId: String, floor: Int) {
    val resName = "b" + buildingId.lowercase() + "_f" + floor
    val ctx = LocalContext.current
    val resId = ctx.resources.getIdentifier(resName, "drawable", ctx.packageName)
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        if (resId != 0) {
            Image(
                painter = painterResource(resId),
                contentDescription = "Floor plan",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Fit
            )
        } else {
            Text("No floor plan image for Building $buildingId Floor $floor")
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EventsScreen(navController: NavHostController) {
    val ctx = LocalContext.current
    val repo = remember { CampusRepoProvider.provide(ctx) }
    val allEvents by remember { repo.streamAllEvents() }.collectAsState(initial = emptyList())

    var building by remember { mutableStateOf<String?>(null) }
    val buildings = listOf("All", "B", "C", "D", "F", "G")
    var buildingExpanded by remember { mutableStateOf(false) }

    var status by remember { mutableStateOf("All") }
    val statuses = listOf("All", "Ongoing", "Upcoming", "Soon")
    var statusExpanded by remember { mutableStateOf(false) }

    var showDatePickerStart by remember { mutableStateOf(false) }
    var showDatePickerEnd by remember { mutableStateOf(false) }
    var startDate by remember { mutableStateOf(java.time.LocalDate.now()) }
    var endDate by remember { mutableStateOf(java.time.LocalDate.now().plusDays(7)) }

    val today = java.time.LocalDate.now()

    val filtered = allEvents.filter { e ->
        val inBuilding = building == null || building == "All" || e.buildingId == building
        val inRange = !e.start.toLocalDate().isBefore(startDate) && !e.start.toLocalDate().isAfter(endDate)
        val now = java.time.LocalDateTime.now()
        val inStatus = when (status) {
            "Ongoing" -> now.isAfter(e.start) && now.isBefore(e.end)
            "Upcoming" -> now.isBefore(e.start) && e.start.isAfter(now.plusDays(3))
            "Soon" -> e.start.isAfter(now) && e.start.isBefore(now.plusDays(3))
            else -> true
        }
        inBuilding && inRange && inStatus
    }.sortedBy { it.start }

    Column(Modifier.fillMaxSize().padding(12.dp)) {
        Text("Events — $today", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(8.dp))

        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.weight(1f)) {
                OutlinedTextField(
                    value = building ?: "All",
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Building") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { buildingExpanded = true }
                )
                DropdownMenu(expanded = buildingExpanded, onDismissRequest = { buildingExpanded = false }) {
                    buildings.forEach { b ->
                        DropdownMenuItem(
                            text = { Text(b) },
                            onClick = { building = if (b == "All") null else b; buildingExpanded = false }
                        )
                    }
                }
            }
            Spacer(Modifier.width(8.dp))
            Box(Modifier.weight(1f)) {
                OutlinedTextField(
                    value = status,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Status") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { statusExpanded = true }
                )
                DropdownMenu(expanded = statusExpanded, onDismissRequest = { statusExpanded = false }) {
                    statuses.forEach { s ->
                        DropdownMenuItem(text = { Text(s) }, onClick = { status = s; statusExpanded = false })
                    }
                }
            }
        }

        Spacer(Modifier.height(8.dp))
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            FilterPill("From: $startDate") { showDatePickerStart = true }
            Spacer(Modifier.width(8.dp))
            FilterPill("To: $endDate") { showDatePickerEnd = true }
        }

        if (showDatePickerStart) {
            val dpState = rememberDatePickerState(
                initialSelectedDateMillis = startDate.atStartOfDay(java.time.ZoneId.systemDefault()).toInstant().toEpochMilli()
            )
            DatePickerDialog(
                onDismissRequest = { showDatePickerStart = false },
                confirmButton = {
                    TextButton(onClick = {
                        dpState.selectedDateMillis?.let { millis ->
                            startDate = java.time.Instant.ofEpochMilli(millis).atZone(java.time.ZoneId.systemDefault()).toLocalDate()
                        }
                        showDatePickerStart = false
                    }) { Text("OK") }
                }
            ) { DatePicker(state = dpState) }
        }

        if (showDatePickerEnd) {
            val dpState2 = rememberDatePickerState(
                initialSelectedDateMillis = endDate.atStartOfDay(java.time.ZoneId.systemDefault()).toInstant().toEpochMilli()
            )
            DatePickerDialog(
                onDismissRequest = { showDatePickerEnd = false },
                confirmButton = {
                    TextButton(onClick = {
                        dpState2.selectedDateMillis?.let { millis ->
                            endDate = java.time.Instant.ofEpochMilli(millis).atZone(java.time.ZoneId.systemDefault()).toLocalDate()
                        }
                        showDatePickerEnd = false
                    }) { Text("OK") }
                }
            ) { DatePicker(state = dpState2) }
        }

        Spacer(Modifier.height(8.dp))
        LazyColumn(Modifier.fillMaxSize()) {
            items(filtered) { e ->
                EventCard(e)
                Spacer(Modifier.height(8.dp))
            }
        }
    }
}

@Composable
fun FilterPill(text: String, onClick: () -> Unit) {
    Surface(onClick = onClick, shape = RoundedCornerShape(20.dp), tonalElevation = 1.dp) {
        Row(Modifier.padding(horizontal = 12.dp, vertical = 6.dp), verticalAlignment = Alignment.CenterVertically) {
            Text(text)
        }
    }
}

@Composable
fun StatusBadge(e: CampusEvent): Pair<String, Color> {
    val now = java.time.LocalDateTime.now()
    return when {
        now.isAfter(e.start) && now.isBefore(e.end) -> "Ongoing" to Color(0xFFE0F2FF)
        e.start.isAfter(now) && e.start.isBefore(now.plusDays(3)) -> "Coming Soon" to Color(0xFFF7EDE3)
        e.start.isAfter(now) -> "Upcoming" to Color(0xFFEAF7E9)
        else -> "Past" to Color.LightGray
    }
}

@Composable
fun EventCard(e: CampusEvent) {
    val (label, bg) = StatusBadge(e)
    Surface(shape = RoundedCornerShape(12.dp), color = Color(0xFFF2F3F7), tonalElevation = 1.dp, modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text(e.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Box(Modifier.background(bg, shape = RoundedCornerShape(12.dp)).padding(horizontal = 10.dp, vertical = 4.dp)) {
                    Text(label)
                }
            }
            Spacer(Modifier.height(6.dp))
            Text("Held By: ${e.heldBy}")
            Text("Date: ${e.start.toLocalDate()}")
            Text("Time: ${e.start.toLocalTime()} – ${e.end.toLocalTime()}")
            Text("Room: ${e.room}")
        }
    }
}

@Composable
fun EventRow(e: CampusEvent, onClick: () -> Unit) {
    Column(
        Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(16.dp)
    ) {
        Text(e.name, style = MaterialTheme.typography.titleMedium)
        Text("${e.heldBy} • ${e.room} • ${e.start.toLocalDate()} ${e.start.toLocalTime()}–${e.end.toLocalTime()}")
    }
}

@Composable
fun EventDetailScreen(eventId: String) {
    val ctx = LocalContext.current
    val repo = remember { CampusRepoProvider.provide(ctx) }
    val events by remember { repo.streamAllEvents() }.collectAsState(initial = emptyList())
    val e = events.firstOrNull { it.id == eventId }

    if (e == null) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { Text("Event not found") }
        return
    }

    Column(Modifier.fillMaxSize().padding(16.dp)) {
        Text(e.name, style = MaterialTheme.typography.headlineSmall)
        Text("Held by: ${e.heldBy}")
        Text("Building ${e.buildingId} • Room ${e.room}")
        Text("Starts: ${e.start}")
        Text("Ends: ${e.end}")
        Spacer(Modifier.height(16.dp))
        Button(onClick = {
            val intent = Intent(Intent.ACTION_INSERT).apply {
                data = CalendarContract.Events.CONTENT_URI
                putExtra(CalendarContract.Events.TITLE, e.name)
                putExtra(CalendarContract.Events.EVENT_LOCATION, "Building ${e.buildingId} • ${e.room}")
                putExtra(CalendarContract.EXTRA_EVENT_BEGIN_TIME, e.start.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli())
                putExtra(CalendarContract.EXTRA_EVENT_END_TIME, e.end.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli())
            }
            ctx.startActivity(intent)
        }) { Text("Add to Calendar") }
    }
}

@Composable
fun SearchScreen(navController: NavHostController) {
    val ctx = LocalContext.current
    val repo = remember { CampusRepoProvider.provide(ctx) }
    var q by remember { mutableStateOf("") }
    var results by remember { mutableStateOf<List<SearchResult>>(emptyList()) }
    val scope = rememberCoroutineScope()

    Column(Modifier.fillMaxSize().padding(12.dp)) {
        OutlinedTextField(
            value = q,
            onValueChange = { newQ ->
                q = newQ
                scope.launch { results = repo.search(newQ) }
            },
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text("Search faculty, room, or event…") }
        )
        LazyColumn {
            items(results) { r ->
                Column(
                    Modifier
                        .fillMaxWidth()
                        .clickable {
                            when (r) {
                                is SearchResult.FacultyResult -> navController.navigate("building/${r.buildingId}")
                                is SearchResult.RoomResult -> navController.navigate("floor/${r.buildingId}/${r.floor}")
                                is SearchResult.EventResult -> navController.navigate("event/${r.eventId}")
                            }
                        }
                        .padding(12.dp)
                ) {
                    Text(r.title, fontWeight = FontWeight.Bold)
                    Text(r.subtitle)
                }
                Divider()
            }
        }
    }
}

@Composable
fun currentRoute(navController: NavHostController): String? {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    return navBackStackEntry?.destination?.route
}

@Preview(name = "Home Screen (Borders)", showSystemUi = true, showBackground = true)
@Composable
private fun PreviewHomeScreenBorders() {
    val nav = rememberNavController()
    HomeScreen(navController = nav, debugBorders = true)
}

@Preview(name = "Home Screen (Normal)", showSystemUi = true, showBackground = true)
@Composable
private fun PreviewHomeScreen() {
    val nav = rememberNavController()
    HomeScreen(navController = nav)
}

@Preview(name = "Events Screen", showSystemUi = true, showBackground = true)
@Composable
private fun PreviewEventsScreen() {
    val nav = rememberNavController()
    EventsScreen(nav)
}

@Preview(name = "Whole App Shell", showSystemUi = true, showBackground = true)
@Composable
private fun PreviewApp() {
    CampusGuideApp()
}
