package adventofcode

import adventofcode.model.Direction
import kotlinx.benchmark.Benchmark
import kotlinx.benchmark.Setup
import kotlinx.benchmark.State
import kotlinx.benchmark.Warmup
import org.openjdk.jmh.annotations.Fork
import org.openjdk.jmh.annotations.Measurement
import org.openjdk.jmh.annotations.Scope
import java.util.concurrent.TimeUnit

@Suppress("DuplicatedCode")
@State(Scope.Benchmark)
@Fork(1)
@Warmup(iterations = 2, time = 2)
@Measurement(iterations = 10, time = 2, timeUnit = TimeUnit.SECONDS)
class Day16 {

    var input = emptyList<String>()

    @Setup
    fun setup() {
        input = readInput("Day16")
    }

    @Benchmark
    fun part1() = energize(-1, 0, Direction.RIGHT.value)

    @Benchmark
    fun part2(): Int {
        val m = input.size
        val n = input.first().length
        return listOf(
            (0 until n).map { x -> intArrayOf(x, m, 0) }, // beams moving up
            (0 until m).map { y -> intArrayOf(-1, y, 1) }, // beams moving right
            (0 until n).map { x -> intArrayOf(x, -1, 2) }, // beams moving down
            (0 until m).map { y -> intArrayOf(n, y, 3) }, // beams moving left
        ).flatten().maxOf { (x, y, d) -> energize(x, y, d) }
    }

    fun energize(startX: Int, startY: Int, startDirection: Int): Int {
        val queue = ArrayDeque<IntArray>()
        queue.add(intArrayOf(startX, startY, startDirection))

        val m = input.size
        val n = input.first().length
        val visited = Array(m) { IntArray(n) }
        var energizedTiles = 0

        while (queue.isNotEmpty()) {
            var (x, y, d) = queue.removeLast()

            // Update either the x or y based on the direction
            // For both vertical directions the LSb is set to 0 while for both horizontal directions the LSb is set to 1
            // up    = 0b00
            // right = 0b01
            // down  = 0b10
            // left  = 0b11
            if (d and 1 == 0) {
                y = y - 1 + d
            } else {
                x = x + 2 - d
            }

            // Check if the position is within the grid
            if (x in 0 until m && y in 0 until n) {
                val visitedState = visited[y][x]

                // If this tile has not yet been visited from any direction, increment the number of energized tiles
                if (visitedState == 0) {
                    energizedTiles++
                }

                // Check if this tile has not yet been visited in the given direction
                if ((visitedState shr d) and 1 == 0) {
                    when (input[y][x]) {
                        '\\' -> queue.add(intArrayOf(x, y, d xor 3))
                        '/' -> queue.add(intArrayOf(x, y, d xor 1))
                        '|' -> {
                            if (d and 1 == 0) {
                                // If moving up or down continue
                                queue.add(intArrayOf(x, y, d))
                            } else {
                                // If moving left or right split the beam into 2 vertical beams
                                queue.add(intArrayOf(x, y, 0))
                                queue.add(intArrayOf(x, y, 2))
                            }
                        }
                        '-' -> {
                            if (d and 1 == 1) {
                                // If moving left or right
                                queue.add(intArrayOf(x, y, d))
                            } else {
                                // If moving up or down split the beam into 2 horizontal ones
                                queue.add(intArrayOf(x, y, 1))
                                queue.add(intArrayOf(x, y, 3))
                            }
                        }
                        else -> queue.add(intArrayOf(x, y, d))
                    }
                }

                visited[y][x] = visitedState or (1 shl d)
            }
        }

        return energizedTiles
    }
}

fun main() {
    val day16 = Day16()

    day16.input = readInput("Day16_test")
    checkAnswer(day16.part1(), 46)
    checkAnswer(day16.part2(), 51)

    day16.input = readInput("Day16")
    checkAnswerAndPrint(day16.part1(), 7860, "Part 1")
    checkAnswerAndPrint(day16.part2(), 8331, "Part 2")
}