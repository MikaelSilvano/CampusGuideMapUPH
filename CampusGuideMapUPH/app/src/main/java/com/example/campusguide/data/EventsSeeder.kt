package com.example.campusguide.data

import android.content.Context
import androidx.annotation.RawRes
import com.example.campusguide.R
import com.example.campusguide.data.remote.EventDto
import com.example.campusguide.data.remote.RetrofitProvider
import com.example.campusguide.data.remote.getIdTokenOrThrow
import kotlinx.coroutines.delay
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import java.time.Instant
import java.time.OffsetDateTime
import java.time.format.DateTimeFormatter

object EventsSeeder {

    private fun isoToMillis(iso: String): Long {
        return try {
            OffsetDateTime.parse(iso, DateTimeFormatter.ISO_OFFSET_DATE_TIME)
                .toInstant().toEpochMilli()
        } catch (_: Throwable) {
            Instant.parse(iso).toEpochMilli()
        }
    }

    suspend fun seedFromRaw(
        context: Context,
        @RawRes resId: Int = R.raw.events_seed_600,
        delayMsPerItem: Long = 120
    ) = withContext(Dispatchers.IO) {
        val api = RetrofitProvider.api
        val bearer = "Bearer ${getIdTokenOrThrow()}"

        val json = context.resources.openRawResource(resId)
            .bufferedReader().use { it.readText() }
        val arr = JSONArray(json)

        for (i in 0 until arr.length()) {
            val o = arr.getJSONObject(i)

            val dto = EventDto(
                name = o.getString("name"),
                building = o.getString("building"),
                floor = o.optInt("floor", 0),
                room = o.getString("room"),
                heldBy = o.getString("heldBy"),
                date = isoToMillis(o.getString("date")),
                startTimeMinutes = o.getInt("startTimeMinutes"),
                endTimeMinutes = o.getInt("endTimeMinutes"),
                posterUrl = if (o.isNull("posterUrl")) "" else o.getString("posterUrl"),
                published = o.getBoolean("published")
            )

            runCatching { api.createEvent(bearer, dto) }
                .onFailure { e -> throw IllegalStateException("Gagal insert index=$i: ${e.message}", e) }

            delay(delayMsPerItem)
        }
    }
}
