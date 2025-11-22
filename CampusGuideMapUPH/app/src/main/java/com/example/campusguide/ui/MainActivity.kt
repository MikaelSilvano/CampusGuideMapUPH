package com.example.campusguide.ui

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.CalendarContract
import androidx.compose.foundation.Canvas
import androidx.compose.ui.res.colorResource
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.saveable.rememberSaveable
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
import com.example.campusguide.BuildConfig
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
import androidx.compose.ui.BiasAlignment
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
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.geometry.Offset
import kotlinx.coroutines.tasks.await
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId
import com.example.campusguide.ui.admin.*
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.OutlinedTextField
import coil.compose.AsyncImage
import kotlinx.coroutines.tasks.await
import coil.compose.SubcomposeAsyncImage
import coil.compose.SubcomposeAsyncImageContent
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.filled.Close
import com.example.campusguide.ui.common.VerticalScrollbar
import androidx.compose.runtime.Composable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.ScrollState
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.text.style.TextOverflow
import com.example.campusguide.ui.common.HorizontalScrollbar
import coil.compose.AsyncImagePainter
import android.util.Log
import androidx.compose.material.icons.outlined.VideoLibrary
import com.example.campusguide.data.FrequentlyVisitedPlace


private val UPH_Navy = Color(0xFF16224C)
private val UPH_Red  = Color(0xFFE31E2E)
private val UPH_White = Color(0xFFFFFFFF)
private val UPH_Orange = Color(0xFFF58A0A)
val routeColor = Color(0xFFA64AEF)

private val FACULTY_CARD_WIDTH  = 220.dp
private val FACULTY_TITLE_HEIGHT = 44.dp
private val FACULTY_IMAGE_HEIGHT = 110.dp

@Composable
private fun uphTextFieldColors() = TextFieldDefaults.colors(
    focusedIndicatorColor    = UPH_Navy,
    unfocusedIndicatorColor  = UPH_Navy,
    disabledIndicatorColor   = UPH_Navy.copy(alpha = 0.30f),
    focusedLabelColor        = UPH_Navy,
    cursorColor              = UPH_Navy,
    focusedTrailingIconColor = UPH_Navy,
    unfocusedTrailingIconColor = UPH_Navy,
    focusedContainerColor    = Color.Transparent,
    unfocusedContainerColor  = Color.Transparent,
    focusedPlaceholderColor  = UPH_Navy.copy(alpha = 0.55f),
    unfocusedPlaceholderColor= UPH_Navy.copy(alpha = 0.55f),
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun uphDatePickerColors() = DatePickerDefaults.colors(
    containerColor = Color(0xFFF8F9FD),

    titleContentColor = UPH_Navy,
    headlineContentColor = UPH_Navy,
    subheadContentColor = UPH_Navy,

    weekdayContentColor = UPH_Navy,
    dayContentColor = UPH_Navy,
    disabledDayContentColor = UPH_Navy.copy(alpha = 0.35f),
    yearContentColor = UPH_Navy,
    currentYearContentColor = UPH_Navy,

    todayDateBorderColor = UPH_Navy,
    todayContentColor = UPH_Navy,

    selectedDayContainerColor = UPH_Orange,
    selectedDayContentColor = UPH_White,

    dayInSelectionRangeContainerColor = UPH_Orange.copy(alpha = 0.18f),
    dayInSelectionRangeContentColor = UPH_Navy,

    selectedYearContainerColor = UPH_Orange,
    selectedYearContentColor = UPH_White,

    navigationContentColor = UPH_Navy
)

@Composable
private fun uphTextBtnColors() =
    ButtonDefaults.textButtonColors(contentColor = UPH_Navy)

class MainActivity : ComponentActivity() {
    // Dipanggil saat activity dibuat untuk set konten ke composable utama
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Initialize graph once
        com.example.campusguide.path.PathRepository.init(this, R.raw.campus_graph)

        setContent { CampusGuideApp() }
    }
}

data class EntranceUI(
    val id: String,
    val label: String,
    val buildingId: String
)

private val ADMIN_ROUTES = listOf(
    ROUTE_ADMIN_DASH, ROUTE_ADMIN_ADD, ROUTE_ADMIN_EDIT, ROUTE_ADMIN_REMOVE, ROUTE_ADMIN_HISTORY
)

fun NavHostController.goToHomeClearingAdmin() {
    ADMIN_ROUTES.forEach { popBackStack(route = it, inclusive = true) }
    val popped = popBackStack(route = "home", inclusive = false)
    if (!popped) {
        navigate("home") {
            popUpTo(graph.startDestinationId) { saveState = true }
            launchSingleTop = true
            restoreState = true
        }
    }
}

private fun entrancesFor(buildingId: String): List<EntranceUI> = when (buildingId) {
    "B" -> listOf(
        EntranceUI("B_West",  "West Entrance",  "B"),
        EntranceUI("B_South", "South Entrance", "B"),
        EntranceUI("B_East",  "East Entrance",  "B")
    )
    "C" -> listOf(
        EntranceUI("C_South", "South Entrance", "C"),
        EntranceUI("C_East",  "East Entrance",  "C")
    )
    "D" -> listOf(
        EntranceUI("D_West", "West Entrance", "D"),
        EntranceUI("D_East", "East Entrance", "D")
    )
    "F" -> listOf(EntranceUI("F_Main", "Main Entrance", "F"))
    "G" -> listOf(EntranceUI("G_Main", "Main Entrance", "G"))
    "H" -> listOf(EntranceUI("H_Main", "Main Entrance", "H"))
    else -> emptyList()
}


