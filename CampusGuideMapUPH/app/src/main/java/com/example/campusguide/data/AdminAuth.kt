package com.example.campusguide.data

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import com.google.firebase.firestore.FirebaseFirestoreSettings

object AdminAuth {
    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()

    suspend fun signIn(email: String, password: String): Result<Unit> = runCatching {
        auth.signInWithEmailAndPassword(email, password).await()
        val uid = auth.currentUser?.uid ?: error("No UID")
        val isAdmin = db.collection("admins").document(uid).get().await().exists()
        if (!isAdmin) error("Not an admin")
    }

    fun signOut() { auth.signOut() }

    val currentUid: String? get() = auth.currentUser?.uid
}
