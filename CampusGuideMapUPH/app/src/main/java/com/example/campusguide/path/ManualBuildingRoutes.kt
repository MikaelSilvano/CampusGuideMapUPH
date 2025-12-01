package com.example.campusguide.path

enum class CardinalDirection { NORTH, SOUTH, EAST, WEST }

data class BuildingPathStep(
    val id: String,      // e.g. "bb", "eagle", "basketball"
    val label: String    // e.g. "Building B", "Eagle", "Basketball Court"
)

private data class RouteKey(
    val from: String,             // "B", "C", "D", "H", "F"
    val to: String,
    val direction: CardinalDirection? // null = direction-agnostic
)

object ManualBuildingRoutes {

    private val STEP_DEFS: Map<String, BuildingPathStep> = mapOf(
        "bb"         to BuildingPathStep("bb", "Building B"),
        "bc"         to BuildingPathStep("bc", "Building C"),
        "bd"         to BuildingPathStep("bd", "Building D"),
        "bf"         to BuildingPathStep("bf", "Building F"),
        "bh"         to BuildingPathStep("bh", "Building H"),
        "eagle"      to BuildingPathStep("eagle", "Eagle"),
        "cross"      to BuildingPathStep("cross", "Cross"),
        "basketball" to BuildingPathStep("basketball", "Basketball"),
        "flags"      to BuildingPathStep("flags", "Flags"),
        "gazebo"     to BuildingPathStep("gazebo", "Gazebo"),
        "statue"     to BuildingPathStep("statue", "Statue")
    )



    private val routes: Map<RouteKey, List<String>> = buildRoutes()

    private fun buildRoutes(): Map<RouteKey, List<String>> {
        val m = mutableMapOf<RouteKey, List<String>>()

        fun add(
            from: String,
            to: String,
            direction: CardinalDirection?,
            steps: List<String>
        ) {
            m[RouteKey(from, to, direction)] = steps
        }
        add("B", "D", CardinalDirection.NORTH, listOf("bb", "eagle", "cross", "bd"))
        add("B", "D", CardinalDirection.WEST,  listOf("gazebo", "cross", "bd"))
        add("B", "D", CardinalDirection.EAST,  listOf("statue", "eagle", "cross", "bd"))

        add("B", "C", CardinalDirection.NORTH, listOf("bb", "statue", "bc"))
        add("B", "C", CardinalDirection.WEST,  listOf("statue", "bc"))
        add("B", "C", CardinalDirection.EAST,  listOf("gazebo", "eagle", "bb", "statue", "bc"))

        add("B", "H", CardinalDirection.NORTH, listOf("bb", "statue", "bh"))
        add("B", "H", CardinalDirection.WEST,  listOf("statue", "bh"))
        add("B", "H", CardinalDirection.EAST,  listOf("gazebo", "eagle", "bb", "statue", "bh"))

        add("B", "F", CardinalDirection.NORTH, listOf("bb", "eagle", "basketball", "cross", "flags", "bd", "bf"))
        add("B", "F", CardinalDirection.WEST,  listOf("statue", "bb", "eagle", "basketball", "cross", "flags", "bd", "bf"))
        add("B", "F", CardinalDirection.EAST,  listOf("gazebo", "eagle", "cross", "flags", "bd", "bf"))

        add("C", "H", CardinalDirection.SOUTH, listOf("statue", "bh"))
        add("C", "H", CardinalDirection.WEST,  listOf("statue", "bh"))

        add("C", "D", CardinalDirection.SOUTH, listOf("eagle", "basketball", "cross", "bd"))
        add("C", "D", CardinalDirection.WEST,  listOf("eagle", "basketball", "cross", "bd"))

        add("C", "F", CardinalDirection.SOUTH, listOf("eagle", "basketball", "cross", "bd", "bf"))
        add("C", "F", CardinalDirection.WEST,  listOf("eagle", "basketball", "cross", "bd", "bf"))

        add("H", "D", null, listOf("statue", "eagle", "basketball", "cross", "bd"))
        add("H", "F", null, listOf("statue", "eagle", "basketball", "cross", "bd", "bf"))
        add("H", "C", null, listOf("bh", "statue"))

        add("D", "F", null, listOf("bd", "bf"))

        // --------------------------------------------------------------------
        //  DERIVED REVERSE ROUTES (as required in the prompt)
        // --------------------------------------------------------------------

        // C → B : reverse of B → C (for each direction)
        listOf(CardinalDirection.NORTH, CardinalDirection.WEST, CardinalDirection.EAST).forEach { dir ->
            m[RouteKey("B", "C", dir)]?.let { forward ->
                add("C", "B", dir, forward.asReversed())
            }
        }

        // H → C : reverse of C → H (direction doesn't matter when starting from H,
        // but we already defined H -> C (null) above, so this is optional)
        listOf(CardinalDirection.SOUTH, CardinalDirection.WEST).forEach { dir ->
            m[RouteKey("C", "H", dir)]?.let { forward ->
                // store as direction-agnostic for H
                add("H", "C", null, forward.asReversed())
            }
        }

        // H → B : reverse of B → H (direction-agnostic when starting from H)
        listOf(CardinalDirection.NORTH, CardinalDirection.WEST, CardinalDirection.EAST).forEach { dir ->
            m[RouteKey("B", "H", dir)]?.let { forward ->
                add("H", "B", null, forward.asReversed())
            }
        }

        // D → B : reverse of B → D (direction-agnostic when starting from D)
        listOf(CardinalDirection.NORTH, CardinalDirection.EAST, CardinalDirection.WEST).forEach { dir ->
            m[RouteKey("B", "D", dir)]?.let { forward ->
                add("D", "B", null, forward.asReversed())
            }
        }

        // D → C : reverse of C → D (direction-agnostic when starting from D)
        listOf(CardinalDirection.SOUTH, CardinalDirection.WEST).forEach { dir ->
            m[RouteKey("C", "D", dir)]?.let { forward ->
                add("D", "C", null, forward.asReversed())
            }
        }

        // D → H : reverse of H → D (also direction-agnostic)
        m[RouteKey("H", "D", null)]?.let { forward ->
            add("D", "H", null, forward.asReversed())
        }

        // F as start: reverse of everything that ends at F (direction-agnostic)
        val existing = m.toMap()
        existing.forEach { (key, steps) ->
            if (key.to == "F") {
                add("F", key.from, null, steps.asReversed())
            }
        }

        return m

    }

    /**
     * Resolve a manual route between two buildings, parameterized by the
     * starting entrance direction (when it matters).
     *
     * fromBuilding / toBuilding are "B", "C", "D", "H", "F".
     */
    fun resolve(
        fromBuilding: String,
        toBuilding: String,
        fromDirection: CardinalDirection?
    ): List<BuildingPathStep>? {
        val key1 = RouteKey(fromBuilding, toBuilding, fromDirection)
        val key2 = RouteKey(fromBuilding, toBuilding, null) // direction-agnostic fallback

        val ids = routes[key1] ?: routes[key2] ?: return null
        return ids.mapNotNull { STEP_DEFS[it] }
    }
}
