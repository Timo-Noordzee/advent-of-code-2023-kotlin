package adventofcode

import adventofcode.model.Direction
import adventofcode.model.Point
import adventofcode.model.neighbors
import adventofcode.model.plus
import kotlinx.benchmark.Benchmark
import kotlinx.benchmark.Setup
import kotlinx.benchmark.State
import kotlinx.benchmark.Warmup
import org.openjdk.jmh.annotations.Fork
import org.openjdk.jmh.annotations.Measurement
import org.openjdk.jmh.annotations.Scope
import java.util.*
import java.util.concurrent.TimeUnit
import kotlin.collections.ArrayDeque

private typealias Graph = Map<Point, MutableList<Pair<Point, Int>>>

private fun Char.slopeToDirection() = when (this) {
    '^' -> Direction.UP
    '>' -> Direction.RIGHT
    'v' -> Direction.DOWN
    '<' -> Direction.LEFT
    else -> error("unknown slope $this")
}

private fun Char.isSlope() = this == '^' || this == '>' || this == 'v' || this == '<'

@Suppress("DuplicatedCode")
@State(Scope.Benchmark)
@Fork(1)
@Warmup(iterations = 2, time = 2)
@Measurement(iterations = 10, time = 2, timeUnit = TimeUnit.SECONDS)
class Day23 {

    var input = emptyList<String>()

    @Setup
    fun setup() {
        input = readInput("Day23")
    }

    @Benchmark
    fun part1(): Int {
        val m = input.size
        val n = input.size
        val startQueue = ArrayDeque<Pair<Point, Char>>()
        val graph = mutableMapOf<Point, MutableList<Pair<Point, Int>>>()
        val visitedSlopes = mutableSetOf<Point>()
        val pathStart = Point(input.first().indexOfFirst { it == '.' }, 0)
        val pathEnd = Point(input.last().indexOfLast { it == '.' }, input.lastIndex)
        startQueue += (pathStart to 'v')

        while (startQueue.isNotEmpty()) {
            val (start, slope) = startQueue.removeFirst()
            val slopeDirection = slope.slopeToDirection()

            val queue = ArrayDeque<Triple<Point, Direction, Int>>()
            queue += Triple(start + slopeDirection, slopeDirection, 1)

            while (queue.isNotEmpty()) {
                val (current, directionOfTravel, step) = queue.removeFirst()
                if (current == pathEnd) {
                    graph.getOrPut(start) { mutableListOf() }.add(current to step)
                    continue
                }

                when (val tile = input[current.y][current.x]) {
                    '.' -> {
                        // Add all unvisited adjacent path tiles to the queue
                        Direction.ALL.forEach { direction ->
                            // When exploring a path it isn't possible to travel in the opposite direction (move back a tile)
                            if (direction != directionOfTravel.opposite) {
                                val neighbor = current + direction
                                if (neighbor.y in 0 until m && neighbor.x in 0 until n) {
                                    when (val neighborTile = input[neighbor.y][neighbor.x]) {
                                        '#' -> {} // ignore
                                        '.' -> queue += Triple(neighbor, direction, step + 1)
                                        else -> {
                                            // A slop cannot be entered from the opposite direction it faces
                                            // For example > cannot be entered when moving to the left (<)
                                            if (neighborTile.slopeToDirection().opposite != direction) {
                                                queue += Triple(neighbor, direction, step + 1)
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                    else -> {
                        // Add an edge between the starting slope and the current slope
                        graph.getOrPut(start) { mutableListOf() }.add(current to step)

                        // If the slope has not yet been visited, add it to the startQueue to explore new paths starting from this slope
                        if (visitedSlopes.add(current)) {
                            startQueue += (current to tile)
                        }
                    }
                }
            }
        }

        // Log graph using DOT syntax for visualization in Graphviz
//        graph.entries.forEach { (from, value) ->
//            value.forEach { (to, step) ->
//                println("\"$from\" -> \"$to\" [label=\"$step\"]")
//            }
//        }

        return longestPath(graph, pathStart, pathEnd)
    }

    @Benchmark
    fun part2(): Int {
        val m = input.size
        val n = input.size
        val startQueue = ArrayDeque<Point>()
        val visitedJunctions = mutableSetOf<Point>()
        val graph = mutableMapOf<Point, MutableList<Pair<Point, Int>>>()
        val pathStart = Point(input.first().indexOfFirst { it == '.' }, 0)
        val pathEnd = Point(input.last().indexOfLast { it == '.' }, input.lastIndex)
        startQueue += pathStart

        while (startQueue.isNotEmpty()) {
            val start = startQueue.removeFirst()
            val queue = ArrayDeque<Pair<Point, Int>>()
            val visited = mutableSetOf<Point>()
            queue += Pair(start, 0)
            visited += start

            while (queue.isNotEmpty()) {
                val (current, step) = queue.removeFirst()
                if (current == pathEnd) {
                    graph.getOrPut(start) { mutableListOf() }.add(current to step)
                    continue
                }

                val tile = input[current.y][current.x]

                // A tile is a junction if it is a path and adjacent to at least 2 slopes.
                // The starting tile (step=0) does not count as a new junction.
                val isJunction = tile == '.' && step > 0 && current.neighbors.filter { neighbor ->
                    neighbor.y in 0 until m && neighbor.x in 0 until n && input[neighbor.y][neighbor.x].isSlope()
                }.size > 1

                if (isJunction) {
                    // Add an edge between the starting point and the current junction
                    graph.getOrPut(start) { mutableListOf() }.add(current to step)

                    // If the current junction has not yet been visited, add it to the startQueue to explore paths from this junction
                    if (visitedJunctions.add(current)) {
                        startQueue += current
                    }
                } else {
                    // Add all unvisited adjacent path tiles to the queue
                    current.neighbors.forEach { neighbor ->
                        if (neighbor.y in 0 until m && neighbor.x in 0 until n) {
                            if (neighbor !in visited && input[neighbor.y][neighbor.x] != '#') {
                                queue += Pair(neighbor, step + 1)
                                visited += neighbor
                            }
                        }
                    }
                }
            }
        }

        // Log graph using DOT syntax for visualization in Graphviz
//        graph.entries.forEach { (from, value) ->
//            value.forEach { (to, step) ->
//                println("\"$from\" -> \"$to\" [label=\"$step\"]")
//            }
//        }

        return longestPath(graph, pathStart, pathEnd)
    }

    private fun longestPath(graph: Graph, start: Point, target: Point): Int {
        val visited = mutableSetOf<Point>()
        var longestPath = 0

        fun longestPath(current: Point, length: Int) {
            if (current == target) {
                longestPath = length.coerceAtLeast(longestPath)
                return
            }

            val adjacent = graph[current] ?: return
            adjacent.forEach { (next, weight) ->
                if (next !in visited) {
                    visited += next
                    longestPath(next, length + weight)
                    visited -= next
                }
            }
        }

        longestPath(start, 0)
        return longestPath
    }
}

fun main() {
    val day23 = Day23()

    day23.input = readInput("Day23_test")
    checkAnswer(day23.part1(), 94)
    checkAnswer(day23.part2(), 154)

    day23.input = readInput("Day23")
    checkAnswerAndPrint(day23.part1(), 2190, "Part 1")
    checkAnswerAndPrint(day23.part2(), 6258, "Part 2")
}