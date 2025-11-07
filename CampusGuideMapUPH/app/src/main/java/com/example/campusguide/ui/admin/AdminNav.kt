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

// Menambahkan seluruh rute/halaman admin ke NavGraph aplikasi
fun NavGraphBuilder.adminGraph(
    nav: NavController,
    onGoToMap: () -> Unit,
){
    composable(ROUTE_ADMIN_DASH) {
        val vm: EventsViewModel = viewModel()
        DashboardScreen(
            vm = vm,
            onAdd = { nav.navigate(ROUTE_ADMIN_ADD) },          // Menuju form add
            onEdit = { id -> nav.navigate("admin/edit/$id") },  // Menuju form edit
            onRemove = { nav.navigate(ROUTE_ADMIN_REMOVE) },    // Menuju form remove
            onHistory = { nav.navigate(ROUTE_ADMIN_HISTORY) },   // Menuju form history
            onGoToMap = onGoToMap
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
}
