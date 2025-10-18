package com.example.campusguide.utils

import com.google.firebase.Timestamp
import java.time.Instant

fun isoToTimestamp(iso: String): Timestamp {
    val inst = Instant.parse(iso) // e.g. "2025-03-14T00:00:00Z"
    return Timestamp(inst.epochSecond, inst.nano)
}
