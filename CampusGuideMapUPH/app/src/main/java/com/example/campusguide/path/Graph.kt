package com.example.campusguide.path

data class Node(
    val id: String,
    val x: Float,           // normalized 0..1
    val y: Float,
    // optional metadata (present in JSON used by GraphLoader)
    val type: String? = null,
    val buildingId: String? = null
)

data class Edge(
    val to: String,
    val meters: Float,
    val restricted: Boolean = false
)

data class Graph(
    val nodes: Map<String, Node>,
    val adj: Map<String, List<Edge>>
)
