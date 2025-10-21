package com.example.campusguide.data

import android.content.Context
import androidx.annotation.RawRes
import com.example.campusguide.R
import com.example.campusguide.utils.isoToTimestamp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.WriteBatch
import kotlinx.coroutines.tasks.await
import org.json.JSONArray
import java.io.BufferedReader

object EventsSeeder {

    // Run this ONCE (or guard with a flag) to avoid duplicate inserts.
    suspend fun seed(context: Context, @RawRes resId: Int = R.raw.events_seed_600) {
        val db = FirebaseFirestore.getInstance()

        // If you want to seed to Emulator instead of cloud, uncomment:
        // db.useEmulator("10.0.2.2", 8080)

        val json = context.resources.openRawResource(resId)
            .bufferedReader().use(BufferedReader::readText)
        val arr = JSONArray(json)

        val batches = mutableListOf<WriteBatch>()
        var batch = db.batch()
        var countInBatch = 0
        val maxPerBatch = 400 // stay < 500 doc ops limit

        for (i in 0 until arr.length()) {
            val o = arr.getJSONObject(i)
            val id = o.getString("id")
            val docRef = db.collection("events").document(id)

            val data = hashMapOf(
                "id" to id,
                "name" to o.getString("name"),
                "date" to isoToTimestamp(o.getString("date")),
                "startTimeMinutes" to o.getInt("startTimeMinutes"),
                "endTimeMinutes" to o.getInt("endTimeMinutes"),
                "building" to o.getString("building"),
                "floor" to o.getInt("floor"),
                "room" to o.getString("room"),
                "heldBy" to o.getString("heldBy"),
                "posterUrl" to if (o.isNull("posterUrl")) null else o.getString("posterUrl"),
                "published" to o.getBoolean("published"),
                "createdAt" to isoToTimestamp(o.getString("createdAt")),
                "updatedAt" to isoToTimestamp(o.getString("updatedAt"))
            )

            batch.set(docRef, data)
            countInBatch++

            if (countInBatch >= maxPerBatch) {
                batches.add(batch)
                batch = db.batch()
                countInBatch = 0
            }
        }
        if (countInBatch > 0) batches.add(batch)

        // commit sequentially (await needs coroutines-play-services; see note below)
        for (b in batches) b.commit().await()
    }
}
