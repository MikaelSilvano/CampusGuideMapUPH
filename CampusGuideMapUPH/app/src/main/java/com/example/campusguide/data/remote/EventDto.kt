package com.example.campusguide.data.remote

data class EventDto(
    val id: String? = null,
    val name: String = "",
    val building: String = "",
    val floor: Int? = null,
    val room: String = "",
    val heldBy: String = "",
    val date: Long = 0L,
    val startTimeMinutes: Int = 0,
    val endTimeMinutes: Int = 0,
    val posterUrl: String = "",
    val published: Boolean = false,
    val createdBy: String? = null,
    val createdAt: Long? = null,
    val updatedAt: Long? = null
)
