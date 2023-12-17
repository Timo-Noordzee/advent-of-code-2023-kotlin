package adventofcode

import adventofcode.model.Direction
import adventofcode.model.Point
import adventofcode.model.plus
import kotlinx.benchmark.Benchmark
import kotlinx.benchmark.Setup
import kotlinx.benchmark.State
import kotlinx.benchmark.Warmup
import org.openjdk.jmh.annotations.Fork
import org.openjdk.jmh.annotations.Measurement
import org.openjdk.jmh.annotations.Scope
import java.util.concurrent.TimeUnit
import kotlin.math.absoluteValue

private operator fun List<String>.get(point: Point): Char {
    val (j, i) = point
    if (i in indices) {
        if (j in this[i].indices) {
            return this[i][j]
        }
    }
    return '.'
}

private class Pipe(val symbol: Char, private val outputDirection: Array<Direction?>) {

    fun canEnter(directionOfTravel: Direction): Boolean {
        return outputDirection[directionOfTravel.value] != null
    }

    /**
     * Enter a pipe from the given direction of travel and return the new direction of travel when exiting the pipe
     *
     * For example, when entering a 'L' pipe while travelling down, the new direction of travel will be to the right
     *
     * @return the new direction of travel
     */
    fun enter(directionOfTravel: Direction): Direction {
        return outputDirection[directionOfTravel.value] ?: error(
            "pipe $symbol cannot be entered while travelling $directionOfTravel"
        )
    }
}

@Suppress("DuplicatedCode")
@State(Scope.Benchmark)
@Fork(1)
@Warmup(iterations = 2, time = 2)
@Measurement(iterations = 10, time = 2, timeUnit = TimeUnit.SECONDS)
class Day10 {

    private val pipes = listOf(
        Pipe('|', arrayOf(Direction.UP, null, Direction.DOWN, null)),
        Pipe('-', arrayOf(null, Direction.RIGHT, null, Direction.LEFT)),
        Pipe('L', arrayOf(null, null, Direction.RIGHT, Direction.UP)),
        Pipe('J', arrayOf(null, Direction.UP, Direction.LEFT, null)),
        Pipe('7', arrayOf(Direction.LEFT, Direction.DOWN, null, null)),
        Pipe('F', arrayOf(Direction.RIGHT, null, null, Direction.DOWN))
    ).associateBy { it.symbol }

    var input = emptyList<String>()

    private fun findMainLoop(input: List<String>): List<Pair<Point, Int>> {
        // Find the coordinates of the starting position
        val start = input.indices.firstNotNullOfOrNull { i ->
            val line = input[i]
            val j = line.indexOfFirst { it == 'S' }
            if (j == -1) null else Point(j, i)
        } ?: error("cannot find starting position")

        // Find the first possible direction of travel from the starting position
        val startingDirection = Direction.ALL.firstOrNull { direction ->
            val neighbor = start + direction
            val tile = input[neighbor]
            val pipe = pipes[tile] ?: return@firstOrNull false
            pipe.canEnter(direction)
        } ?: error("there is no pipe connected to starting position $start")

        var steps = 1
        var directionOfTravel = startingDirection
        var position = start + directionOfTravel
        var tile = input[position]

        val path = mutableListOf<Pair<Point, Int>>()
        path.add(start to 0)

        while (tile != 'S') {
            path.add(position to steps++)

            val pipe = pipes[tile] ?: error("tile $tile at position $position isn't a pipe")

            directionOfTravel = pipe.enter(directionOfTravel)
            position += directionOfTravel
            tile = input[position]
        }

        return path
    }

    @Setup
    fun setup() {
        input = readInput("Day10")
    }

    @Benchmark
    fun part1() = findMainLoop(input).size / 2

    @Benchmark
    fun part2(): Int {
        val path = findMainLoop(input).associate { (point, step) -> point to step }
        val pathPerRow = path.entries.groupBy { (point, _) -> point.y }
        return pathPerRow.values.sumOf { points ->
            points.asSequence()
                // Sort the positions on this row by their x coordinate starting with the left-most position
                .sortedBy { (point, _) -> point.x }
                // Take each position of which the tile below is part of the main loop and the previous step in the loop
                .filter { (point, step) ->
                    val pointBelow = point.copy(y = point.y + 1)
                    val difference = (step - path.getOrDefault(pointBelow, step)).absoluteValue
                    difference == 1
                }
                // Discard the step as only the position is relevant from now on
                .map { (point, _) -> point }
                // Take chunks of 2. All tiles within these 2 positions are part of the inner loop (event-odd-rule)
                .chunked(2)
                // Count the number of tiles between the start and end position which aren't part of the main loop
                .sumOf { (start, end) ->
                    (start.x + 1 until end.x).count { x ->
                        val position = Point(x, start.y)
                        position !in path
                    }
                }
        }
    }
}

fun main() {
    val day10 = Day10()

    day10.input = readInput("Day10_test_1")
    checkAnswer(day10.part1(), 8)
    day10.input = readInput("Day10_test_2")
    checkAnswer(day10.part2(), 10)

    day10.input = readInput("Day10")
    println("Part 1: ${day10.part1()}")
    println("Part 2: ${day10.part2()}")
}