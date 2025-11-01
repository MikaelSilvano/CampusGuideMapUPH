package com.example.campusguide.data.remote

import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.tasks.await

suspend fun getIdTokenOrThrow(): String {
    val user = Firebase.auth.currentUser ?: error("User has not login")
    val token = user.getIdToken(true).await().token
    return token ?: error("Failed to take token ID")
}
