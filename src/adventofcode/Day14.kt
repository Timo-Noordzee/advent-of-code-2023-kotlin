package adventofcode

import kotlinx.benchmark.Benchmark
import kotlinx.benchmark.Setup
import kotlinx.benchmark.State
import kotlinx.benchmark.Warmup
import org.openjdk.jmh.annotations.Fork
import org.openjdk.jmh.annotations.Measurement
import org.openjdk.jmh.annotations.Scope
import java.util.concurrent.TimeUnit

private class RocksGrid(private val grid: Array<CharArray>) {

    private val m = grid.size
    private val n = grid.first().size

    var loadFactor: Int = 0
        private set

    fun tiltNorth(): Int {
        loadFactor = 0
        val nextEmpty = IntArray(n)
        for (i in grid.indices) {
            val line = grid[i]
            for (j in line.indices) {
                when (line[j]) {
                    'O' -> {
                        loadFactor += (n - nextEmpty[j])
                        grid[i][j] = '.'
                        grid[nextEmpty[j]][j] = 'O'
                        nextEmpty[j]++
                    }
                    '#' -> nextEmpty[j] = i + 1
                }
            }
        }
        return loadFactor
    }

    fun tiltSouth(): Int {
        loadFactor = 0
        val nextEmpty = IntArray(n) { m - 1 }
        for (i in grid.indices.reversed()) {
            val line = grid[i]
            for (j in line.indices) {
                when (line[j]) {
                    'O' -> {
                        loadFactor += (n - nextEmpty[j])
                        grid[i][j] = '.'
                        grid[nextEmpty[j]][j] = 'O'
                        nextEmpty[j]--
                    }
                    '#' -> nextEmpty[j] = i - 1
                }
            }
        }
        return loadFactor
    }

    fun tiltWest(): Int {
        for (i in grid.indices) {
            val line = grid[i]
            var nextEmpty = 0
            for (j in line.indices) {
                when (line[j]) {
                    'O' -> {
                        grid[i][j] = '.'
                        grid[i][nextEmpty] = 'O'
                        nextEmpty++
                    }
                    '#' -> nextEmpty = j + 1
                }
            }
        }
        return loadFactor
    }

    fun tiltEast(): Int {
        for (i in grid.indices) {
            val line = grid[i]
            var nextEmpty = n - 1
            for (j in line.indices.reversed()) {
                when (line[j]) {
                    'O' -> {
                        grid[i][j] = '.'
                        grid[i][nextEmpty] = 'O'
                        nextEmpty--
                    }
                    '#' -> nextEmpty = j - 1
                }
            }
        }
        return loadFactor
    }
}

@Suppress("DuplicatedCode")
@State(Scope.Benchmark)
@Fork(1)
@Warmup(iterations = 2, time = 2)
@Measurement(iterations = 10, time = 2, timeUnit = TimeUnit.SECONDS)
class Day14 {

    var input = emptyList<String>()

    @Setup
    fun setup() {
        input = readInput("Day14")
    }

    @Benchmark
    fun part1(): Int {
        val m = input.size
        val grid = Array(m) { i -> input[i].toCharArray() }
        val rocksGrid = RocksGrid(grid)
        return rocksGrid.tiltNorth()
    }

    @Benchmark
    fun part2(): Int {
        val m = input.size
        val grid = Array(m) { i -> input[i].toCharArray() }
        val rocksGrid = RocksGrid(grid)
        val patternsSeen = mutableSetOf<String>()
        val totalCycles = 1_000_000_000

        var cycle = 0
        var patternStartKey = ""
        var patternLength = 0

        while (cycle < totalCycles) {
            val totalLoad = IntArray(4)
            totalLoad[0] = rocksGrid.tiltNorth()
            totalLoad[1] = rocksGrid.tiltWest()
            totalLoad[2] = rocksGrid.tiltSouth()
            totalLoad[3] = rocksGrid.tiltEast()

            cycle++

            // Construct a pattern key based using the total load of the last 4 states (1 cycle)
            val patternKey = totalLoad.joinToString()

            // If this pattern key has not yet been seen previously, add it to the set of patterns seen
            if (patternKey !in patternsSeen) {
                patternLength = 0
                patternsSeen.add(patternKey)
            }

            if (patternLength == 0) {
                // The current key marks the start of a possible pattern (previous key was unknown).
                patternStartKey = patternKey
            } else if (patternKey == patternStartKey) {
                // Repeat the remaining number of cycles to reach the targeted total number of cycles
                repeat((totalCycles - cycle) % patternLength) {
                    rocksGrid.tiltNorth()
                    rocksGrid.tiltWest()
                    rocksGrid.tiltSouth()
                    rocksGrid.tiltEast()
                }

                return rocksGrid.loadFactor
            }

            patternLength++
        }

        return rocksGrid.loadFactor
    }
}

fun main() {
    val day14 = Day14()

    day14.input = readInput("Day14_test")
    checkAnswer(day14.part1(), 136)
    checkAnswer(day14.part2(), 64)

    day14.input = readInput("Day14")
    println("Part 1: ${day14.part1()}")
    println("Part 2: ${day14.part2()}")
}