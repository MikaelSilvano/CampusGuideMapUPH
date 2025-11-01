package com.example.campusguide.data

import android.net.Uri
import com.example.campusguide.data.remote.EventDto
import com.example.campusguide.data.remote.FunctionsApi
import com.example.campusguide.data.remote.RetrofitProvider
import com.example.campusguide.data.remote.getIdTokenOrThrow
import com.google.firebase.Timestamp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.Date
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.tasks.await

class EventsRepository(
    private val api: FunctionsApi = RetrofitProvider.api
) {
    private fun EventDto.toEvent(): Event =
        Event(
            id = id.orEmpty(),
            name = name,
            building = building,
            floor = floor ?: 1,
            room = room,
            heldBy = heldBy,
            date = Timestamp(Date(date)),
            startTimeMinutes = startTimeMinutes,
            endTimeMinutes = endTimeMinutes,
            posterUrl = posterUrl,
            published = published
        )

    private fun Event.toDto(): EventDto =
        EventDto(
            id = id,
            name = name,
            building = building,
            floor = floor,
            room = room,
            heldBy = heldBy,
            date = date.toDate().time,
            startTimeMinutes = startTimeMinutes,
            endTimeMinutes = endTimeMinutes,
            posterUrl = posterUrl.orEmpty(),
            published = published
        )

    suspend fun listAllAdmin(): List<Event> {
        val bearer = "Bearer ${getIdTokenOrThrow()}"
        return api.listAllEvents(bearer)
            .map { it.toEvent() }
            .sortedBy { it.date.toDate().time }
    }

    suspend fun listPastAdmin(): List<Event> = withContext(Dispatchers.IO) {
        val nowMs = System.currentTimeMillis()
        val bearer = "Bearer ${getIdTokenOrThrow()}"
        api.listAllEvents(bearer)
            .map { it.toEvent() }
            .filter { it.date.toDate().time < nowMs }
            .sortedByDescending { it.date.toDate().time }
    }

    private suspend fun uploadPosterAndGetUrl(eventId: String, uri: Uri): String {
        val ref = FirebaseStorage.getInstance()
            .reference.child("posters/$eventId.jpg")   // konsisten: posters/{id}.jpg
        ref.putFile(uri).await()
        return ref.downloadUrl.await().toString()
    }

    suspend fun create(e: Event, posterUri: Uri?): String = withContext(Dispatchers.IO) {
        val bearer = "Bearer ${getIdTokenOrThrow()}"

        val created = api.createEvent(bearer, e.toDto().copy(posterUrl = ""))
        val newId = created.id.orEmpty()

        if (posterUri != null && newId.isNotBlank()) {
            val url = uploadPosterAndGetUrl(newId, posterUri)
            api.updateEvent(bearer, newId, mapOf("posterUrl" to url))
        }

        newId
    }

    suspend fun update(id: String, e: Event, posterUri: Uri?) = withContext(Dispatchers.IO) {
        val bearer = "Bearer ${getIdTokenOrThrow()}"

        val patch = linkedMapOf<String, Any?>(
            "name"             to e.name.trim(),
            "building"         to e.building.trim(),
            "floor"            to e.floor,
            "room"             to e.room.trim(),
            "heldBy"           to e.heldBy.trim(),
            "date"             to e.date.toDate().time,
            "startTimeMinutes" to e.startTimeMinutes,
            "endTimeMinutes"   to e.endTimeMinutes,
            "published"        to e.published
        )

        if (posterUri != null) {
            val url = uploadPosterAndGetUrl(id, posterUri)
            patch["posterUrl"] = url
        } else if (!e.posterUrl.isNullOrBlank()) {
            patch["posterUrl"] = e.posterUrl
        }

        patch.entries.removeIf { it.value == null }

        try {
            api.updateEvent(bearer, id, patch)
        } catch (ex: retrofit2.HttpException) {
            val body = ex.response()?.errorBody()?.string()
            throw IllegalStateException("HTTP ${ex.code()} – ${body ?: ex.message()}")
        }
    }

    suspend fun setPublished(id: String, published: Boolean) = withContext(Dispatchers.IO) {
        val bearer = "Bearer ${getIdTokenOrThrow()}"
        api.updateEvent(bearer, id, mapOf("published" to published))
    }

    suspend fun delete(id: String) = withContext(Dispatchers.IO) {
        val bearer = "Bearer ${getIdTokenOrThrow()}"
        api.deleteEvent(bearer, id)
    }
}
