package com.example.campusguide.data

import java.time.*
import java.util.*
import kotlinx.coroutines.flow.flow

data class Building(val id: String, val name: String, val floors: Int, val notes: String = "")
data class CampusRoom(val buildingId: String, val floor: Int, val code: String, val faculty: String)
data class CampusEvent(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val buildingId: String,
    val room: String,
    val heldBy: String,
    val start: LocalDateTime,
    val end: LocalDateTime,
    val category: String
)

sealed class SearchResult(val title: String, val subtitle: String) {
    class FacultyResult(val faculty: String, val buildingId: String): SearchResult(faculty, "Faculty located in Building $buildingId")
    class RoomResult(val buildingId: String, val floor: Int, val room: String): SearchResult(room, "Room at Building $buildingId • Floor $floor")
    class EventResult(val eventId: String, val display: String): SearchResult("Event", display)
}

enum class EventFilter { All, Ongoing, Upcoming, Soon }

object InMemoryCampusRepository: CampusRepository {
    val buildings = listOf(
        Building("B", "Building B", 6),
        Building("C", "Building C", 7, "2–4 Library; 3 Global Partnerships & International Office; 4 Rectorate; 5 Visionet; 6–7 Grand Chapel"),
        Building("D", "Building D", 5),
        Building("F", "Building F", 16),
        Building("G", "Building G (Dormitory)", 0)
    )

    val faculties = listOf(
        "Faculty of Music",
        "Faculty of Design",
        "Faculty of Law",
        "Faculty of Social Sciences & Political Sciences",
        "Faculty of Economics & Business",
        "Faculty of Science & Technology",
        "Faculty of Information Technology",
        "Faculty of Artificial Intelligence",
        "Faculty of Hospitality & Tourism",
        "Faculty of Medicine",
        "Faculty of Dentistry",
        "Faculty of Health Sciences",
        "Faculty of Psychology",
        "Faculty of Education",
        "Faculty of Nursing"
    )

    val facultyInBuilding = mapOf(
        "Faculty of Information Technology" to "B",
        "Faculty of Artificial Intelligence" to "B",
        "Faculty of Economics & Business" to "B",
        "Faculty of Music" to "C",
        "Faculty of Design" to "C",
        "Faculty of Law" to "D",
        "Faculty of Psychology" to "D",
        "Faculty of Hospitality & Tourism" to "F",
        "Faculty of Nursing" to "F"
    )

    val rooms: List<CampusRoom> = buildList {
        buildings.forEach { b ->
            (1..b.floors).forEach { f ->
                repeat(6) { idx ->
                    val code = "${b.id}${f}0${idx+1}"
                    val faculty = facultyInBuilding.entries.find { it.value == b.id }?.key ?: "Faculty of ..."
                    add(CampusRoom(b.id, f, code, faculty))
                }
            }
        }
        add(CampusRoom("G", 0, "G-Common-01", "Dormitory"))
        add(CampusRoom("G", 0, "G-Canteen-01", "Dormitory"))
    }

    private val now = LocalDateTime.now()
    val events: MutableList<CampusEvent> = mutableListOf(
        CampusEvent(name="Workshop Android", buildingId="F", room="F201", heldBy="Faculty of IT", start= now.plusHours(1), end= now.plusHours(3), category="Workshop"),
        CampusEvent(name="Open Rehearsal Orchestra", buildingId="C", room="C301", heldBy="Faculty of Music", start= now.plusDays(1).withHour(17), end= now.plusDays(1).withHour(19), category="Performance"),
        CampusEvent(name="Moot Court Practice", buildingId="D", room="D205", heldBy="Faculty of Law", start= now.minusHours(1), end= now.plusHours(1), category="Practice"),
        CampusEvent(name="Seminar Kewirausahaan", buildingId="B", room="B402", heldBy="FEB", start= now.plusDays(2).withHour(9), end= now.plusDays(2).withHour(12), category="Seminar")
    )

    override suspend fun getBuildings(): List<Building> = buildings
    override suspend fun getFloors(buildingId: String): List<Int> =
        buildings.find { it.id == buildingId }?.let { (1..it.floors).toList() } ?: emptyList()

    override fun streamEventsForBuilding(buildingId: String) = flow {
        emit(events.filter { it.buildingId == buildingId })
    }
    override fun streamAllEvents() = flow { emit(events.toList()) }

    override suspend fun search(query: String): List<SearchResult> {
        val q = query.trim()
        if (q.isEmpty()) return emptyList()
        val fac = facultyInBuilding.keys.filter { it.contains(q, true) }.map { f ->
            val b = facultyInBuilding[f] ?: "B"
            SearchResult.FacultyResult(f, b)
        }
        val room = rooms.filter { it.code.contains(q, true) }.map { r ->
            SearchResult.RoomResult(r.buildingId, r.floor, r.code)
        }
        val ev = events.filter { it.name.contains(q, true) }.map { e ->
            SearchResult.EventResult(e.id, "${e.name} • Building ${e.buildingId} • ${e.room}")
        }
        return fac + room + ev
    }

    fun eventsFiltered(filter: EventFilter): List<CampusEvent> {
        val now = LocalDateTime.now()
        return when(filter) {
            EventFilter.All -> events
            EventFilter.Ongoing -> events.filter { now.isAfter(it.start) && now.isBefore(it.end) }
            EventFilter.Upcoming -> events.filter { now.isBefore(it.start) }
            EventFilter.Soon -> events.filter { it.start.isAfter(now) && it.start.isBefore(now.plusDays(3)) }
        }
    }
}
