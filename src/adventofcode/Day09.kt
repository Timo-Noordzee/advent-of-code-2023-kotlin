package adventofcode

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
class Day09 {

    var input = emptyList<String>()

    @Setup
    fun setup() {
        input = readInput("Day09")
    }

    private fun predictNext(row: IntArray): Int {
        var totalDifference = 0
        var differenceLength = row.size - 1
        while (differenceLength >= 0) {
            var allZero = true
            for (i in 0 until differenceLength) {
                val difference = row[i + 1] - row[i]
                row[i] = difference
                allZero = allZero && difference == 0
            }

            if (allZero) {
                return totalDifference
            }

            differenceLength--
            totalDifference += row[differenceLength]
        }

        error("cannot predict next value")
    }

    @Benchmark
    fun part1(): Int {
        return input.sumOf { line ->
            val parts = line.split(' ')
            val history = IntArray(parts.size) { parts[it].toInt() }
            history.last() + predictNext(history)
        }
    }

    @Benchmark
    fun part2(): Int {
        return input.sumOf { line ->
            val parts = line.split(' ')
            val history = IntArray(parts.size) { parts[parts.lastIndex - it].toInt() }
            history.last() + predictNext(history)
        }
    }
}

fun main() {
    val day09 = Day09()

    day09.input = readInput("Day09_test")
    checkAnswer(day09.part1(), 114)
    checkAnswer(day09.part2(), 2)

    day09.input = readInput("Day09")
    println("Part 1: ${day09.part1()}")
    println("Part 2: ${day09.part2()}")
}