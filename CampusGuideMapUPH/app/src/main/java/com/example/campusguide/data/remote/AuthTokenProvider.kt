package com.example.campusguide.data.remote

import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.tasks.await

suspend fun getIdTokenOrThrow(): String {
    val user = Firebase.auth.currentUser ?: error("User belum login")
    val token = user.getIdToken(true).await().token
    return token ?: error("Gagal ambil ID token")
}
