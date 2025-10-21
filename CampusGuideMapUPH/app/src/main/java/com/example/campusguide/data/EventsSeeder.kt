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

    /**
     * Seeds Firestore with events from a raw JSON file.
     * It FIRST deletes all existing docs in "events", then inserts the new ones.
     *
     * Call with your new file:
     * EventsSeeder.seed(context, R.raw.events_after_2025_10_21_600)
     */
    suspend fun seed(context: Context, @RawRes resId: Int = R.raw.events_seed_600) {
        val db = FirebaseFirestore.getInstance()
        val eventsCol = db.collection("events")

        // 1) DELETE everything currently in "events"
        run {
            val snap = eventsCol.get().await()
            if (!snap.isEmpty) {
                val delBatches = mutableListOf<WriteBatch>()
                var batch = db.batch()
                var n = 0
                val MAX_OPS = 450 // keep < 500 per batch

                for (doc in snap.documents) {
                    batch.delete(doc.reference)
                    n++
                    if (n >= MAX_OPS) {
                        delBatches.add(batch)
                        batch = db.batch()
                        n = 0
                    }
                }
                if (n > 0) delBatches.add(batch)

                for (b in delBatches) b.commit().await()
            }
        }

        // 2) LOAD the new JSON
        val json = context.resources.openRawResource(resId)
            .bufferedReader().use(BufferedReader::readText)
        val arr = JSONArray(json)

        // 3) INSERT the new 600 docs
        val insBatches = mutableListOf<WriteBatch>()
        var batch = db.batch()
        var countInBatch = 0
        val MAX_OPS = 450 // Firestore hard limit is 500; stay safely under

        for (i in 0 until arr.length()) {
            val o = arr.getJSONObject(i)
            val id = o.getString("id")
            val docRef = eventsCol.document(id) // same IDs -> overwrite if they exist

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
                "posterUrl" to (if (o.isNull("posterUrl")) null else o.getString("posterUrl")),
                "published" to o.getBoolean("published"),
                "createdAt" to isoToTimestamp(o.getString("createdAt")),
                "updatedAt" to isoToTimestamp(o.getString("updatedAt"))
            )

            batch.set(docRef, data)
            countInBatch++

            if (countInBatch >= MAX_OPS) {
                insBatches.add(batch)
                batch = db.batch()
                countInBatch = 0
            }
        }
        if (countInBatch > 0) insBatches.add(batch)

        for (b in insBatches) b.commit().await()
    }
}
