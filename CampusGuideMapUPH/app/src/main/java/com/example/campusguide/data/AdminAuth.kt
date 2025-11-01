package com.example.campusguide.data

import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.tasks.await

object AdminAuth {
    private val auth = Firebase.auth

    suspend fun signIn(email: String, password: String): Result<Unit> = runCatching {
        auth.signInWithEmailAndPassword(email, password).await()
        val isAdmin = isCurrentUserAdmin()
        if (!isAdmin) error("Not an admin")
    }
    suspend fun isCurrentUserAdmin(): Boolean {
        val user = auth.currentUser ?: return false
        val token = user.getIdToken(true).await()
        return token.claims["role"] == "admin"
    }
}
