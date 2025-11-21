package com.example.campusguide.data

import java.time.*
import java.util.*
import kotlinx.coroutines.flow.flow

// Model bangunan: id, nama, jumlah lantai, dan catatan opsional
data class Building(val id: String, val name: String, val floors: Int, val notes: String = "")

// Model ruangan kampus: referensi building, lantai, kode ruangan, dan fakultas
data class CampusRoom(val buildingId: String, val floor: Int, val code: String, val faculty: String)

// Model event: jadwal lengkap dan kategori
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

// Hasil pencarian gabungan
sealed class SearchResult(val title: String, val subtitle: String) {
    class FacultyResult(val faculty: String, val buildingId: String)
        : SearchResult(faculty, "Faculty located in Building $buildingId")

    class RoomResult(val buildingId: String, val floor: Int, val room: String)
        : SearchResult(room, "Room at Building $buildingId • Floor $floor")

    class EventResult(
        val eventId: String,
        val buildingId: String,
        val display: String
    ) : SearchResult("Event", display)
}

// Filter daftar event
enum class EventFilter { All, Ongoing, Upcoming, Soon }

data class FrequentlyVisitedPlace(
    val buildingId: String,
    val floor: Int,
    val name: String,
    val imageKey: String
)

// Implementasi repository untuk data kampus.
object InMemoryCampusRepository: CampusRepository {
    val buildings = listOf(
        Building("B", "Building B", 6),
        Building("C", "Building C", 7, "2–4 Library; 3 Global Partnerships & International Office; 4 Rectorate; 5 Visionet; 6–7 Grand Chapel"),
        Building("D", "Building D", 5),
        Building("F", "Building F", 16),
        Building("H", "Building HOPE", 4),
    )

    // Pemetaan fakultas
    val facultyInBuilding = mapOf(
        "Faculty of Information Technology" to "B",
        "Faculty of Artificial Intelligence" to "H",
        "Faculty of Economics & Business" to "F",
        "Faculty of Music" to "B",
        "Faculty of Design" to "B",
        "Faculty of Law" to "D",
        "Faculty of Psychology" to "B",
        "Faculty of Hospitality & Tourism" to "D",
        "Faculty of Social Sciences & Political Sciences" to "F",
        "Faculty of Science & Technology" to "B",
        "Faculty of Education" to "B",
    )

    val facultyImageFiles = mapOf(
        "Faculty of Information Technology"                 to "fit",
        "Faculty of Artificial Intelligence"                to "fai",
        "Faculty of Economics & Business"                   to "feb",
        "Faculty of Music"                                  to "fm",
        "Faculty of Design"                                 to "fd",
        "Faculty of Law"                                    to "fh",
        "Faculty of Psychology"                             to "fpsi",
        "Faculty of Hospitality & Tourism"                  to "fhospar",
        "Faculty of Social Sciences & Political Sciences"   to "fisip",
        "Faculty of Science & Technology"                   to "fast",
        "Faculty of Education"                              to "fip"
    )

