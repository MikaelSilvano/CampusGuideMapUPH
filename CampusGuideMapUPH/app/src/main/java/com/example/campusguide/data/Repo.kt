package com.example.campusguide.data

import android.content.Context
import kotlinx.coroutines.flow.Flow

interface CampusRepository {
    suspend fun getBuildings(): List<Building>
    suspend fun getFloors(buildingId: String): List<Int>
    fun streamEventsForBuilding(buildingId: String): Flow<List<CampusEvent>>
    fun streamAllEvents(): Flow<List<CampusEvent>>
    suspend fun search(query: String): List<SearchResult>
}

object CampusRepoProvider {
    @Volatile private var instance: CampusRepository? = null

    fun provide(@Suppress("UNUSED_PARAMETER") ctx: android.content.Context): CampusRepository {
        return instance ?: synchronized(this) {
            instance ?: FirestoreCampusRepository().also { instance = it }
        }
    }
}
