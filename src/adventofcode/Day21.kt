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
import java.util.*
import java.util.concurrent.TimeUnit
import kotlin.collections.ArrayDeque

@Suppress("DuplicatedCode")
@State(Scope.Benchmark)
@Fork(1)
@Warmup(iterations = 2, time = 2)
@Measurement(iterations = 10, time = 2, timeUnit = TimeUnit.SECONDS)
class Day21 {

    var input = emptyList<String>()

    @Setup
    fun setup() {
        input = readInput("Day20")
    }

    private fun traverse(input: List<String>): Map<Point, Int> {
        val m = input.size
        val n = input[0].length
        val queue = ArrayDeque<Pair<Point, Int>>()
        val visited = mutableMapOf<Point, Int>()

        outer@ for (i in input.indices) {
            val line = input[i]
            for (j in line.indices) {
                if (line[j] == 'S') {
                    queue.add(Point(j, i) to 0)
                    break@outer
                }
            }
        }

        while (queue.isNotEmpty()) {
            val (position, distance) = queue.removeFirst()
            if (position in visited) {
                continue
            }

            visited[position] = distance
            Direction.ALL.forEach { direction ->
                val neighbor = position + direction
                if (neighbor.y in 0 until m && neighbor.x in 0 until n) {
                    if (neighbor !in visited && input[neighbor.y][neighbor.x] != '#') {
                        queue.add(neighbor to distance + 1)
                    }
                }
            }
        }

        return visited
    }

    @Benchmark
    fun part1() = traverse(input).values.count { it <= 64 && it % 2 == 0 }

    @Benchmark
    fun part2(): Long {
        val visited = traverse(input)

        // This part is based on https://github.com/villuna/aoc23/wiki/A-Geometric-solution-to-advent-of-code-2023,-day-21
        val halfSize = input.size / 2
        val repetitions = (26501365L - halfSize) / input.size
        val even = repetitions * repetitions
        val odd = (repetitions + 1) * (repetitions + 1)
        val evenCorners = visited.values.count { it % 2 == 0 && it > halfSize }.toLong()
        val oddCorners = visited.values.count { it % 2 == 1 && it > halfSize }.toLong()

        return odd * visited.values.count { it % 2 == 1 } + even * visited.values.count { it % 2 == 0 } - ((repetitions + 1) * oddCorners) + (repetitions * evenCorners)
    }
}

fun main() {
    val day21 = Day21()

    day21.input = readInput("Day21")
    checkAnswerAndPrint(day21.part1(), 3646, "Part 1")
    checkAnswerAndPrint(day21.part2(), 606188414811259L, "Part 2")
}