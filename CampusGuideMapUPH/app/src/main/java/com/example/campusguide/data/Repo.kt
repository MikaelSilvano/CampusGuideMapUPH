package com.example.campusguide.data

import android.content.Context
import kotlinx.coroutines.flow.Flow

data class CampusEventInput(
    val name: String,
    val buildingId: String,
    val floor: Int,
    val room: String,
    val heldBy: String,
    val dateMillis: Long,
    val startTimeMinutes: Int,
    val endTimeMinutes: Int,
    val posterUrl: String = "",
    val published: Boolean = false
)

interface CampusRepository {
    suspend fun getBuildings(): List<Building>
    suspend fun getFloors(buildingId: String): List<Int>
    fun streamEventsForBuilding(buildingId: String): Flow<List<CampusEvent>>
    fun streamAllEvents(): Flow<List<CampusEvent>>
    suspend fun search(query: String): List<SearchResult>
    suspend fun setPublished(id: String, published: Boolean)
    suspend fun createEvent(e: CampusEventInput): String
    suspend fun updateEvent(id: String, patch: Map<String, Any?>)
    suspend fun deleteEvent(id: String)
    suspend fun refreshNow() {}
}

object CampusRepoProvider {
    @Volatile private var instance: CampusRepository? = null

    fun provide(@Suppress("UNUSED_PARAMETER") ctx: Context): CampusRepository {
        return instance ?: synchronized(this) {
            instance ?: FunctionsCampusRepository().also { instance = it }
        }
    }
}