// Setup drawer, top bar, bottom bar, dan NavHost
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CampusGuideApp() {
    val navController = rememberNavController()
    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    var globalError by remember { mutableStateOf<String?>(null) }

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val route = navBackStackEntry?.destination?.route
    val args  = navBackStackEntry?.arguments

    val backgroundRes = when (route) {
        "building/{id}" -> {
            when (args?.getString("id")) {
                "B" -> R.drawable.bb
                "C" -> R.drawable.bc
                "D" -> R.drawable.bd
                "F" -> R.drawable.bf
                "H" -> R.drawable.bh
                else -> R.drawable.uph_building_background
            }
        }
        "floor/{b}/{f}" -> {
            when (args?.getString("b")) {
                "B" -> R.drawable.bb
                "C" -> R.drawable.bc
                "D" -> R.drawable.bd
                "F" -> R.drawable.bf
                "H" -> R.drawable.bh
                else -> R.drawable.uph_building_background
            }
        }
        "event/{buildingId}/{eventId}" -> {
            when (args?.getString("buildingId")) {
                "B" -> R.drawable.bb
                "C" -> R.drawable.bc
                "D" -> R.drawable.bd
                "F" -> R.drawable.bf
                "H" -> R.drawable.bh
                else -> R.drawable.uph_building_background
            }
        }
        "event_detail_user/{id}" -> {
            val eventId = args?.getString("id")
            val ctx = LocalContext.current
            val repo = remember { CampusRepoProvider.provide(ctx) }
            val events by remember { repo.streamAllEvents() }.collectAsState(initial = emptyList())
            val e = events.firstOrNull { it.id == eventId }

            var loadedOnce by remember { mutableStateOf(false) }
            var error by remember { mutableStateOf<String?>(null) }

            LaunchedEffect(events) {
                if (!loadedOnce && events.isNotEmpty()) loadedOnce = true
            }

            LaunchedEffect(true) {
                kotlinx.coroutines.delay(4000)
                if (!loadedOnce && events.isEmpty()) {
                    error = "Unable to load event data. Check your internet connection."
                }
            }

            when (e?.buildingId) {
                "B" -> R.drawable.bb
                "C" -> R.drawable.bc
                "D" -> R.drawable.bd
                "F" -> R.drawable.bf
                "H" -> R.drawable.bh
                else -> R.drawable.uph_building_background
            }
        }
        else -> R.drawable.uph_building_background
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            AppDrawer(onAdminLogin = {
                scope.launch { drawerState.close() }
                navController.navigate(ROUTE_ADMIN_LOGIN)
            })
        }
    ) {
        Scaffold(
            topBar = {
                val current = currentRoute(navController)
                val isAdminScreen = current?.startsWith("admin/") == true

                AppTopBar(
                    showSearch = !isAdminScreen,
                    onSearch = { navController.navigate("search") },
                    onMenu = { scope.launch { drawerState.open() } }
                )
            },
            bottomBar = { BottomBar(navController) }
        ) { padding ->
            AppBackground(
                imageRes = backgroundRes,
                imageAlignment = BiasAlignment(0.4f, 0.7f),
                overlayAlpha = 0.85f,
            ) {
                Box(
                    Modifier
                        .fillMaxSize()
                        .padding(padding)
                ) {
                    NavHost(
                        navController = navController,
                        startDestination = "home",
                        modifier = Modifier.matchParentSize()
                    ) {
                        composable("home")   { HomeScreen(navController) }
                        composable("events") { EventsScreen(navController) }
                        composable("search") { SearchScreen(navController) }

                        composable(ROUTE_ADMIN_LOGIN) {
                            AdminLoginScreen(onSuccess = {
                                navController.popBackStack(
                                    route = ROUTE_ADMIN_LOGIN,
                                    inclusive = true
                                )
                                navController.navigate(ROUTE_ADMIN_DASH) {
                                    launchSingleTop = true
                                }
                            })
                        }

                        adminGraph(
                            nav = navController,
                            onGoToMap = { navController.goToHomeClearingAdmin() }
                        )

                        composable("building/{id}") {
                            val id = it.arguments?.getString("id") ?: ""
                            BuildingDetailScreen(id, navController)
                        }
                        composable("floor/{b}/{f}") {
                            val b = it.arguments?.getString("b") ?: ""
                            val f = it.arguments?.getString("f")?.toIntOrNull() ?: 1
                            FloorPlanScreen(navController, b, f)
                        }
                        composable("event/{buildingId}/{eventId}") { backStackEntry ->
                            val eventId = backStackEntry.arguments?.getString("eventId") ?: ""
                            EventDetailScreen(navController, eventId)
                        }
                        composable("sport/{id}") { backStackEntry ->
                            val id = backStackEntry.arguments?.getString("id") ?: ""
                            SportFacilityScreen(navController, id)
                        }
                        composable("events_calendar") {
                            EventsCalendarUserScreen(navController)
                        }

                        composable("user_calendar_day/{date}") { backStackEntry ->
                            val date = backStackEntry.arguments?.getString("date")!!
                            EventsCalendarUserDayScreen(
                                dateStr = date,
                                onBack = { navController.popBackStack() },
                                onEventClick = { eventId ->
                                    navController.navigate("event_detail_user/$eventId")
                                }
                            )
                        }

                        composable("event_detail_user/{id}") {
                            val id = it.arguments?.getString("id")!!
                            EventDetailScreen(navController, id)
                        }
                    }
                }
                ErrorDialog(globalError) { globalError = null }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
// App bar bagian atas
@Composable
private fun AppTopBar(
    showSearch: Boolean,
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
            if (showSearch) {
                IconButton(onClick = onSearch) {
                    Icon(Icons.Default.Search, contentDescription = "Search")
                }
            }
        }
    )
}

// Bottom navigation bar
@Composable
private fun BottomBar(navController: NavHostController) {
    var askLogout by remember { mutableStateOf(false) }
    val current = currentRoute(navController)

    fun goAfterConfirm(block: () -> Unit) {
        if (current?.startsWith("admin/") == true) askLogout = true else block()
        if (askLogout && current?.startsWith("admin/") != true) askLogout = false
    }

    val navItemColors = NavigationBarItemDefaults.colors(
        selectedIconColor   = UPH_White,
        selectedTextColor   = UPH_White,
        unselectedIconColor = UPH_White.copy(alpha = 0.75f),
        unselectedTextColor = UPH_White.copy(alpha = 0.75f),
        indicatorColor      = UPH_Orange
    )

    NavigationBar(containerColor = UPH_Navy) {
        NavigationBarItem(
            selected = current == "home",
            onClick = { goAfterConfirm { navController.goToHomeClearingAdmin() } },
            icon = { Icon(Icons.Default.Map, contentDescription = "Map") },
            label = { Text("Map") },
            colors = navItemColors
        )
        NavigationBarItem(
            selected = current == "events",
            onClick = { goAfterConfirm {
                navController.navigate("events") {
                    popUpTo(navController.graph.startDestinationId) { saveState = true }
                    launchSingleTop = true; restoreState = true
                }
            } },
            icon = { Icon(Icons.Default.Event, contentDescription = "Events") },
            label = { Text("Events") },
            colors = navItemColors
        )
        NavigationBarItem(
            selected = current == "search",
            onClick = { goAfterConfirm {
                navController.navigate("search") {
                    popUpTo(navController.graph.startDestinationId) { saveState = true }
                    launchSingleTop = true; restoreState = true
                }
            } },
            icon = { Icon(Icons.Default.Search, contentDescription = "Search") },
            label = { Text("Search") },
            colors = navItemColors
        )
    }

    if (askLogout) {
        AlertDialog(
            onDismissRequest = { askLogout = false },
            title = { Text("Are you sure you want to log out?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        askLogout = false
                        navController.goToHomeClearingAdmin()
                    },
                    colors = ButtonDefaults.textButtonColors(contentColor = UPH_Navy)
                ) { Text("Continue") }
            },
            dismissButton = {
                TextButton(
                    onClick = { askLogout = false },
                    colors = ButtonDefaults.textButtonColors(contentColor = UPH_Navy)
                ) { Text("Cancel") }
            }
        )
    }
}

