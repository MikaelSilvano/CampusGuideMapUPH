package com.example.campusguide.data

import android.content.ContentResolver
import android.net.Uri
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageMetadata
import kotlinx.coroutines.tasks.await
import android.webkit.MimeTypeMap
import kotlinx.coroutines.tasks.await

suspend fun uploadPosterAndGetUrl(eventId: String, uri: Uri): String {
    val storage = FirebaseStorage.getInstance().reference

    val ext = MimeTypeMap.getFileExtensionFromUrl(uri.toString())
        ?.takeIf { it.isNotBlank() } ?: "jpg"

    val path = "posters/$eventId/poster.$ext"
    val ref = storage.child(path)

    ref.putFile(uri).await()
    return ref.downloadUrl.await().toString()
}

private fun guessExtension(mime: String?): String =
    when (mime) {
        "image/png"  -> "png"
        "image/webp" -> "webp"
        "image/jpeg" -> "jpg"
        else         -> "jpg"
    }
