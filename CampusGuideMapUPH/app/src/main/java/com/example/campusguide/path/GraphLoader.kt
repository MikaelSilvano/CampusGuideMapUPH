package com.example.campusguide.path

import android.content.Context
import org.json.JSONObject

object GraphLoader {

    fun loadFromRaw(context: Context, rawResId: Int): Graph {
        val json = context.resources.openRawResource(rawResId).bufferedReader().use { it.readText() }
        val root = JSONObject(json)

        // ---- nodes ----
        val nodesObj = mutableMapOf<String, Node>()
        val nodes = root.getJSONArray("nodes")
        for (i in 0 until nodes.length()) {
            val o = nodes.getJSONObject(i)
            val id = o.getString("id")
            nodesObj[id] = Node(
                id = id,
                x = o.getDouble("x").toFloat(),
                y = o.getDouble("y").toFloat(),
                type = o.optString("type", null).takeIf { it?.isNotBlank() == true },
                buildingId = o.optString("buildingId", null).takeIf { it?.isNotBlank() == true }
            )
        }

        // ---- edges (build adjacency, add both directions for undirected) ----
        val adj = mutableMapOf<String, MutableList<Edge>>().apply {
            nodesObj.keys.forEach { put(it, mutableListOf()) }
        }

        val edges = root.getJSONArray("edges")
        for (i in 0 until edges.length()) {
            val e = edges.getJSONObject(i)
            val from = e.getString("from")
            val to = e.getString("to")
            val weight = e.optDouble("weight", 1.0).toFloat()
            val restricted = if (e.has("restricted")) e.getBoolean("restricted") else false

            // one direction
            adj.getValue(from).add(Edge(to = to, meters = weight, restricted = restricted))
            // mirror (undirected)
            adj.getValue(to).add(Edge(to = from, meters = weight, restricted = restricted))
        }

        return Graph(nodes = nodesObj, adj = adj)
    }
}