@Composable
fun DebugSeedButton() {
    if (!BuildConfig.DEBUG) return // hide in release builds

    val ctx = LocalContext.current
    val scope = rememberCoroutineScope()
    val snackbar = remember { SnackbarHostState() }

    Box(Modifier.fillMaxSize()) {
        SnackbarHost(hostState = snackbar, modifier = Modifier.align(Alignment.BottomCenter))

        Button(
            onClick = {
                scope.launch {
                    try {
                        EventsSeeder.seedFromRaw(ctx)
                        snackbar.showSnackbar("Seeding done (600 events)")
                    } catch (e: Exception) {
                        snackbar.showSnackbar("Seeding failed: ${e.message}")
                    }
                }
            },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp)
        ) {
            Text("Seed Events")
        }
    }
}


// Data untuk hotspot
data class Spot(
    val id: String,
    val x: Float,
    val y: Float,
    val color: Color
)

data class SportSpot(
    val id: String,
    val label: String,
    val x: Float,
    val y: Float
)

data class SportFacility(
    val id: String,
    val title: String,
    val imageKey: String
)

private fun sportFacilityFor(id: String): SportFacility? = when (id) {
    "basketball" -> SportFacility("basketball", "Basketball Court", "basketball")
    "gym"        -> SportFacility("gym",        "Gym",                         "gym")
    "pool"       -> SportFacility("pool",       "Olympic Size Swimming Pool",  "pool")
    "soccer"     -> SportFacility("soccer",     "Soccer Field",                "soccer")
    "multisport" -> SportFacility("multisport", "Multisport Venue",            "multisport")
    else -> null
}

// Home page (map UPH)
@Composable
fun HomeScreen(
    navController: NavHostController,
    debugBorders: Boolean = false
) {
    // ---- Path Mode & selection state ----
    var isPathMode by rememberSaveable { mutableStateOf(false) }

    var startBuildingId by rememberSaveable { mutableStateOf<String?>(null) }
    var endBuildingId   by rememberSaveable { mutableStateOf<String?>(null) }
    var startEntrance   by rememberSaveable { mutableStateOf<EntranceUI?>(null) }
    var endEntrance     by rememberSaveable { mutableStateOf<EntranceUI?>(null) }

    // Which entrance sheet is open: "start", "end", or null
    var showPickerFor by remember { mutableStateOf<String?>(null) }

    // ---- Pathfinding graph & rendered path ----
    val pathRepo = remember { com.example.campusguide.path.PathRepository.get() }
    val graph = remember { pathRepo.graph() }
    var pathNodeIds by remember { mutableStateOf<List<String>>(emptyList()) }

    // ---- Map pins (default colors; F is NOT green anymore) ----
    val hotspots = listOf(
        Spot("B", 0.665f, 0.73f, UPH_Orange),
        Spot("H", 0.55f,  0.77f, Color(0xFFFFC27A)),
        Spot("C", 0.35f,  0.78f, Color(0xFFFFD54F)),
        Spot("D", 0.46f,  0.46f, Color(0xFF1E88E5)),
        Spot("F", 0.23f,  0.36f, Color(0xFF23943c)),
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

                val base = 34.dp
                hotspots.forEach { s ->
                    val xOff = (maxWidth * s.x) - (base / 2)
                    val yOff = (maxHeight * s.y) - (base / 2)

                    val isSelected = isPathMode && (s.id == startBuildingId || s.id == endBuildingId)

                    BuildingDot(
                        id = s.id,
                        color = if (isSelected) colorResource(id = R.color.selected_purple) else s.color,
                        size = base,
                        modifier = Modifier
                            .offset(x = xOff, y = yOff)
                            .clickable {
                                if (!isPathMode) {
                                    navController.navigate("building/${s.id}")
                                } else {
                                    when {
                                        startBuildingId == null || (startBuildingId != null && endBuildingId != null) -> {
                                            startBuildingId = s.id
                                            endBuildingId = null
                                            startEntrance = null
                                            endEntrance = null
                                            pathNodeIds = emptyList()

                                            val es = entrancesFor(s.id)
                                            if (es.size <= 1) {
                                                startEntrance = es.firstOrNull()
                                                if (startEntrance != null && endEntrance != null) {
                                                    pathNodeIds = com.example.campusguide.path.Router.shortestPath(
                                                        graph = graph,
                                                        startId = startEntrance!!.id,
                                                        endId = endEntrance!!.id
                                                    )
                                                }
                                            } else {
                                                showPickerFor = "start"
                                            }
                                        }
                                        endBuildingId == null -> {
                                            endBuildingId = s.id
                                            val es = entrancesFor(s.id)
                                            if (es.size <= 1) {
                                                endEntrance = es.firstOrNull()
                                                if (startEntrance != null && endEntrance != null) {
                                                    pathNodeIds = com.example.campusguide.path.Router.shortestPath(
                                                        graph = graph,
                                                        startId = startEntrance!!.id,
                                                        endId = endEntrance!!.id
                                                    )
                                                }
                                            } else {
                                                showPickerFor = "end"
                                            }
                                        }
                                    }
                                }
                            }
                    )
                }

                // Hotspot untuk sport facilities
                val sportSpots = listOf(
                    SportSpot("basketball", "1", 0.33f, 0.61f),
                    SportSpot("gym",        "2", 0.32f, 0.53f),
                    SportSpot("pool",       "3", 0.30f, 0.47f),
                    SportSpot("soccer",     "4", 0.62f, 0.29f),
                    SportSpot("multisport", "5", 0.75f, 0.11f)
                )

                val sportDotSize = 30.dp

                sportSpots.forEach { s ->
                    val xOff = (maxWidth * s.x) - (sportDotSize / 2)
                    val yOff = (maxHeight * s.y) - (sportDotSize / 2)

                    SportDot(
                        label = s.label,
                        size = sportDotSize,
                        modifier = Modifier
                            .offset(x = xOff, y = yOff)
                            .clickable {
                                navController.navigate("sport/${s.id}")
                            }
                    )
                }
            }

            Canvas(
                modifier = Modifier
                    .matchParentSize()
                    .zIndex(2f)
            ) {
                if (pathNodeIds.size >= 2) {
                    val pts: List<Offset> = pathRepo
                        .toNormalizedOffsets(pathNodeIds)
                        .map { (nx, ny) -> Offset(nx * size.width, ny * size.height) }

                    for (i in 0 until (pts.size - 1)) {
                        drawLine(
                            brush = SolidColor(routeColor),
                            start = pts[i],
                            end   = pts[i + 1],
                            strokeWidth = 10f
                        )
                    }
                }
            }
            PathModeButton(
                enabled = isPathMode,
                onToggle = {
                    isPathMode = !isPathMode
                    if (!isPathMode) {
                        startBuildingId = null
                        endBuildingId = null
                        startEntrance = null
                        endEntrance = null
                        showPickerFor = null
                        pathNodeIds = emptyList()
                    }
                },
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(end = 16.dp, bottom = 88.dp)
                    .zIndex(10f)
            )
        }

        Spacer(Modifier.height(16.dp))

        // Helper text
        val help = when {
            !isPathMode -> "Press the circle button to view detailed information."
            startBuildingId == null -> "Path Mode: choose a START building."
            endBuildingId == null -> "Path Mode: choose a DESTINATION building."
            else -> "Path Mode: path shown. Toggle button to reset."
        }
        Text(help, color = Color.Gray)

        Spacer(Modifier.height(24.dp))
    }

    // ---- Entrance pickers (keep INSIDE HomeScreen) ----
    when (showPickerFor) {
        "start" -> {
            val list = entrancesFor(startBuildingId ?: "")
            EntrancePickerSheet(
                title = "Choose starting entrance — Building $startBuildingId",
                items = list,
                onPick = { e ->
                    startEntrance = e
                    showPickerFor = null
                    if (startEntrance != null && endEntrance != null) {
                        pathNodeIds = com.example.campusguide.path.Router.shortestPath(
                            graph = graph,
                            startId = startEntrance!!.id,
                            endId = endEntrance!!.id
                        )
                    }
                },
                onDismiss = { showPickerFor = null }
            )
        }
        "end" -> {
            val list = entrancesFor(endBuildingId ?: "")
            EntrancePickerSheet(
                title = "Choose destination entrance — Building $endBuildingId",
                items = list,
                onPick = { e ->
                    endEntrance = e
                    showPickerFor = null
                    if (startEntrance != null && endEntrance != null) {
                        pathNodeIds = com.example.campusguide.path.Router.shortestPath(
                            graph = graph,
                            startId = startEntrance!!.id,
                            endId = endEntrance!!.id
                        )
                    }
                },
                onDismiss = { showPickerFor = null }
            )
        }
    }
}

