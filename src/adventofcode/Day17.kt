package adventofcode

import adventofcode.model.Direction
import kotlinx.benchmark.Benchmark
import kotlinx.benchmark.Setup
import kotlinx.benchmark.State
import kotlinx.benchmark.Warmup
import org.openjdk.jmh.annotations.Fork
import org.openjdk.jmh.annotations.Measurement
import org.openjdk.jmh.annotations.Scope
import java.util.*
import java.util.concurrent.TimeUnit

@Suppress("DuplicatedCode")
@State(Scope.Benchmark)
@Fork(1)
@Warmup(iterations = 2, time = 2)
@Measurement(iterations = 10, time = 2, timeUnit = TimeUnit.SECONDS)
class Day17 {

    var input = emptyList<String>()

    @Setup
    fun setup() {
        input = readInput("Day17")
    }

    @Benchmark
    fun part1() = solve(minBlocks = 1, maxBlocks = 3)

    @Benchmark
    fun part2() = solve(minBlocks = 4, maxBlocks = 10)

    /**
     * Calculate the path with the least amount of heat loss using the crucible parameters
     *
     * @param minBlocks the minimum number of blocks the crucible needs to be moved in the same direction before it can turn
     * @param maxBlocks the maximum number of consecutive blocks the crucible can be moved in the same direction
     */
    fun solve(minBlocks: Int, maxBlocks: Int): Int {
        val m = input.size
        val n = input.first().length
        val iTarget = m - 1
        val jTarget = n - 1

        // queue = [<i>,<j>,<heathLoss>,<steps>,<direction>]
        val queue = PriorityQueue<IntArray>(compareBy { it[2] })
        val visited = Array(m) { Array(n) { BooleanArray((maxBlocks + 1) * 4) } }
        queue.add(intArrayOf(0, 0, 0, 0, 1))

        while (queue.isNotEmpty()) {
            val (i, j, heathLoss, steps, d) = queue.remove()
            val direction = Direction(d)

            if (i == iTarget && j == jTarget && steps >= minBlocks) {
                return heathLoss
            }

            if (steps < minBlocks) {
                val iNext = i + direction.verticalDelta
                val jNext = j + direction.horizontalDelta

                if (iNext in 0 until m && jNext in 0 until n) {
                    // The state contains the direction of travel together with the number of blocks moved in the same direction consecutively
                    val state = direction.value * 10 + steps + 1

                    // If the city has not yet been visited in the current state, visit it
                    if (!visited[iNext][jNext][state]) {
                        visited[iNext][jNext][state] = true

                        queue.add(
                            intArrayOf(
                                iNext, // The next i position
                                jNext, // The next j position
                                heathLoss + (input[iNext][jNext] - '0'), // The total heath loss
                                steps + 1, // The number of blocks moving in the same direction
                                direction.value // The direction of travel
                            )
                        )
                    }
                }
            } else {
                val oppositeDirection = direction.opposite

                direction@ for (nextDirection in Direction.ALL) {
                    // The crucible can't move in the opposite direction (reverse)
                    if (nextDirection == oppositeDirection) {
                        continue@direction
                    }

                    // Keep track of how many blocks the crucible moved in the same direction consecutively
                    var consecutiveBlocks = steps
                    if (nextDirection == direction) {
                        consecutiveBlocks++
                        // The crucible can't move more than `maxBlocks` in the same direction consecutively
                        if (consecutiveBlocks > maxBlocks) {
                            continue@direction
                        }
                    } else {
                        // When the crucible changed direction reset the number of consecutive blocks counter
                        consecutiveBlocks = 1
                    }

                    // Calculate the next i and j coordinate based on the direction of travel
                    val iNext = i + nextDirection.verticalDelta
                    val jNext = j + nextDirection.horizontalDelta

                    // Check if the next coordinate is within the map
                    if (iNext in 0 until m && jNext in 0 until n) {
                        // The state contains the direction of travel together with the number of blocks moved in the same direction consecutively
                        val state = nextDirection.value * maxBlocks + consecutiveBlocks

                        // If the city has not yet been visited in the current state, visit it
                        if (!visited[iNext][jNext][state]) {
                            visited[iNext][jNext][state] = true

                            queue.add(
                                intArrayOf(
                                    iNext,
                                    jNext,
                                    heathLoss + (input[iNext][jNext] - '0'),
                                    consecutiveBlocks,
                                    nextDirection.value
                                )
                            )
                        }
                    }
                }
            }
        }

        error("no path found to the machine parts factory")
    }
}

fun main() {
    val day17 = Day17()

    day17.input = readInput("Day17_test")
    checkAnswer(day17.part1(), 102)
    checkAnswer(day17.part2(), 94)
    day17.input = readInput("Day17_test_2")
    checkAnswer(day17.part2(), 71)

    day17.input = readInput("Day17")
    checkAnswerAndPrint(day17.part1(), 967, "Part 1")
    checkAnswerAndPrint(day17.part2(), 1101, "Part 2")
}