    val frequentlyVisitedByBuilding: Map<String, List<FrequentlyVisitedPlace>> = mapOf(
        "B" to listOf(
            FrequentlyVisitedPlace("B", 1, "B Lounge",                     "b_lounge"),
            FrequentlyVisitedPlace("B", 1, "Pelita Hall",                  "pelita_hall"),
            FrequentlyVisitedPlace("B", 1, "Pelita Shop",                  "pelita_shop"),
            FrequentlyVisitedPlace("B", 1, "Koperasi",                     "koperasi"),
            FrequentlyVisitedPlace("B", 1, "B114 - AIDA 3D Lab",           "b114_aida_3d_lab"),
            FrequentlyVisitedPlace("B", 1, "B115 - Clay Creation Lab",     "b115_clay_lab"),
            FrequentlyVisitedPlace("B", 1, "B116 - Lighting Studio",       "b116_lighting_studio"),
            FrequentlyVisitedPlace("B", 1, "B121 - Animation Lab",         "b121_animation_lab"),
            FrequentlyVisitedPlace("B", 1, "B127 - Computer Design Lab",   "b127_computer_design_lab"),
            FrequentlyVisitedPlace("B", 1, "Health Center",                "health_center"),
            FrequentlyVisitedPlace("B", 1, "Soil Mechanic Lab",            "soil_mechanic_lab"),

            FrequentlyVisitedPlace("B", 2, "Food Research & Innovation Lab","food_research_lab"),
            FrequentlyVisitedPlace("B", 2, "Faculty of Design Office",     "fd_office"),

            FrequentlyVisitedPlace("B", 3, "Student Life Office",          "student_life_office"),
            FrequentlyVisitedPlace("B", 3, "Leaders Chamber",              "leaders_chamber"),
            FrequentlyVisitedPlace("B", 3, "Faculty of Information Technology Office", "fit_office"),
            FrequentlyVisitedPlace("B", 3, "Faculty of Music Office",      "fm_office"),

            FrequentlyVisitedPlace("B", 4, "Grace Hall",                   "grace_hall"),
            FrequentlyVisitedPlace("B", 4, "Architecture Studio",          "architecture_studio"),

            FrequentlyVisitedPlace("B", 5, "Faculty of Psychology Office", "fpsi_office"),
            FrequentlyVisitedPlace("B", 5, "Center for Foundational Education Office", "cfe_office"),
            FrequentlyVisitedPlace("B", 5, "Faculty of Science & Technology Office", "fast_office"),
            FrequentlyVisitedPlace("B", 5, "Counseling Lab",               "counseling_lab"),

            FrequentlyVisitedPlace("B", 6, "Faculty of Education Office",  "fip_office")
        ),

        "C" to listOf(
            FrequentlyVisitedPlace("C", 1, "Food Junction",      "food_junction" +
                    ""),

            FrequentlyVisitedPlace("C", 2, "Johanes Oentoro Library",      "jo_library_l2"),
            FrequentlyVisitedPlace("C", 2, "MYC Multipurpose Room",        "myc_multipurpose"),

            FrequentlyVisitedPlace("C", 3, "Johanes Oentoro Library",      "jo_library_l3"),
            FrequentlyVisitedPlace("C", 3, "Global Partnerships & International Office",      "gpio"),

            FrequentlyVisitedPlace("C", 4, "Johanes Oentoro Library",      "jo_library_l4"),
            FrequentlyVisitedPlace("C", 4, "Admission Office",             "admission_office"),

            FrequentlyVisitedPlace("C", 5, "Rectorate Office",             "rectorate_office"),

            FrequentlyVisitedPlace("C", 6, "Grand Chapel",                 "grand_chapel_l6"),
            FrequentlyVisitedPlace("C", 7, "Grand Chapel",                 "grand_chapel_l7")
        ),

        "D" to listOf(
            FrequentlyVisitedPlace("D", 1, "Spice Xpert Lab",              "spice_xpert_lab"),
            FrequentlyVisitedPlace("D", 1, "Beenovative",                  "beenovative"),
            FrequentlyVisitedPlace("D", 1, "Salt & Light Lab",             "salt_light_lab"),
            FrequentlyVisitedPlace("D", 1, "Kitchen Lab",                  "kitchen_lab"),
            FrequentlyVisitedPlace("D", 1, "Bakery & Pastry Lab",          "bakery_pastry_lab"),
            FrequentlyVisitedPlace("D", 1, "Resto & Bar",                  "resto_bar"),

            FrequentlyVisitedPlace("D", 2, "Front Office Lab",             "front_office_lab"),

            FrequentlyVisitedPlace("D", 3, "Faculty of Hospitality & Tourism Office", "fhospar_office"),
            FrequentlyVisitedPlace("D", 3, "Moot Court",                   "moot_court"),

            FrequentlyVisitedPlace("D", 4, "Faculty of Law Office",        "law_office"),

            FrequentlyVisitedPlace("D", 5, "D501 Auditorium",              "d501_auditorium"),
            FrequentlyVisitedPlace("D", 5, "D502 Auditorium",              "d502_auditorium"),
            FrequentlyVisitedPlace("D", 5, "D503 Breakout Room",           "d503_breakout")
        ),

        "F" to listOf(
            FrequentlyVisitedPlace("F", 1, "Sparklabs Incubation",         "sparklabs_incubation"),
            FrequentlyVisitedPlace("F", 1, "F Lounge",         "f_lounge"),

            FrequentlyVisitedPlace("F", 2, "FIT Lab",                      "fit_lab"),
            FrequentlyVisitedPlace("F", 2, "Galeri Investasi",             "galeri_investasi"),

            FrequentlyVisitedPlace("F", 9, "TV Lab",                       "tv_lab"),

            FrequentlyVisitedPlace("F", 12,"Faculty of Economics & Business Office", "feb_office"),

            FrequentlyVisitedPlace("F", 15,"Faculty of Social and Political Science Office", "fisip_office"),

            FrequentlyVisitedPlace("F", 16,"General Affairs Office",       "general_affairs_office"),
            FrequentlyVisitedPlace("F", 16,"Human Resources Office",       "hr_office"),
        ),

        "H" to listOf(
            FrequentlyVisitedPlace("HOPE", 4,"HOPE4 Auditorium",       "hope_auditorium")
        )
    )

    // Daftar ruangan contoh per building/floor
    val rooms: List<CampusRoom> = buildList {
        buildings.forEach { b ->
            val allFloors = (1..b.floors).toList()
            val validFloors = when (b.id) {
                "F" -> allFloors.filter { it !in listOf(4, 13, 14) }
                else -> allFloors
            }

            validFloors.forEach { f ->
                repeat(6) { idx ->
                    val code = "${b.id}${f}0${idx + 1}"
                    val faculty = facultyInBuilding.entries.find { it.value == b.id }?.key ?: "Faculty of ..."
                    add(CampusRoom(b.id, f, code, faculty))
                }
            }
        }
    }

