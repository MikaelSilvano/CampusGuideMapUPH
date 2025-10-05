package com.example.campusguide.data

import android.content.Context
import kotlinx.coroutines.flow.Flow

// Repository kampus (mengambil sumber data untuk buildings, floors, event, dan pencarian)
interface CampusRepository {
    suspend fun getBuildings(): List<Building>
    suspend fun getFloors(buildingId: String): List<Int>
    fun streamEventsForBuilding(buildingId: String): Flow<List<CampusEvent>>
    fun streamAllEvents(): Flow<List<CampusEvent>>
    suspend fun search(query: String): List<SearchResult>
}

// Singleton untuk memilih implementasi repository
object CampusRepoProvider {
    @Volatile private var instance: CampusRepository? = null
    fun provide(@Suppress("UNUSED_PARAMETER") ctx: Context): CampusRepository {
        return instance ?: synchronized(this) {
            instance ?: FirestoreCampusRepository().also { instance = it }
        }
    }
}
