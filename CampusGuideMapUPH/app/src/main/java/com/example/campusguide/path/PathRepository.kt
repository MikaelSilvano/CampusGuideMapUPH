package com.example.campusguide.path

import android.content.Context
import androidx.annotation.RawRes
import org.json.JSONArray
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader

/**
 * Loads the campus graph from a raw JSON (res/raw/campus_graph.json).
 * Exposes the Graph and a helper to map node IDs to normalized offsets for drawing.
 */
class PathRepository private constructor(
    private val graph: Graph
) {
    fun graph(): Graph = graph

    /** Convert node IDs to normalized (x,y) pairs for canvas drawing */
    fun toNormalizedOffsets(nodeIds: List<String>): List<Pair<Float, Float>> {
        return nodeIds.mapNotNull { id ->
            graph.nodes[id]?.let { it.x to it.y }
        }
    }

    companion object {
        @Volatile private var INSTANCE: PathRepository? = null

        fun get(): PathRepository {
            return INSTANCE ?: error("PathRepository not initialized. Call init(context, rawRes) first.")
        }

        fun init(context: Context, @RawRes rawJson: Int) {
            if (INSTANCE != null) return
            val json = readRawAsString(context, rawJson)
            val graph = parseGraph(JSONObject(json))
            INSTANCE = PathRepository(graph)
        }

        private fun readRawAsString(context: Context, @RawRes rawId: Int): String {
            context.resources.openRawResource(rawId).use { input ->
                BufferedReader(InputStreamReader(input)).use { br ->
                    val sb = StringBuilder()
                    var line: String?
                    while (br.readLine().also { line = it } != null) {
                        sb.append(line).append('\n')
                    }
                    return sb.toString()
                }
            }
        }

        private fun parseGraph(root: JSONObject): Graph {
            // ---- nodes ----
            val nodesMap = mutableMapOf<String, Node>()
            val nodesJson = root.getJSONArray("nodes")
            for (i in 0 until nodesJson.length()) {
                val o = nodesJson.getJSONObject(i)
                val id = o.getString("id")
                nodesMap[id] = Node(
                    id = id,
                    x = o.getDouble("x").toFloat(),
                    y = o.getDouble("y").toFloat(),
                    type = o.optString("type", null).takeIf { it?.isNotBlank() == true },
                    buildingId = o.optString("buildingId", null).takeIf { it?.isNotBlank() == true }
                )
            }

            // ---- adjacency ----
            val adjTemp = mutableMapOf<String, MutableList<Edge>>().apply {
                nodesMap.keys.forEach { put(it, mutableListOf()) }
            }

            fun addEdge(from: String, to: String, w: Float, restricted: Boolean = false) {
                // only add if both nodes exist
                if (nodesMap.containsKey(from) && nodesMap.containsKey(to)) {
                    adjTemp.getOrPut(from) { mutableListOf() }
                        .add(Edge(to = to, meters = w, restricted = restricted))
                }
            }

            val edgesJson = root.getJSONArray("edges")
            for (i in 0 until edgesJson.length()) {
                val e = edgesJson.getJSONObject(i)
                val from = e.getString("from")
                val to = e.getString("to")
                val w = if (e.has("weight")) e.getDouble("weight").toFloat() else 1f
                val restricted = e.optBoolean("restricted", false)
                val undirected = e.optBoolean("undirected", true) // default: add both directions

                addEdge(from, to, w, restricted)
                if (undirected) addEdge(to, from, w, restricted)
            }

            // freeze lists
            val adj = adjTemp.mapValues { it.value.toList() }
            return Graph(nodes = nodesMap, adj = adj)
        }

    }
}
