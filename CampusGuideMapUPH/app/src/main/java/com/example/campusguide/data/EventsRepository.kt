package com.example.campusguide.data

import android.net.Uri
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.toObject
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.tasks.await
import com.google.firebase.storage.StorageException

// Repository untuk operasi CRUD event + upload poster di Firebase Storage
class EventsRepository(
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance(),
    private val storage: FirebaseStorage = FirebaseStorage.getInstance()
) {
    private val col = db.collection("events")

    suspend fun listAllFuture(): List<Event> {
        val cal = java.util.Calendar.getInstance().apply {
            set(java.util.Calendar.HOUR_OF_DAY, 0)
            set(java.util.Calendar.MINUTE, 0)
            set(java.util.Calendar.SECOND, 0)
            set(java.util.Calendar.MILLISECOND, 0)
        }
        val todayStart = com.google.firebase.Timestamp(cal.time)

        val snap = col
            .whereGreaterThanOrEqualTo("date", todayStart)
            .orderBy("date")
            .get()
            .await()

        return snap.documents.mapNotNull { d ->
            d.toObject<Event>()?.copy(id = d.id)
        }
    }

    // Membuat event baru
    suspend fun create(event: Event, posterUri: Uri?): String {
        val id = col.document().id
        val posterUrl = posterUri?.let { uploadPoster(id, it) }
        val now = Timestamp.now()
        val payload = event.copy(
            posterUrl = posterUrl,
            createdAt = now,
            updatedAt = now
        )
        col.document(id).set(payload).await()
        return id
    }

    // Memperbarui event
    suspend fun update(id: String, event: Event, newPosterUri: Uri?) {
        val posterUrl = when {
            newPosterUri != null -> uploadPoster(id, newPosterUri)
            else -> event.posterUrl
        }
        col.document(id).update(
            mapOf(
                "name" to event.name,
                "date" to event.date,
                "startTimeMinutes" to event.startTimeMinutes,
                "endTimeMinutes" to event.endTimeMinutes,
                "building" to event.building,
                "floor" to event.floor,
                "room" to event.room,
                "heldBy" to event.heldBy,
                "posterUrl" to posterUrl,
                "published" to event.published,
                "updatedAt" to Timestamp.now()
            )
        ).await()
    }

    // Mengubah status publish/unpublish event
    suspend fun setPublished(id: String, published: Boolean) {
        col.document(id).update(
            mapOf(
                "published" to published,
                "updatedAt" to Timestamp.now()
            )
        ).await()
    }

    // Menghapus event
    suspend fun delete(id: String) {
        col.document(id).delete().await()

        try {
            storage.reference.child("posters/$id.jpg").delete().await()
        } catch (e: Exception) {
            val se = e as? StorageException
            if (se?.errorCode != StorageException.ERROR_OBJECT_NOT_FOUND) {
                throw e
            }
        }
    }

    // Upload poster ke path
    private suspend fun uploadPoster(id: String, uri: Uri): String {
        val ref = storage.reference.child("posters/$id.jpg")
        ref.putFile(uri).await()
        return ref.downloadUrl.await().toString()
    }

    // Past events
    suspend fun listPast(): List<Event> {
        val cal = java.util.Calendar.getInstance().apply {
            set(java.util.Calendar.HOUR_OF_DAY, 0)
            set(java.util.Calendar.MINUTE, 0)
            set(java.util.Calendar.SECOND, 0)
            set(java.util.Calendar.MILLISECOND, 0)
        }
        val todayStart = com.google.firebase.Timestamp(cal.time)

        val snap = col
            .whereLessThan("date", todayStart)
            .orderBy("date")
            .get()
            .await()

        return snap.documents.mapNotNull { d ->
            d.toObject(Event::class.java)?.copy(id = d.id)
        }
    }
}
