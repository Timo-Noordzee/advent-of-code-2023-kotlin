package adventofcode

import adventofcode.model.Point
import adventofcode.model.distanceTo
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
class Day11 {

    var input = emptyList<String>()

    @Setup
    fun setup() {
        input = readInput("Day11")
    }

    private fun solve(expansionFactor: Int): Long {
        val m = input.size
        val n = input[0].length

        val galaxies = mutableListOf<Point>()

        // Keep track of which rows/columns expand
        val doesRowExpand = BooleanArray(m) { true }
        val doesColumnExpand = BooleanArray(n) { true }

        for (i in input.indices) {
            val line = input[i]
            for (j in input.indices) {
                if (line[j] == '#') {
                    doesRowExpand[i] = false
                    doesColumnExpand[j] = false
                    galaxies.add(Point(j, i))
                }
            }
        }

        val numberOfExpandingRows = IntArray(m)
        for (i in 1 until m) {
            numberOfExpandingRows[i] = numberOfExpandingRows[i - 1]
            if (doesRowExpand[i]) {
                numberOfExpandingRows[i]++
            }
        }

        val numberOfExpandingColumn = IntArray(n)
        for (j in 1 until n) {
            numberOfExpandingColumn[j] = numberOfExpandingColumn[j - 1]
            if (doesColumnExpand[j]) {
                numberOfExpandingColumn[j]++
            }
        }

        val expandedGalaxies = galaxies.map { (x, y) ->
            Point(
                x = x + numberOfExpandingColumn[x] * expansionFactor,
                y = y + numberOfExpandingRows[y] * expansionFactor
            )
        }

        var totalDistance = 0L
        for (a in expandedGalaxies.indices) {
            for (b in a + 1 until expandedGalaxies.size) {
                totalDistance += expandedGalaxies[a] distanceTo expandedGalaxies[b]
            }
        }

        return totalDistance
    }

    @Benchmark
    fun part1() = solve(1)


    @Benchmark
    fun part2() = solve(999_999)
}

fun main() {
    val day11 = Day11()

    day11.input = readInput("Day11_test")
    checkAnswer(day11.part1(), 374L)
//    checkAnswer(day11.part2(), 8410L)

    day11.input = readInput("Day11")
    println("Part 1: ${day11.part1()}")
    println("Part 2: ${day11.part2()}")
}