@Composable
fun PathModeButton(
    enabled: Boolean,
    onToggle: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = CircleShape,
        color = if (enabled) Color(0xFF20315C) else Color(0xCC16224C),
        shadowElevation = 6.dp,
        modifier = modifier
            .size(56.dp)
            .clip(CircleShape)
            .clickable { onToggle() }
    ) {
        Box(contentAlignment = Alignment.Center) {
            val iconRes = if (enabled) R.drawable.path_mode_button_invert
            else R.drawable.path_mode_button
            Image(
                painter = painterResource(id = iconRes),
                contentDescription = if (enabled) "Exit Path Mode" else "Enter Path Mode"
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EntrancePickerSheet(
    title: String,
    items: List<EntranceUI>,
    onPick: (EntranceUI) -> Unit,
    onDismiss: () -> Unit
) {
    if (items.isEmpty()) return
    ModalBottomSheet(onDismissRequest = onDismiss, containerColor = Color.White) {
        Column(Modifier.fillMaxWidth().padding(16.dp)) {
            Text(title, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(12.dp))
            items.forEach { e ->
                Surface(
                    onClick = { onPick(e) },
                    shape = RoundedCornerShape(12.dp),
                    tonalElevation = 0.dp,
                    color = Color(0xFFF2F3F7),
                    modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp)
                ) {
                    Row(
                        Modifier.fillMaxWidth().padding(14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("${e.label} • ${e.id}")
                        Icon(Icons.Outlined.KeyboardArrowRight, contentDescription = null)
                    }
                }
            }
            Spacer(Modifier.height(18.dp))
        }
    }
}



// Pin bulat untuk menandai bangunan di peta
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
fun SportDot(
    label: String,
    size: Dp,
    modifier: Modifier = Modifier
) {
    Surface(
        color = UPH_Navy,
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
            Text(
                label,
                color = UPH_White,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

// Menu bar (drawer)
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
                label = "UPH Campus Tour Video",
                leading = { Icon(Icons.Outlined.VideoLibrary, null, tint = textPrimary) },
                trailing = { Icon(Icons.Outlined.KeyboardArrowRight, null, tint = chevron) },
                container = cardDark
            ) { open("https://www.youtube.com/watch?v=m8kHYpeL_q0") }

            DrawerItem(
                label = "UPH Lippo Village Map",
                leading = { Icon(Icons.Outlined.Map, null, tint = textPrimary) },
                trailing = { Icon(Icons.Outlined.KeyboardArrowRight, null, tint = chevron) },
                container = cardDark
            ) { open("https://maps.app.goo.gl/rJouzJ585cAuywr96") }

            DrawerItem(
                label = "UPH Website",
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
                label = "UPH Phone",
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

// Item komponen baris di dalam drawer
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

// Search tab
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
                val normalized = normalizeSearchQuery(newQ)
                scope.launch {
                    results = if (normalized.isBlank()) emptyList() else repo.search(normalized)
                }
            },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Search") },
            placeholder = { Text("Search faculty, room, or event…") },
            trailingIcon = {
                if (q.isNotEmpty()) {
                    IconButton(onClick = { q = ""; results = emptyList() }) {
                        Icon(Icons.Default.Close, contentDescription = "Clear")
                    }
                }
            },
            colors = uphTextFieldColors()
        )

        val scroll = rememberScrollState()
        val showResults = q.isNotBlank()

        Box(Modifier.fillMaxSize()) {
            if (showResults) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(scroll)
                ) {
                    if (results.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("No match found!", color = UPH_Navy)
                        }
                    } else {
                        results.forEach { r ->
                            val title = when (r) {
                                is SearchResult.RoomResult ->
                                    displayRoomCode(r.buildingId, r.title)
                                is SearchResult.EventResult ->
                                    r.title
                                else ->
                                    r.title
                            }

                            val subtitle = when (r) {
                                is SearchResult.RoomResult ->
                                    "${buildingDisplayName(r.buildingId)} • Floor ${r.floor}"

                                is SearchResult.EventResult -> {
                                    val base = prettyEventSubtitle(r.subtitle)
                                    val buildingToken = "Building ${r.buildingId}"
                                    base.replace(buildingToken, buildingDisplayName(r.buildingId))
                                }

                                else ->
                                    r.subtitle
                            }


                            Column(
                                Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        when (r) {
                                            is SearchResult.FacultyResult -> navController.navigate("building/${r.buildingId}")
                                            is SearchResult.RoomResult    -> navController.navigate("floor/${r.buildingId}/${r.floor}")
                                            is SearchResult.EventResult   -> navController.navigate("event/${r.buildingId}/${r.eventId}")
                                        }
                                    }
                                    .padding(12.dp)
                            ) {
                                Text(title, fontWeight = FontWeight.Bold, color = UPH_Navy)
                                Text(subtitle)
                            }
                            Divider()
                        }
                        Spacer(Modifier.height(2.dp))
                    }
                }

                VerticalScrollbar(
                    scroll = scroll,
                    modifier = Modifier
                        .align(Alignment.CenterEnd)
                        .padding(end = 6.dp)
                )
            } else {
                Spacer(Modifier.height(0.dp))
            }
        }
    }
}

