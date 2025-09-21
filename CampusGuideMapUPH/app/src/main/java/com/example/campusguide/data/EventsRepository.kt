package com.example.campusguide.data

import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

data class Event(
    val id: String = "",
    val name: String = "",
    val heldBy: String = "",
    val buildingId: String = "",
    val room: String = "",
    val start: java.time.LocalDateTime = java.time.LocalDateTime.now(),
    val end: java.time.LocalDateTime = java.time.LocalDateTime.now(),
    val posterPath: String = ""
)

private fun Event.toMap(): Map<String, Any?> = mapOf(
    "name" to name,
    "heldBy" to heldBy,
    "buildingId" to buildingId,
    "room" to room,
    "start" to Timestamp(java.util.Date.from(start.atZone(java.time.ZoneId.systemDefault()).toInstant())),
    "end" to Timestamp(java.util.Date.from(end.atZone(java.time.ZoneId.systemDefault()).toInstant())),
    "posterPath" to posterPath,
    "updatedAt" to Timestamp.now()
)

private fun mapToEvent(id: String, m: Map<String, Any?>): Event {
    fun tsToLdt(v: Any?): java.time.LocalDateTime {
        val ts = v as? com.google.firebase.Timestamp ?: return java.time.LocalDateTime.now()
        return java.time.Instant.ofEpochSecond(ts.seconds, ts.nanoseconds.toLong())
            .atZone(java.time.ZoneId.systemDefault()).toLocalDateTime()
    }
    return Event(
        id = id,
        name = m["name"] as? String ?: "",
        heldBy = m["heldBy"] as? String ?: "",
        buildingId = m["buildingId"] as? String ?: "",
        room = m["room"] as? String ?: "",
        start = tsToLdt(m["start"]),
        end = tsToLdt(m["end"]),
        posterPath = m["posterPath"] as? String ?: ""
    )
}

object EventsRepository {
    private val db = FirebaseFirestore.getInstance()
    private val col get() = db.collection("events")

    fun streamAll() = callbackFlow<List<Event>> {
        val reg: ListenerRegistration = col.addSnapshotListener { snap, e ->
            if (e != null) { trySend(emptyList()); return@addSnapshotListener }
            val list = snap?.documents.orEmpty().map { d -> mapToEvent(d.id, d.data ?: emptyMap()) }
                .sortedBy { it.start }
            trySend(list)
        }
        awaitClose { reg.remove() }
    }

    fun streamByBuilding(buildingId: String) = callbackFlow<List<Event>> {
        val q = col.whereEqualTo("buildingId", buildingId)
        val reg = q.addSnapshotListener { snap, e ->
            if (e != null) { trySend(emptyList()); return@addSnapshotListener }
            val list = snap?.documents.orEmpty().map { d -> mapToEvent(d.id, d.data ?: emptyMap()) }
                .sortedBy { it.start }
            trySend(list)
        }
        awaitClose { reg.remove() }
    }

    suspend fun add(e: Event): String =
        col.add(e.toMap()).await().id

    suspend fun update(id: String, partial: Map<String, Any?>) {
        col.document(id).set(partial + ("updatedAt" to com.google.firebase.Timestamp.now()), SetOptions.merge()).await()
    }

    suspend fun delete(id: String) {
        col.document(id).delete().await()
    }
}
