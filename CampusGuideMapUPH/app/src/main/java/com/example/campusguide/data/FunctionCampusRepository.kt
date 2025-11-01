package com.example.campusguide.data

import com.example.campusguide.data.remote.RetrofitProvider
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.LocalTime
import com.example.campusguide.data.remote.getIdTokenOrThrow
import com.example.campusguide.data.remote.EventDto

class FunctionsCampusRepository(
    private val ioScope: CoroutineScope = CoroutineScope(Dispatchers.IO)
) : CampusRepository {

    private val api = RetrofitProvider.api

    private val _events = MutableStateFlow<List<CampusEvent>>(emptyList())
    val eventsFlow: StateFlow<List<CampusEvent>> = _events.asStateFlow()

    private var refresher: Job? = null

    init {
        startAutoRefresh(5_000L)
    }

    private fun startAutoRefresh(periodMs: Long) {
        if (refresher?.isActive == true) return
        refresher = ioScope.launch {
            while (true) {
                refreshOnce()
                delay(periodMs)
            }
        }
    }

    suspend fun refresh() = refreshOnce()

    private suspend fun refreshOnce() {
        runCatching {
            api.listEvents()
                .mapNotNull { it.toCampusEventOrNull() }
                .sortedWith(
                    compareBy<CampusEvent> { it.start.toEpochSecond(ZoneId.systemDefault().rules.getOffset(it.start)) }
                )
        }.onSuccess { _events.value = it }
            .onFailure { _events.value = emptyList() }
    }

    override fun streamAllEvents(): Flow<List<CampusEvent>> = eventsFlow

    override fun streamEventsForBuilding(buildingId: String): Flow<List<CampusEvent>> =
        eventsFlow.map { list -> list.filter { it.buildingId == buildingId } }
            .distinctUntilChanged()

    override suspend fun getBuildings(): List<Building> =
        InMemoryCampusRepository.getBuildings()

    override suspend fun getFloors(buildingId: String): List<Int> =
        InMemoryCampusRepository.getFloors(buildingId)

    override suspend fun search(q: String): List<SearchResult> {
        val base: List<SearchResult> =
            runCatching { InMemoryCampusRepository.search(q) }.getOrDefault(emptyList())

        val term = q.trim().lowercase()
        val fromEvents = eventsFlow.value
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

    override suspend fun setPublished(id: String, published: Boolean) {
        val bearer = "Bearer " + getIdTokenOrThrow()
        RetrofitProvider.api.updateEvent(bearer, id, mapOf("published" to published))
        refresh()
    }

    override suspend fun createEvent(e: CampusEventInput): String {
        val bearer = "Bearer " + getIdTokenOrThrow()
        val dto = EventDto(
            name = e.name,
            building = e.buildingId,
            floor = e.floor,
            room = e.room,
            heldBy = e.heldBy,
            date = e.dateMillis,
            startTimeMinutes = e.startTimeMinutes,
            endTimeMinutes = e.endTimeMinutes,
            posterUrl = e.posterUrl,
            published = e.published
        )
        val created = RetrofitProvider.api.createEvent(bearer, dto)
        refresh()
        return created.id.orEmpty()
    }

    override suspend fun updateEvent(id: String, patch: Map<String, Any?>) {
        val bearer = "Bearer " + getIdTokenOrThrow()
        RetrofitProvider.api.updateEvent(bearer, id, patch)
        refresh()
    }

    override suspend fun deleteEvent(id: String) {
        val bearer = "Bearer " + getIdTokenOrThrow()
        RetrofitProvider.api.deleteEvent(bearer, id)
        refresh()
    }

    override suspend fun refreshNow() = refresh()
}

private fun EventDto.toCampusEventOrNull(): CampusEvent? {
    if (name.isBlank()) return null

    val zone: ZoneId = ZoneId.systemDefault()
    val day: LocalDate = Instant.ofEpochMilli(date).atZone(zone).toLocalDate()
    val start: LocalDateTime = LocalDateTime.of(day, LocalTime.of(startTimeMinutes / 60, startTimeMinutes % 60))
    val end: LocalDateTime = LocalDateTime.of(day, LocalTime.of(endTimeMinutes / 60, endTimeMinutes % 60))

    return CampusEvent(
        id = id.orEmpty(),
        name = name,
        heldBy = heldBy,
        buildingId = building,
        room = room,
        start = start,
        end = end,
        category = "Event"
    )
}
