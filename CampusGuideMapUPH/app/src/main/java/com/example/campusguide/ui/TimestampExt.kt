package com.example.campusguide.ui

import com.google.firebase.Timestamp
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

fun Timestamp.toLocalDate(): LocalDate =
    Instant.ofEpochMilli(this.toDate().time)
        .atZone(ZoneId.systemDefault())
        .toLocalDate()