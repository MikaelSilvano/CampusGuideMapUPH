package com.example.campusguide.data

import android.content.Context
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey

// Penyimpanan kredensial admin yang terenkripsi
class CredentialStore private constructor(context: Context) {
    private val prefsName = "secure_admin_creds"

    // Master key untuk enkripsi SharedPreferences
    private val masterKey = MasterKey.Builder(context)
        .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
        .build()

    // SharedPreferences terenkripsi untuk menyimpan kredensial
    private val prefs = EncryptedSharedPreferences.create(
        context,
        prefsName,
        masterKey,
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )

    // Menyimpan email & password terenkripsi dan menandai status tersimpan
    fun save(email: String, password: String) {
        prefs.edit().putString(KEY_EMAIL, email)
            .putString(KEY_PASSWORD, password)
            .putBoolean(KEY_SAVED, true)
            .apply()
    }

    // Menghapus semua data
    fun clear() {
        prefs.edit().clear().apply()
    }

    // Mengecek apakah kredensial pernah disimpan
    fun isSaved(): Boolean = prefs.getBoolean(KEY_SAVED, false)

    fun email(): String = prefs.getString(KEY_EMAIL, "") ?: ""
    fun password(): String = prefs.getString(KEY_PASSWORD, "") ?: ""

    companion object {
        private const val KEY_EMAIL = "email"
        private const val KEY_PASSWORD = "password"
        private const val KEY_SAVED = "saved"

        @Volatile private var INSTANCE: CredentialStore? = null

        // Mendapatkan instance singleton CredentialStore
        fun get(context: Context): CredentialStore =
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: CredentialStore(context.applicationContext).also { INSTANCE = it }
            }
    }
}
