package com.example.campusguide.ui.admin

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.campusguide.data.Event
import com.example.campusguide.data.EventsRepository
import com.google.firebase.Timestamp
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit

data class AdminState(
    val events: List<Event> = emptyList(),
    val loading: Boolean = false,
    val error: String? = null,
    val countOngoing: Int = 0,
    val countUpcoming: Int = 0,
    val countComingSoon: Int = 0
)

// ViewModel antara UI admin dan repository event
class EventsViewModel(
    private val repo: EventsRepository = EventsRepository()
) : ViewModel() {

    private val _state = MutableStateFlow(AdminState())
    val state: StateFlow<AdminState> = _state

    // Mengambil ulang daftar event dan membuat statistik
    fun refresh() {
        viewModelScope.launch {
            _state.value = _state.value.copy(loading = true, error = null)
            try {
                val list = repo.listAllFuture()
                val now = Timestamp.now()
                val in7 = Timestamp(now.seconds + TimeUnit.DAYS.toSeconds(7), 0)
                val ongoing = list.count { it.isOngoing(now) }
                val upcoming = list.count { !it.isOngoing(now) && it.date <= in7 }
                val coming = list.count { it.date > in7 }
                _state.value = _state.value.copy(
                    events = list, loading = false,
                    countOngoing = ongoing, countUpcoming = upcoming, countComingSoon = coming
                )
            } catch (e: Exception) {
                _state.value = _state.value.copy(loading = false, error = e.message)
            }
        }
    }

    // Membuat event baru
    fun create(event: Event, posterUri: Uri?, onDone: (String)->Unit, onError: (String)->Unit) {
        viewModelScope.launch {
            try {
                val id = repo.create(event, posterUri)
                refresh()
                onDone(id)
            } catch (e: Exception) {
                onError(e.message ?: "Failed to add")
            }
        }
    }

    // Memperbarui data event
    fun update(event: Event, posterUri: Uri?, onDone: ()->Unit, onError: (String)->Unit) {
        viewModelScope.launch {
            try {
                repo.update(event.id, event, posterUri)
                refresh()
                onDone()
            } catch (e: Exception) {
                onError(e.message ?: "Failed to update")
            }
        }
    }

    // Mengubah status event published/unpublished
    fun setPublished(id: String, published: Boolean, onDone: ()->Unit, onError: (String)->Unit) {
        viewModelScope.launch {
            try {
                repo.setPublished(id, published)
                refresh()
                onDone()
            } catch (e: Exception) {
                onError(e.message ?: "Failed to update visibility")
            }
        }
    }

    // Menghapus event dari database
    fun delete(id: String, onDone: ()->Unit, onError: (String)->Unit) {
        viewModelScope.launch {
            try {
                repo.delete(id)
                refresh()
                onDone()
            } catch (e: Exception) {
                onError(e.message ?: "Failed to delete")
            }
        }
    }

    fun loadPastEvents(onDone: (List<Event>) -> Unit, onError: (String) -> Unit) {
        viewModelScope.launch {
            try {
                val list = repo.listPast()
                onDone(list)
            } catch (e: Exception) {
                onError(e.message ?: "Failed to load history")
            }
        }
    }
}