// Detail event
@Composable
fun EventDetailScreen(navController: NavHostController, eventId: String) {
    val ctx = LocalContext.current
    val repo = remember { CampusRepoProvider.provide(ctx) }
    val events by remember { repo.streamAllEvents() }.collectAsState(initial = emptyList())
    val e = events.firstOrNull { it.id == eventId }

    var posterUrl by remember(eventId) { mutableStateOf<String?>(null) }
    LaunchedEffect(eventId, e) {
        val fromModel = try {
            @Suppress("UNCHECKED_CAST")
            (e as? Any)?.javaClass?.getDeclaredField("posterUrl")?.let { f ->
                f.isAccessible = true
                (f.get(e) as? String)?.takeIf { it.isNotBlank() }
            }
        } catch (_: Exception) {
            null
        }

        if (fromModel != null) {
            posterUrl = fromModel
            return@LaunchedEffect
        }

        val storage = FirebaseStorage.getInstance().reference
        val candidates = listOf(
            "posters/$eventId.jpg",
            "posters/$eventId.png"
        )
        posterUrl = null
        for (path in candidates) {
            try {
                posterUrl = storage.child(path).downloadUrl.await().toString()
                if (posterUrl != null) break
            } catch (_: Exception) {
            }
        }
    }

    if (e == null) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { Text("Event not found") }
        return
    }

    Column(Modifier.fillMaxSize().padding(16.dp)) {
        BackToMapButton(navController, modifier = Modifier.padding(bottom = 8.dp))

        Text(e.name, style = MaterialTheme.typography.headlineSmall)
        Text("Held by: ${e.heldBy}")
        Text("${buildingDisplayName(e.buildingId)} • Room ${displayRoomCode(e.buildingId, e.room)}")
        Text("Starts: ${e.start.format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"))}")
        Text("Ends: ${e.end.format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"))}")
        Spacer(Modifier.height(16.dp))
        Button(
            onClick = {
                val intent = Intent(Intent.ACTION_INSERT).apply {
                    data = CalendarContract.Events.CONTENT_URI
                    putExtra(CalendarContract.Events.TITLE, e.name)
                    putExtra(
                        CalendarContract.Events.EVENT_LOCATION,
                        "${buildingDisplayName(e.buildingId)} • Room ${displayRoomCode(e.buildingId, e.room)}"
                    )
                    putExtra(CalendarContract.EXTRA_EVENT_BEGIN_TIME, e.start.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli())
                    putExtra(CalendarContract.EXTRA_EVENT_END_TIME, e.end.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli())
                }
                ctx.startActivity(intent)
            },
            colors = ButtonDefaults.buttonColors(
                containerColor = UPH_Navy,
                contentColor = UPH_White
            )
        ) { Text("Add to Calendar") }
        Spacer(Modifier.height(12.dp))
        posterUrl?.let { url ->
            SubcomposeAsyncImage(
                model = coil.request.ImageRequest.Builder(ctx)
                    .data(url)
                    .crossfade(true)
                    .build(),
                contentDescription = "Event Poster",
                contentScale = ContentScale.Fit,
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 160.dp, max = 420.dp)
                    .clip(RoundedCornerShape(30.dp))
                    .align(Alignment.CenterHorizontally)
            ) {
                when (painter.state) {
                    is coil.compose.AsyncImagePainter.State.Loading -> {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(min = 160.dp, max = 420.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(
                                color = UPH_Navy,
                                trackColor = UPH_Navy.copy(alpha = 0.15f)
                            )
                        }
                    }
                    is coil.compose.AsyncImagePainter.State.Success -> {
                        SubcomposeAsyncImageContent()
                    }
                    else -> {
                        Spacer(Modifier.height(0.dp))
                    }
                }
            }

            Spacer(Modifier.height(12.dp))
        }
    }
}

private val sportDescriptions = mapOf(
    "basketball" to
            "Our indoor basketball court is home to UPH’s championship-winning teams, offering international-standard facilities that support the players’ journey to consistent athletic excellence.",

    "soccer" to
            "With a well-maintained pitch, our soccer field provides an outstanding experience for players looking to train or compete in matches.",

    "pool" to
            "Our Olympic-sized swimming pool provides a quality setting for students to refine their techniques, enjoy a refreshing swim, and challenge themselves in aquatic sports.",

    "multisport" to
            "At our futsal and badminton court, students can refine their skills, reach their athletic goals and follow in the footsteps of UPH’s past champions.",

    "gym" to
            "With spacious workout zones and quality equipment, our gym is a place where students can stay active and achieve their fitness goals for a healthier life."
)

@Composable
fun SportFacilityScreen(navController: NavHostController, facilityId: String) {
    val ctx = LocalContext.current
    val facility = sportFacilityFor(facilityId)

    if (facility == null) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("Facility not found")
        }
        return
    }

    var imageUrl by remember(facilityId) { mutableStateOf<String?>(null) }
    var isLoading by remember(facilityId) { mutableStateOf(true) }

    LaunchedEffect(facilityId) {
        isLoading = true
        imageUrl = null

        val storage = FirebaseStorage.getInstance().reference
        val candidates = listOf(
            "sports/${facility.imageKey}.jpg",
            "sports/${facility.imageKey}.jpeg",
            "sports/${facility.imageKey}.png"
        )

        for (path in candidates) {
            try {
                val url = storage.child(path).downloadUrl.await()
                imageUrl = url.toString()
                break
            } catch (_: Exception) {
            }
        }
        isLoading = false
    }

    Column(
        Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        BackToMapButton(navController, modifier = Modifier.padding(bottom = 8.dp))

        Text(
            facility.title,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )
        Spacer(Modifier.height(12.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 180.dp, max = 360.dp)
                .clip(RoundedCornerShape(24.dp))
                .background(Color(0xFFE9EDF6)),
            contentAlignment = Alignment.Center
        ) {
            when {
                isLoading && imageUrl == null -> {
                    Box(
                        modifier = Modifier.matchParentSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(
                            modifier   = Modifier.size(40.dp),
                            color      = UPH_Navy,
                            trackColor = UPH_Navy.copy(alpha = 0.15f)
                        )
                    }
                }
                imageUrl != null -> {
                    SubcomposeAsyncImage(
                        model = imageUrl,
                        contentDescription = facility.title,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.matchParentSize()
                    ) {
                        when (painter.state) {
                            is AsyncImagePainter.State.Success -> SubcomposeAsyncImageContent()
                            is AsyncImagePainter.State.Loading -> {
                                Box(
                                    modifier = Modifier.matchParentSize(),
                                    contentAlignment = Alignment.Center
                                ) {
                                    CircularProgressIndicator(
                                        modifier   = Modifier.size(40.dp),
                                        color      = UPH_Navy,
                                        trackColor = UPH_Navy.copy(alpha = 0.15f)
                                    )
                                }
                            }
                            else -> {  }
                        }
                    }
                }
                else -> {
                    Text(
                        "No image available",
                        color = UPH_Navy
                    )
                }
            }
        }

        Spacer(Modifier.height(20.dp))

        Text(
            "Description",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold
        )
        Spacer(Modifier.height(6.dp))
        Text(
            sportDescriptions[facilityId]
                ?: "No description available.",
            color = UPH_Navy
        )
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

    val outerScroll = rememberScrollState()

    Box(Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(outerScroll)
                .padding(12.dp)
        ) {
            BackToMapButton(navController, Modifier.padding(bottom = 8.dp))

            Text(
                building?.name ?: "Building $buildingId",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )

            // === Faculties section (hide if none) ===
            val allFac = InMemoryCampusRepository.facultyInBuilding
            val faculties = allFac.filterValues { it == buildingId }.keys.toList()

            if (faculties.isNotEmpty()) {
                Text("List of Faculties", style = MaterialTheme.typography.titleMedium)
                Spacer(Modifier.height(6.dp))

                val facScroll = rememberScrollState()
                Box(Modifier.fillMaxWidth()) {
                    Row(Modifier.horizontalScroll(facScroll)) {
                        faculties.forEach { f ->
                            FacultyCard(
                                title = f,
                                modifier = Modifier.padding(end = 8.dp)
                            )
                        }
                    }
                    HorizontalScrollbar(
                        scroll = facScroll,
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .padding(top = 4.dp)
                    )
                }

                Spacer(Modifier.height(12.dp))
                Divider()
                Spacer(Modifier.height(12.dp))
            }

            FrequentlyVisitedSection(buildingId = buildingId, navController = navController)

            Spacer(Modifier.height(12.dp))
            Divider()
            Spacer(Modifier.height(8.dp))

            Text("Floor Navigation", style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(6.dp))
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                floors.forEach { f ->
                    OutlinedButton(
                        onClick = { navController.navigate("floor/${buildingId}/$f") },
                        border = BorderStroke(1.dp, UPH_Navy),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = UPH_Navy
                        )
                    ) { Text("Floor $f") }
                }
            }

            Spacer(Modifier.height(12.dp))
            Divider()
            Spacer(Modifier.height(8.dp))

            Text("Events", style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(6.dp))

            Column {
                events.forEach { e ->
                    EventCard(e) {
                        navController.navigate("event/${e.buildingId}/${e.id}")
                    }
                    Spacer(Modifier.height(8.dp))
                }
            }

            Spacer(Modifier.height(12.dp))
        }

        com.example.campusguide.ui.common.VerticalScrollbar(
            scroll = outerScroll,
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .padding(end = 6.dp)
        )
    }
}

