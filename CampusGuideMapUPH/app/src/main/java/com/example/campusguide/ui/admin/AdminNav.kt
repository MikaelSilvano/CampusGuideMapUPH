package com.example.campusguide.ui.admin

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.NavController

const val ROUTE_ADMIN_LOGIN  = "admin/login"
const val ROUTE_ADMIN_DASH   = "admin/dashboard"
const val ROUTE_ADMIN_ADD    = "admin/add"
const val ROUTE_ADMIN_EDIT   = "admin/edit/{eventId}"
const val ROUTE_ADMIN_REMOVE = "admin/remove"
const val ROUTE_ADMIN_HISTORY = "admin/history"
const val ROUTE_ADMIN_CALENDAR = "admin/calendar_admin"
const val ROUTE_ADMIN_CALENDAR_DAY = "admin/calendar_admin_day/{date}"

fun NavGraphBuilder.adminGraph(
    nav: NavController,
    onGoToMap: () -> Unit,
) {
    composable(ROUTE_ADMIN_DASH) {
        val vm: EventsViewModel = viewModel()
        DashboardScreen(
            vm = vm,
            onAdd = { nav.navigate(ROUTE_ADMIN_ADD) },
            onEdit = { id -> nav.navigate("admin/edit/$id") },
            onRemove = { nav.navigate(ROUTE_ADMIN_REMOVE) },
            onHistory = { nav.navigate(ROUTE_ADMIN_HISTORY) },
            onGoToMap = onGoToMap,
            onCalendar = { nav.navigate(ROUTE_ADMIN_CALENDAR) } // ← HERE
        )
    }
    composable(ROUTE_ADMIN_ADD) {
        val vm: EventsViewModel = viewModel()
        AddOrEditEventScreen(
            mode = FormMode.Add,
            vm = vm,
            onDone = { nav.popBackStack() },
            onCancel = { nav.popBackStack() }
        )
    }
    composable(ROUTE_ADMIN_EDIT) { backStackEntry ->
        val id = backStackEntry.arguments?.getString("eventId").orEmpty()
        val vm: EventsViewModel = viewModel()
        AddOrEditEventScreen(
            mode = FormMode.Edit(id),
            vm = vm,
            onDone = { nav.popBackStack() },
            onCancel = { nav.popBackStack() }
        )
    }
    composable(ROUTE_ADMIN_REMOVE) {
        val vm: EventsViewModel = viewModel()
        RemoveEventScreen(
            vm = vm,
            onBack = { nav.popBackStack() }
        )
    }
    composable(ROUTE_ADMIN_HISTORY) {
        val vm: EventsViewModel = viewModel()
        HistoryScreen(
            vm = vm,
            onBack = { nav.popBackStack() }
        )
    }
    composable(ROUTE_ADMIN_CALENDAR) {
        val vm: EventsViewModel = viewModel()
        CalendarAdminScreen(
            vm = vm,
            onBack = { nav.popBackStack() },
            onSelectDate = { date ->
                val route = ROUTE_ADMIN_CALENDAR_DAY.replace("{date}", date.toString())
                nav.navigate(route)
            }
        )
    }

    composable(ROUTE_ADMIN_CALENDAR_DAY) { backStackEntry ->
        val date = backStackEntry.arguments?.getString("date") ?: ""

        val vm: EventsViewModel = viewModel()
        CalendarDayEventsScreen(
            dateStr = date,
            vm = vm,
            onBack = { nav.popBackStack() },
            onEventClick = { eventId ->
                nav.navigate("admin/edit/$eventId")
            }
        )
    }
}
