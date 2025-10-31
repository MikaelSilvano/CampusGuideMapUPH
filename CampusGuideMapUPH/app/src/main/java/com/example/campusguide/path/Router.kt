package com.example.campusguide.path

import java.util.PriorityQueue

object Router {

    /**
     * Dijkstra single-source shortest path.
     * Returns list of node IDs from start to end (inclusive), or emptyList() if no route.
     */
    fun shortestPath(
        graph: Graph,
        startId: String,
        endId: String
    ): List<String> {
        if (startId == endId) return listOf(startId)
        if (!graph.nodes.containsKey(startId) || !graph.nodes.containsKey(endId)) return emptyList()

        val dist = mutableMapOf<String, Float>().withDefault { Float.POSITIVE_INFINITY }
        val prev = mutableMapOf<String, String?>()
        val visited = mutableSetOf<String>()

        val pq = PriorityQueue(compareBy<Pair<String, Float>> { it.second })
        dist[startId] = 0f
        pq.add(startId to 0f)

        while (pq.isNotEmpty()) {
            val (u, d) = pq.poll()
            if (visited.contains(u)) continue
            visited.add(u)
            if (u == endId) break

            val edges = graph.adj[u] ?: emptyList()
            for (e in edges) {
                val v = e.to
                val alt = d + e.meters
                if (alt < dist.getValue(v)) {
                    dist[v] = alt
                    prev[v] = u
                    pq.add(v to alt)
                }
            }
        }

        if (!prev.containsKey(endId) && startId != endId) return emptyList()

        // Reconstruct
        val path = ArrayDeque<String>()
        var cur: String? = endId
        while (cur != null) {
            path.addFirst(cur)
            cur = prev[cur]
        }
        // If start not at head, no valid route
        return if (path.firstOrNull() == startId) path.toList() else emptyList()
    }
}