// Layar denah lantai yang memuat gambar dari Firebase Storage
@Composable
fun FloorPlanScreen(navController: NavHostController, buildingId: String, floor: Int) {
    val resName = "b" + buildingId.lowercase() + "_f" + floor
    val storagePath = "maps/$buildingId/$resName.jpg"
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
                    coil.compose.SubcomposeAsyncImage(
                        model = url,
                        contentDescription = "Floor plan",
                        contentScale = ContentScale.Fit,
                        modifier = Modifier.fillMaxSize()
                    ) {
                        when (painter.state) {
                            is AsyncImagePainter.State.Success -> {
                                Image(
                                    painter = painter,
                                    contentDescription = "Floor plan",
                                    contentScale = ContentScale.Fit,
                                    modifier = Modifier.fillMaxSize()
                                )
                            }
                            else -> {
                                Box(
                                    modifier = Modifier.fillMaxSize(),
                                    contentAlignment = Alignment.Center
                                ) {
                                    CircularProgressIndicator(
                                        modifier   = Modifier.size(40.dp),
                                        color      = UPH_Navy,
                                        trackColor = UPH_Navy.copy(alpha = 0.15f),
                                        strokeWidth = 4.dp
                                    )
                                }
                            }
                        }
                    }
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

                else -> Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(
                        modifier   = Modifier.size(40.dp),
                        color      = UPH_Navy,
                        trackColor = UPH_Navy.copy(alpha = 0.15f),
                        strokeWidth = 4.dp
                    )
                }
            }
        }

        BackToMapButton(navController, Modifier.align(Alignment.TopStart).padding(8.dp))
    }
}