    private val now = LocalDateTime.now()

    // Event dummy
    val events: MutableList<CampusEvent> = mutableListOf(
        CampusEvent(name="Workshop Android", buildingId="F", room="F201", heldBy="Faculty of IT", start= now.plusHours(1), end= now.plusHours(3), category="Workshop"),
        CampusEvent(name="Open Rehearsal Orchestra", buildingId="C", room="C301", heldBy="Faculty of Music", start= now.plusDays(1).withHour(17), end= now.plusDays(1).withHour(19), category="Performance"),
        CampusEvent(name="Moot Court Practice", buildingId="D", room="D205", heldBy="Faculty of Law", start= now.minusHours(1), end= now.plusHours(1), category="Practice"),
        CampusEvent(name="Seminar Kewirausahaan", buildingId="B", room="B402", heldBy="FEB", start= now.plusDays(2).withHour(9), end= now.plusDays(2).withHour(12), category="Seminar")
    )

    override suspend fun getBuildings(): List<Building> = buildings

    override suspend fun getFloors(buildingId: String): List<Int> {
        val b = buildings.find { it.id == buildingId } ?: return emptyList()

        val allFloors = (1..b.floors).toList()

        return when (buildingId) {
            "F" -> allFloors.filter { it !in listOf(4, 13, 14) }
            else -> allFloors
        }
    }

    // Stream event khusus satu building
    override fun streamEventsForBuilding(buildingId: String) = flow {
        emit(events.filter { it.buildingId == buildingId })
    }

    // Stream seluruh event
    override fun streamAllEvents() = flow { emit(events.toList()) }

    // Searching
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
            SearchResult.EventResult(
                eventId    = e.id,
                buildingId = e.buildingId,
                display    = "${e.name} • ${buildingDisplayName(e.buildingId)} • ${e.room}"
            )
        }
        return fac + room + ev
    }

    override suspend fun setPublished(id: String, published: Boolean) {

    }

    override suspend fun createEvent(e: CampusEventInput): String {
        val zone = ZoneId.systemDefault()
        val day = Instant.ofEpochMilli(e.dateMillis).atZone(zone).toLocalDate()
        val start = LocalDateTime.of(day, LocalTime.of(e.startTimeMinutes / 60, e.startTimeMinutes % 60))
        val end   = LocalDateTime.of(day, LocalTime.of(e.endTimeMinutes / 60, e.endTimeMinutes % 60))

        val newId = UUID.randomUUID().toString()
        val added = CampusEvent(
            id = newId,
            name = e.name,
            buildingId = e.buildingId,
            room = e.room,
            heldBy = e.heldBy,
            start = start,
            end = end,
            category = "Event"
        )
        events.add(added)
        return newId
    }

    override suspend fun updateEvent(id: String, patch: Map<String, Any?>) {
        val idx = events.indexOfFirst { it.id == id }
        if (idx < 0) return
        val cur = events[idx]

        fun <T> Map<String, Any?>.opt(key: String, cast: Class<T>): T? =
            this[key]?.let { if (cast.isInstance(it)) cast.cast(it) else null }

        val name = patch.opt("name", String::class.java) ?: cur.name
        val buildingId = patch.opt("building", String::class.java) ?: cur.buildingId
        val room = patch.opt("room", String::class.java) ?: cur.room
        val heldBy = patch.opt("heldBy", String::class.java) ?: cur.heldBy

        val dateMillis = (patch["date"] as? Number)?.toLong()
        val sMin = (patch["startTimeMinutes"] as? Number)?.toInt()
        val eMin = (patch["endTimeMinutes"] as? Number)?.toInt()

        val zone = ZoneId.systemDefault()
        val day = when {
            dateMillis != null -> Instant.ofEpochMilli(dateMillis).atZone(zone).toLocalDate()
            else -> cur.start.toLocalDate()
        }
        val newStart = when {
            sMin != null -> LocalDateTime.of(day, LocalTime.of(sMin / 60, sMin % 60))
            dateMillis != null -> LocalDateTime.of(day, cur.start.toLocalTime())
            else -> cur.start
        }
        val newEnd = when {
            eMin != null -> LocalDateTime.of(day, LocalTime.of(eMin / 60, eMin % 60))
            dateMillis != null -> LocalDateTime.of(day, cur.end.toLocalTime())
            else -> cur.end
        }

        events[idx] = cur.copy(
            name = name,
            buildingId = buildingId,
            room = room,
            heldBy = heldBy,
            start = newStart,
            end = newEnd
        )
    }

    override suspend fun deleteEvent(id: String) {
        events.removeAll { it.id == id }
    }
}

fun buildingDisplayName(id: String): String {
    return InMemoryCampusRepository.buildings
        .find { it.id == id }
        ?.name
        ?: "Building $id"
}
