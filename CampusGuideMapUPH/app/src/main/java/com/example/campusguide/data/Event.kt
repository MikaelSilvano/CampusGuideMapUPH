package com.example.campusguide.data

import com.google.firebase.Timestamp

data class Event(
    val id: String = "",
    val name: String = "",
    val date: Timestamp = Timestamp.now(),
    val startTimeMinutes: Int = 9 * 60,
    val endTimeMinutes: Int = 10 * 60,
    val building: String = "",
    val floor: Int = 1,
    val room: String = "",
    val heldBy: String = "",
    val posterUrl: String? = null,
    val createdAt: Timestamp = Timestamp.now(),
    val updatedAt: Timestamp = Timestamp.now()
) {
    fun isOngoing(nowTs: Timestamp = Timestamp.now()): Boolean {
        val now = nowTs.toDate()
        val d = date.toDate()
        val sameDay = now.year == d.year && now.month == d.month && now.date == d.date
        if (!sameDay) return false
        val minutes = now.hours * 60 + now.minutes
        return minutes in startTimeMinutes..endTimeMinutes
    }
}