// Layar daftar event
@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun EventsScreen(navController: NavHostController) {
    val ctx = LocalContext.current
    val repo = remember { CampusRepoProvider.provide(ctx) }

    val allEvents by remember { repo.streamAllEvents() }
        .collectAsState(initial = emptyList())

    var error by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(true) {
        try {
        } catch (e: Exception) {
            error = "Unable to load events. Please check your connection."
        }
    }

    var loadedOnce by remember { mutableStateOf(false) }
    LaunchedEffect(allEvents) {
        if (!loadedOnce && allEvents.isNotEmpty()) loadedOnce = true
    }
    val isLoading = !loadedOnce

    LaunchedEffect(true) {
        kotlinx.coroutines.delay(8000)
        if (!loadedOnce && allEvents.isEmpty()) {
            error = "Unable to load events. Please check your internet connection."
        }
    }

    var building by remember { mutableStateOf("All") }
    val buildingOptions = listOf("All") + InMemoryCampusRepository.buildings.map { it.id }

    var status by remember { mutableStateOf("All") }
    val statuses = listOf("All", "Ongoing", "Upcoming", "Coming Soon")

    var startDate by remember { mutableStateOf(LocalDate.now()) }
    var endDate   by remember { mutableStateOf(LocalDate.now().plusDays(7)) }

    var showDatePickerStart by remember { mutableStateOf(false) }
    var showDatePickerEnd by remember { mutableStateOf(false) }

    val today = LocalDate.now()

    val now = LocalDateTime.now()
    val filtered = remember(allEvents, building, status, now) {
        allEvents.filter { e ->
            val inBuilding = (building == "All") || (e.buildingId == building)
            val inStatus = when (status) {
                "Ongoing"     -> now.isAfter(e.start) && now.isBefore(e.end)
                "Upcoming"    -> e.start.isAfter(now) && e.start.isAfter(now.plusDays(7))
                "Coming Soon" -> e.start.isAfter(now) && e.start.isBefore(now.plusDays(14))
                else -> true
            }
            inBuilding && inStatus
        }.sortedBy { it.start }
    }

    Column(Modifier.fillMaxSize().padding(12.dp)) {
        Text("Events — $today", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(8.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            var buildingExpanded by remember { mutableStateOf(false) }

            ExposedDropdownMenuBox(
                expanded = buildingExpanded,
                onExpandedChange = { buildingExpanded = !buildingExpanded },
                modifier = Modifier.weight(1f)
            ) {
                val buildingLabel = if (building == "All") {
                    "All"
                } else {
                    buildingDisplayName(building)
                }

                OutlinedTextField(
                    value = buildingLabel,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Building") },
                    trailingIcon = {
                        ExposedDropdownMenuDefaults.TrailingIcon(expanded = buildingExpanded)
                    },
                    modifier = Modifier
                        .menuAnchor()
                        .fillMaxWidth(),
                    colors = TextFieldDefaults.colors(
                        focusedIndicatorColor = UPH_Navy,
                        focusedLabelColor = UPH_Navy,
                        cursorColor = UPH_Navy,
                        focusedTrailingIconColor = UPH_Navy,
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent
                    )
                )

                ExposedDropdownMenu(
                    expanded = buildingExpanded,
                    onDismissRequest = { buildingExpanded = false },
                    containerColor = UPH_Navy,
                    shape = RoundedCornerShape(12.dp)
                ) {
                    buildingOptions.forEach { id ->
                        val label = if (id == "All") "All" else buildingDisplayName(id)
                        DropdownMenuItem(
                            text = { Text(label, color = UPH_White) },
                            onClick = {
                                building = id
                                buildingExpanded = false
                            },
                            colors = MenuDefaults.itemColors(textColor = UPH_White)
                        )
                    }
                }
            }

            var statusExpanded by remember { mutableStateOf(false) }

            ExposedDropdownMenuBox(
                expanded = statusExpanded,
                onExpandedChange = { statusExpanded = !statusExpanded },
                modifier = Modifier.weight(1f)
            ) {
                OutlinedTextField(
                    value = status,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Status") },
                    trailingIcon = {
                        ExposedDropdownMenuDefaults.TrailingIcon(expanded = statusExpanded)
                    },
                    modifier = Modifier
                        .menuAnchor()
                        .fillMaxWidth(),
                    colors = TextFieldDefaults.colors(
                        focusedIndicatorColor = UPH_Navy,
                        focusedLabelColor = UPH_Navy,
                        cursorColor = UPH_Navy,
                        focusedTrailingIconColor = UPH_Navy,
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent
                    )
                )

                ExposedDropdownMenu(
                    expanded = statusExpanded,
                    onDismissRequest = { statusExpanded = false },
                    containerColor = UPH_Navy,
                    shape = RoundedCornerShape(12.dp)
                ) {
                    statuses.forEach { s ->
                        DropdownMenuItem(
                            text = { Text(s, color = UPH_White) },
                            onClick = {
                                status = s
                                statusExpanded = false
                            },
                            colors = MenuDefaults.itemColors(textColor = UPH_White)
                        )
                    }
                }
            }
        }

        Spacer(Modifier.height(8.dp))

        Row(
            Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            FilterPill("From: $startDate") { showDatePickerStart = true }
            Spacer(Modifier.width(8.dp))
            FilterPill("To: $endDate") { showDatePickerEnd = true }

            Spacer(Modifier.weight(1f))

            IconButton(onClick = { navController.navigate("events_calendar") }) {
                Icon(
                    Icons.Default.Event,
                    contentDescription = "Calendar",
                    tint = UPH_Navy
                )
            }
        }

        if (showDatePickerStart) {
            val dpState = rememberDatePickerState(
                initialSelectedDateMillis = startDate
                    .atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
            )
            DatePickerDialog(
                onDismissRequest = { showDatePickerStart = false },
                confirmButton = {
                    TextButton(
                        onClick = {
                            dpState.selectedDateMillis?.let { millis ->
                                startDate = Instant.ofEpochMilli(millis)
                                    .atZone(ZoneId.systemDefault()).toLocalDate()
                                if (startDate.isAfter(endDate)) endDate = startDate
                            }
                            showDatePickerStart = false
                        },
                        colors = uphTextBtnColors()
                    ) { Text("OK") }
                }
            ) {
                DatePicker(
                    state = dpState,
                    colors = uphDatePickerColors()
                )
            }
        }

        if (showDatePickerEnd) {
            val dpState2 = rememberDatePickerState(
                initialSelectedDateMillis = endDate
                    .atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
            )
            DatePickerDialog(
                onDismissRequest = { showDatePickerEnd = false },
                confirmButton = {
                    TextButton(
                        onClick = {
                            dpState2.selectedDateMillis?.let { millis ->
                                endDate = Instant.ofEpochMilli(millis)
                                    .atZone(ZoneId.systemDefault()).toLocalDate()
                                if (endDate.isBefore(startDate)) startDate = endDate
                            }
                            showDatePickerEnd = false
                        },
                        colors = uphTextBtnColors()
                    ) { Text("OK") }
                }
            ) {
                DatePicker(
                    state = dpState2,
                    colors = uphDatePickerColors()
                )
            }
        }

        Spacer(Modifier.height(8.dp))

        val scroll = rememberScrollState()

        if (isLoading) {
            LinearProgressIndicator(
                modifier = Modifier.fillMaxWidth(),
                color = UPH_Navy,
                trackColor = UPH_Navy.copy(alpha = 0.18f)
            )
            Spacer(Modifier.height(8.dp))
        }

        Box(Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(scroll)
            ) {
                filtered.forEach { e ->
                    EventCard(e) {
                        navController.navigate("event/${e.buildingId}/${e.id}")
                    }
                    Spacer(Modifier.height(8.dp))
                }
                Spacer(Modifier.height(2.dp))
            }

            VerticalScrollbar(
                scroll = scroll,
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .padding(end = 6.dp)
            )
        }
    }
    ErrorDialog(error) { error = null }
    DebugSeedButton()
}

@Composable
private fun FacultyCard(
    title: String,
    modifier: Modifier = Modifier
) {
    var imageUrl  by remember(title) { mutableStateOf<String?>(null) }
    var isLoading by remember(title) { mutableStateOf(true) }

    LaunchedEffect(title) {
        isLoading = true
        imageUrl = null

        val base = InMemoryCampusRepository.facultyImageFiles[title]
        Log.d("FacultyCard", "Title = '$title', base = '$base'")

        if (base != null) {
            val storage = FirebaseStorage.getInstance().reference
            val candidates = listOf(
                "faculties/$base.jpg",
                "faculties/$base.jpeg",
                "faculties/$base.png"
            )

            for (path in candidates) {
                try {
                    Log.d("FacultyCard", "Trying path: $path")
                    val url = storage.child(path).downloadUrl.await()
                    imageUrl = url.toString()
                    Log.d("FacultyCard", "SUCCESS for $path -> $imageUrl")
                    break
                } catch (e: Exception) {
                    Log.e("FacultyCard", "FAILED for $path: ${e.message}")
                }
            }
        } else {
            Log.w("FacultyCard", "No base name for title='$title'")
        }

        isLoading = false
    }

    Surface(
        shape = RoundedCornerShape(16.dp),
        color = Color(0xFFF2F3F7),
        tonalElevation = 1.dp,
        modifier = modifier.width(FACULTY_CARD_WIDTH)
    ) {
        Column(
            Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(FACULTY_TITLE_HEIGHT),
                contentAlignment = Alignment.TopStart
            ) {
                Text(
                    title,
                    color = UPH_Navy,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }

            when {
                isLoading -> {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(FACULTY_IMAGE_HEIGHT)
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0xFFE9EDF6)),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(
                            color = UPH_Navy,
                            trackColor = UPH_Navy.copy(alpha = 0.15f),
                            strokeWidth = 3.dp
                        )
                    }
                }

                imageUrl != null -> {
                    SubcomposeAsyncImage(
                        model = imageUrl,
                        contentDescription = "Faculty photo",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(FACULTY_IMAGE_HEIGHT)
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0xFFE9EDF6))
                    ) {
                        when (painter.state) {
                            is AsyncImagePainter.State.Success -> SubcomposeAsyncImageContent()
                            is AsyncImagePainter.State.Loading -> {
                                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                    CircularProgressIndicator(
                                        color = UPH_Navy,
                                        trackColor = UPH_Navy.copy(alpha = 0.15f),
                                        strokeWidth = 3.dp
                                    )
                                }
                            }
                            else -> {
                                Box(
                                    Modifier
                                        .fillMaxSize()
                                        .background(Color(0xFFE9EDF6))
                                )
                            }
                        }
                    }
                }

                else -> {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(FACULTY_IMAGE_HEIGHT)
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0xFFE9EDF6))
                    )
                }
            }
        }
    }
}

