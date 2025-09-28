package com.example.campusguide.ui

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.CalendarContract
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.outlined.Map
import androidx.compose.material.icons.outlined.KeyboardArrowRight
import androidx.compose.material.icons.outlined.Language
import androidx.compose.material.icons.outlined.CameraAlt
import androidx.compose.material.icons.outlined.Phone
import androidx.compose.material.icons.outlined.AdminPanelSettings
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import coil.compose.AsyncImage
import com.example.campusguide.R
import com.example.campusguide.data.*
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId

private val UPH_Navy = Color(0xFF16224C)
private val UPH_Red  = Color(0xFFE31E2E)
private val UPH_White = Color(0xFFFFFFFF)
private val UPH_Orange = Color(0xFFF58A0A)

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
    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            AppDrawer(onAdminLogin = {
                scope.launch { drawerState.close() }
                navController.navigate("admin_login")
            })
        }
    ) {
        Scaffold(
            topBar = {
                AppTopBar(
                    onSearch = { navController.navigate("search") },
                    onMenu = { scope.launch { drawerState.open() } }
                )
            },
            bottomBar = { BottomBar(navController) }
        ) { padding ->
            NavHost(navController, startDestination = "home", Modifier.padding(padding)) {
                composable("home")   { HomeScreen(navController) }
                composable("events") { EventsScreen(navController) }
                composable("search") { SearchScreen(navController) }

                composable("admin_login") {
                    AdminLoginScreen(onSuccess = {
                        navController.navigate("admin_dashboard") { launchSingleTop = true }
                    })
                }
                composable("admin_dashboard") { Text("Admin Dashboard — coming soon") }

                composable("building/{id}") { backStackEntry ->
                    val id = backStackEntry.arguments?.getString("id") ?: ""
                    BuildingDetailScreen(id, navController)
                }

                composable("floor/{b}/{f}") { backStackEntry ->
                    val b = backStackEntry.arguments?.getString("b") ?: ""
                    val f = backStackEntry.arguments?.getString("f")?.toIntOrNull() ?: 1
                    FloorPlanScreen(navController, b, f)
                }

                composable("event/{eventId}") { backStackEntry ->
                    val eventId = backStackEntry.arguments?.getString("eventId") ?: ""
                    EventDetailScreen(navController, eventId)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AppTopBar(
    onSearch: () -> Unit,
    onMenu: () -> Unit
) {
    TopAppBar(
        navigationIcon = {
            IconButton(onClick = onMenu) {
                Icon(Icons.Default.Menu, contentDescription = "Menu", tint = UPH_White)
            }
        },
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
                    modifier = Modifier.height(28.dp).padding(end = 8.dp)
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

@Composable
private fun BottomBar(navController: NavHostController) {
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
            icon = { Icon(Icons.Default.Map, contentDescription = "Map", tint = UPH_White) },
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


data class Spot(
    val id: String,
    val x: Float,
    val y: Float,
    val color: Color
)

@Composable
fun HomeScreen(
    navController: NavHostController,
    debugBorders: Boolean = false
) {

    val hotspots = listOf(
        Spot("F", 0.23f,  0.36f, Color(0xFF2ECC71)),
        Spot("B", 0.665f, 0.73f, UPH_Orange),
        Spot("H", 0.55f,  0.77f, Color(0xFFFFC27A)),
        Spot("C", 0.35f,  0.78f, Color(0xFFFFD54F)),
        Spot("D", 0.46f,  0.46f, Color(0xFF1E88E5)),
        Spot("G", 0.35f,  0.06f, Color(0xFFEF5350))
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
        ) {
            Image(
                painter = painterResource(R.drawable.uph_map),
                contentDescription = "UPH Map",
                contentScale = ContentScale.FillWidth,
                modifier = Modifier.fillMaxWidth()
            )

            BoxWithConstraints(
                modifier = Modifier
                    .matchParentSize()
                    .zIndex(1f)
            ) {
                if (debugBorders) {
                    Box(Modifier.matchParentSize().border(2.dp, Color.Red))
                }
                val badge = 34.dp
                hotspots.forEach { s ->
                    val xOff = (maxWidth * s.x) - (badge / 2)
                    val yOff = (maxHeight * s.y) - (badge / 2)
                    BuildingDot(
                        id = s.id,
                        color = s.color,
                        size = badge,
                        modifier = Modifier
                            .offset(x = xOff, y = yOff)
                            .clickable { navController.navigate("building/${s.id}") }
                    )
                }
            }
        }

        Spacer(Modifier.height(16.dp))
        Text("Tap bulatan pada peta untuk membuka detail.", color = Color.Gray)
        Spacer(Modifier.height(24.dp))
    }
}

@Composable
fun BuildingDot(
    id: String,
    color: Color,
    size: Dp,
    modifier: Modifier = Modifier
) {
    Surface(
        color = color,
        shape = CircleShape,
        shadowElevation = 4.dp,
        modifier = modifier.size(size)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .border(2.dp, UPH_White, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text(id, color = UPH_White, style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
fun AppDrawer(onAdminLogin: () -> Unit) {
    val ctx = LocalContext.current
    fun open(url: String) = ctx.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))

    val bgDeep     = UPH_Navy
    val cardDark   = Color(0xFF0F1A3A)
    val textPrimary= Color(0xFFE8ECF7)
    val chevron    = textPrimary.copy(alpha = 0.7f)
    val warning    = UPH_Orange

    ModalDrawerSheet(
        modifier = Modifier.fillMaxHeight(),
        drawerContainerColor = bgDeep,
        drawerContentColor = textPrimary
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 28.dp, start = 20.dp, end = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Image(
                painter = painterResource(id = R.drawable.uph_logo),
                contentDescription = "UPH",
                modifier = Modifier
                    .fillMaxWidth()
                    .height(72.dp)
            )
            Spacer(Modifier.height(10.dp))
            Divider(color = Color.White.copy(alpha = 0.15f))
            Spacer(Modifier.height(10.dp))
            Text("Campus Guide UPH Lippo Village", style = MaterialTheme.typography.titleMedium, color = textPrimary)
        }

        Spacer(Modifier.weight(1f))

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            DrawerItem(
                label = "UPH Lippo Village Map",
                leading = { Icon(Icons.Outlined.Map, null, tint = textPrimary) },
                trailing = { Icon(Icons.Outlined.KeyboardArrowRight, null, tint = chevron) },
                container = cardDark
            ) { open("https://maps.app.goo.gl/rJouzJ585cAuywr96") }

            DrawerItem(
                label = "UPH website",
                leading = { Icon(Icons.Outlined.Language, null, tint = textPrimary) },
                trailing = { Icon(Icons.Outlined.KeyboardArrowRight, null, tint = chevron) },
                container = cardDark
            ) { open("https://www.uph.edu/id") }

            DrawerItem(
                label = "UPH Instagram",
                leading = { Icon(Icons.Outlined.CameraAlt, null, tint = textPrimary) },
                trailing = { Icon(Icons.Outlined.KeyboardArrowRight, null, tint = chevron) },
                container = cardDark
            ) { open("https://www.instagram.com/uphimpactslives/") }

            DrawerItem(
                label = "UPH phone",
                leading = { Icon(Icons.Outlined.Phone, null, tint = textPrimary) },
                trailing = { Icon(Icons.Outlined.KeyboardArrowRight, null, tint = chevron) },
                container = cardDark
            ) { open("tel:0215460901") }

            DrawerItem(
                label = "Admin Login",
                leading = { Icon(Icons.Outlined.AdminPanelSettings, null, tint = warning) },
                trailing = { Icon(Icons.Outlined.KeyboardArrowRight, null, tint = chevron) },
                container = cardDark
            ) { onAdminLogin() }
        }
        Spacer(Modifier.height(12.dp))
    }
}

@Composable
private fun DrawerItem(
    label: String,
    leading: @Composable () -> Unit,
    trailing: @Composable () -> Unit,
    container: Color,
    onClick: () -> Unit
) {
    Surface(
        tonalElevation = 0.dp,
        color = container,
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 60.dp)
            .clickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 18.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                leading()
                Spacer(Modifier.width(14.dp))
                Text(label, style = MaterialTheme.typography.titleMedium)
            }
            trailing()
        }
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
fun EventDetailScreen(navController: NavHostController, eventId: String) {
    val ctx = LocalContext.current
    val repo = remember { CampusRepoProvider.provide(ctx) }
    val events by remember { repo.streamAllEvents() }.collectAsState(initial = emptyList())
    val e = events.firstOrNull { it.id == eventId }

    if (e == null) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { Text("Event not found") }
        return
    }

    Column(Modifier.fillMaxSize().padding(16.dp)) {
        BackToMapButton(navController, modifier = Modifier.padding(bottom = 8.dp))

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
        BackToMapButton(navController, Modifier.padding(bottom = 8.dp))

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
fun FloorPlanScreen(navController: NavHostController, buildingId: String, floor: Int) {
    val resName = "b" + buildingId.lowercase() + "_f" + floor
    val storagePath = "maps/$buildingId/$resName.png"
    val ctx = LocalContext.current

    var url by remember { mutableStateOf<String?>(null) }
    var tried by remember { mutableStateOf(false) }

    LaunchedEffect(storagePath) {
        tried = false
        try {
            val ref = FirebaseStorage.getInstance().reference.child(storagePath)
            url = ref.downloadUrl.await().toString()
        } catch (_: Exception) {
            url = null
        } finally {
            tried = true
        }
    }
    val drawableId = remember(resName) {
        ctx.resources.getIdentifier(resName, "drawable", ctx.packageName)
    }

    Box(Modifier.fillMaxSize()) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            when {
                url != null -> {
                    AsyncImage(
                        model = url,
                        contentDescription = "Floor plan",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Fit
                    )
                }
                tried && drawableId != 0 -> {
                    Image(
                        painter = painterResource(drawableId),
                        contentDescription = "Floor plan",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Fit
                    )
                }
                tried -> Text("No floor plan image for Building $buildingId Floor $floor")
                else -> CircularProgressIndicator()
            }
        }
        BackToMapButton(navController, Modifier.align(Alignment.TopStart).padding(8.dp))
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
    var startDate by remember { mutableStateOf(LocalDate.now()) }
    var endDate by remember { mutableStateOf(LocalDate.now().plusDays(7)) }

    val today = LocalDate.now()

    val filtered = allEvents.filter { e ->
        val inBuilding = building == null || building == "All" || e.buildingId == building
        val inRange = !e.start.toLocalDate().isBefore(startDate) && !e.start.toLocalDate().isAfter(endDate)
        val now = LocalDateTime.now()
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
                initialSelectedDateMillis = startDate.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
            )
            DatePickerDialog(
                onDismissRequest = { showDatePickerStart = false },
                confirmButton = {
                    TextButton(onClick = {
                        dpState.selectedDateMillis?.let { millis ->
                            startDate = Instant.ofEpochMilli(millis).atZone(ZoneId.systemDefault()).toLocalDate()
                        }
                        showDatePickerStart = false
                    }) { Text("OK") }
                }
            ) { DatePicker(state = dpState) }
        }

        if (showDatePickerEnd) {
            val dpState2 = rememberDatePickerState(
                initialSelectedDateMillis = endDate.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
            )
            DatePickerDialog(
                onDismissRequest = { showDatePickerEnd = false },
                confirmButton = {
                    TextButton(onClick = {
                        dpState2.selectedDateMillis?.let { millis ->
                            endDate = Instant.ofEpochMilli(millis).atZone(ZoneId.systemDefault()).toLocalDate()
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
    val now = LocalDateTime.now()
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
fun BackToMapButton(navController: NavHostController, modifier: Modifier = Modifier) {
    Surface(
        shape = CircleShape,
        color = Color(0x2216224C),
        shadowElevation = 2.dp,
        modifier = modifier
            .size(40.dp)
            .clip(CircleShape)
            .clickable {
                val popped = navController.popBackStack(route = "home", inclusive = false)
                if (!popped) {
                    navController.navigate("home") {
                        popUpTo(navController.graph.startDestinationId) { saveState = true }
                        launchSingleTop = true
                        restoreState = true
                    }
                }
            }
    ) {
        Box(contentAlignment = Alignment.Center) {
            Icon(
                imageVector = Icons.Filled.ArrowBack,
                contentDescription = "Back to Map",
                tint = UPH_White
            )
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
