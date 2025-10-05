package com.example.campusguide.data

import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.coroutines.flow.*
import java.time.LocalDateTime
import java.time.ZoneId

// Repository berbasis Firestore untuk membaca data kampus & event secara real-time
class FirestoreCampusRepository(
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()
) : CampusRepository {

    // StateFlow semua event (dipublish)
    private val _events = MutableStateFlow<List<CampusEvent>>(emptyList())
    val eventsFlow: StateFlow<List<CampusEvent>> = _events.asStateFlow()

    private var globalReg: ListenerRegistration? = null

    init {
        startGlobalListener()
    }

    // Snapshot listener Firestore untuk koleksi events
    private fun startGlobalListener() {
        if (globalReg != null) return

        val cal = java.util.Calendar.getInstance().apply {
            set(java.util.Calendar.HOUR_OF_DAY, 0)
            set(java.util.Calendar.MINUTE, 0)
            set(java.util.Calendar.SECOND, 0)
            set(java.util.Calendar.MILLISECOND, 0)
        }
        val todayStart = Timestamp(cal.time)

        globalReg = db.collection("events")
            .whereGreaterThanOrEqualTo("date", todayStart)
            .orderBy("date")
            .addSnapshotListener { snap, err ->
                if (err != null) {
                    _events.value = emptyList()
                    return@addSnapshotListener
                }
                val list = snap?.documents.orEmpty().mapNotNull { d ->
                    val ev = d.toObject(Event::class.java) ?: return@mapNotNull null
                    val e = ev.copy(id = d.id)
                    if (e.published) e.toCampusEvent() else null
                }
                _events.value = list
            }
    }

    // Mengalirkan semua event
    override fun streamAllEvents(): Flow<List<CampusEvent>> = eventsFlow

    // Mengalirkan event untuk building tertentu
    override fun streamEventsForBuilding(buildingId: String): Flow<List<CampusEvent>> =
        eventsFlow.map { list -> list.filter { it.buildingId == buildingId } }
            .distinctUntilChanged()

    // Mengambil daftar bangunan dari repository
    override suspend fun getBuildings(): List<Building> =
        InMemoryCampusRepository.getBuildings()

    // Mengambil daftar lantai untuk building tertentu dari repository
    override suspend fun getFloors(buildingId: String): List<Int> =
        InMemoryCampusRepository.getFloors(buildingId)

    // Gabungan search
    override suspend fun search(q: String): List<SearchResult> {
        val base: List<SearchResult> =
            try { InMemoryCampusRepository.search(q) } catch (_: Throwable) { emptyList() }

        val term = q.trim().lowercase()
        val fromEvents: List<SearchResult> = eventsFlow.value
            .filter { e ->
                e.name.lowercase().contains(term) ||
                        e.heldBy.lowercase().contains(term) ||
                        e.room.lowercase().contains(term)
            }
            .map { e ->
                SearchResult.EventResult(
                    eventId = e.id,
                    display = "${e.name} • ${e.start.toLocalDate()} • ${e.buildingId}${e.room}"
                )
            }

        return base + fromEvents
    }
}

// Konversi model Firestore Event menjadi model UI CampusEvent
private fun Event.toCampusEvent(): CampusEvent {
    val zone = ZoneId.systemDefault()
    val day = date.toDate().toInstant().atZone(zone).toLocalDate()
    val start = LocalDateTime.of(day, java.time.LocalTime.of(startTimeMinutes / 60, startTimeMinutes % 60))
    val end   = LocalDateTime.of(day, java.time.LocalTime.of(endTimeMinutes / 60, endTimeMinutes % 60))

    return CampusEvent(
        id = id,
        name = name,
        heldBy = heldBy,
        buildingId = building,
        room = room,
        start = start,
        end = end,
        category = "Event"
    )
}