@Composable
private fun FeaturedPlaceCard(
    place: FrequentlyVisitedPlace,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var imageUrl  by remember(place.imageKey) { mutableStateOf<String?>(null) }
    var isLoading by remember(place.imageKey) { mutableStateOf(true) }

    LaunchedEffect(place.imageKey) {
        isLoading = true
        imageUrl = null

        val storage = FirebaseStorage.getInstance().reference
        val candidates = listOf(
            "featured/${place.imageKey}.jpg",
            "featured/${place.imageKey}.jpeg",
            "featured/${place.imageKey}.png"
        )

        for (path in candidates) {
            try {
                val url = storage.child(path).downloadUrl.await()
                imageUrl = url.toString()
                break
            } catch (_: Exception) {
            }
        }
        isLoading = false
    }

    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(16.dp),
        color = Color(0xFFF2F3F7),
        tonalElevation = 1.dp,
        modifier = modifier.width(FACULTY_CARD_WIDTH)
    ) {
        Column(
            Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                place.name,
                color = UPH_Navy,
                fontWeight = FontWeight.SemiBold,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(FACULTY_IMAGE_HEIGHT)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFFE9EDF6)),
                contentAlignment = Alignment.Center
            ) {
                when {
                    isLoading && imageUrl == null -> {
                        CircularProgressIndicator(
                            modifier   = Modifier.size(32.dp),
                            color      = UPH_Navy,
                            trackColor = UPH_Navy.copy(alpha = 0.15f),
                            strokeWidth = 3.dp
                        )
                    }

                    imageUrl != null -> {
                        SubcomposeAsyncImage(
                            model = imageUrl,
                            contentDescription = place.name,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.matchParentSize()
                        ) {
                            when (painter.state) {
                                is AsyncImagePainter.State.Success -> {
                                    SubcomposeAsyncImageContent()
                                }
                                is AsyncImagePainter.State.Loading -> {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxSize(),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        CircularProgressIndicator(
                                            modifier   = Modifier.size(32.dp),
                                            color      = UPH_Navy,
                                            trackColor = UPH_Navy.copy(alpha = 0.15f),
                                            strokeWidth = 3.dp
                                        )
                                    }
                                }
                                else -> {
                                }
                            }
                        }
                    }
                    else -> {
                    }
                }
            }

            Box(
                modifier = Modifier
                    .background(
                        color = UPH_Navy.copy(alpha = 0.06f),
                        shape = RoundedCornerShape(999.dp)
                    )
                    .padding(horizontal = 10.dp, vertical = 4.dp)
            ) {
                Text(
                    text = "Floor ${place.floor}",
                    color = UPH_Navy,
                    style = MaterialTheme.typography.labelMedium
                )
            }
        }
    }
}

@Composable
private fun FrequentlyVisitedSection(
    buildingId: String,
    navController: NavHostController
) {
    val items = InMemoryCampusRepository
        .frequentlyVisitedByBuilding[buildingId]
        .orEmpty()

    if (items.isEmpty()) return

    Text("Frequently Visited", style = MaterialTheme.typography.titleMedium)
    Spacer(Modifier.height(6.dp))

    val scroll = rememberScrollState()
    Box(Modifier.fillMaxWidth()) {
        Row(Modifier.horizontalScroll(scroll)) {
            items.forEach { place ->
                FeaturedPlaceCard(
                    place = place,
                    onClick = {
                        navController.navigate("floor/${place.buildingId}/${place.floor}")
                    },
                    modifier = Modifier.padding(end = 8.dp)
                )
            }
        }

        HorizontalScrollbar(
            scroll = scroll,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(top = 4.dp)
        )
    }
}

// Mengembalikan label status event dan warna badge
@Composable
fun StatusBadge(e: CampusEvent): Pair<String, Color> {
    val now = LocalDateTime.now()

    return when {
        now.isAfter(e.end) ->
            "Past" to Color.LightGray

        now.isAfter(e.start) && now.isBefore(e.end) ->
            "Ongoing" to Color(0xFFE0F2FF)

        e.start.isAfter(now) && e.start.isBefore(now.plusDays(7)) ->
            "Coming Soon" to Color(0xFFF7EDE3)

        else ->
            "Upcoming" to Color(0xFFEAF7E9)
    }
}

@Composable
fun FilterPill(text: String, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(20.dp),
        color = UPH_Navy,
        contentColor = UPH_White,
        tonalElevation = 0.dp,
        border = BorderStroke(1.dp, UPH_White)
    ) {
        Row(
            Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text, color = UPH_White)
        }
    }
}

// Event card untuk membuka detailed event
@Composable
fun EventCard(e: CampusEvent, onClick: () -> Unit = {}) {
    val (label, bg) = StatusBadge(e)
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = Color(0xFFF2F3F7),
        tonalElevation = 1.dp,
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
    ) {
        Column(Modifier.padding(16.dp)) {
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(e.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Box(
                    Modifier
                        .background(bg, shape = RoundedCornerShape(12.dp))
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text(label)
                }
            }
            Spacer(Modifier.height(6.dp))
            Text("Held By: ${e.heldBy}")
            Text("Date: ${e.start.toLocalDate()}")
            Text("Time: ${e.start.toLocalTime()} – ${e.end.toLocalTime()}")
            Text("Room: ${displayRoomCode(e.buildingId, e.room)}")
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

// Tombol bulat untuk kembali ke halaman peta/home
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

// Utility function
private fun cleanRoom(room: String): String {
    val t = room.trim()
    return if (t.startsWith("Room", ignoreCase = true)) t.removePrefix("Room").trimStart() else t
}

private fun roomLabel(room: String): String = "Room ${cleanRoom(room)}"

private fun prettyEventSubtitle(s: String): String {
    var t = s.replace("•", "• ")
    t = t.replace(Regex("""\b([A-Z])\s*Room\b"""), "$1 • Room")
    t = t.replace(Regex("""\s{2,}"""), " ")
    return t.trim()
}

private fun normalizeSearchQuery(q: String): String {
    val t = q.trim()
    if (t.length >= 2 && t.startsWith("hp", ignoreCase = true)) {
        val rest = t.substring(2)
        return if (rest.isBlank()) "H" else "H$rest"
    }
    return t
}


fun buildingDisplayName(id: String?): String {
    return when (id) {
        "H" -> "Building HOPE"
        null -> ""
        else -> "Building $id"
    }
}

fun displayRoomCode(buildingId: String?, room: String): String {
    val clean = room.trim()

    return if (buildingId == "H" && clean.startsWith("H")) {
        "HP" + clean.removePrefix("H")
    } else clean
}

@Composable
fun ErrorDialog(message: String?, onDismiss: () -> Unit) {
    if (message == null) return

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Failed to load data") },
        text = { Text(message) },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("OK", color = UPH_Navy)
            }
        }
    )
}
