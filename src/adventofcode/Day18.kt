package adventofcode

import adventofcode.model.Point
import adventofcode.model.Polygon
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
class Day18 {

    var input = emptyList<String>()

    @Setup
    fun setup() {
        input = readInput("Day18")
    }

    @Benchmark
    fun part1(): Long {
        val vertices = input.runningFold(Point(0, 0)) { position, line ->
            val (direction, amount, _) = line.split(' ')
            when (direction) {
                "R" -> position.copy(x = position.x + amount.toInt())
                "L" -> position.copy(x = position.x - amount.toInt())
                "U" -> position.copy(y = position.y - amount.toInt())
                "D" -> position.copy(y = position.y + amount.toInt())
                else -> error("unknown direction")
            }
        }

        return Polygon(vertices).areaWithPerimeter()
    }

    @Benchmark
    fun part2(): Long {
        val vertices = input.runningFold(Point(0, 0)) { position, line ->
            val color = line.substringAfter('#').removeSuffix(")")
            val amount = color.substring(0 until 5).toInt(16)
            when (color.last()) {
                '0' -> position.copy(x = position.x + amount) // Right
                '1' -> position.copy(y = position.y + amount) // Down
                '2' -> position.copy(x = position.x - amount) // Left
                '3' -> position.copy(y = position.y - amount) // Up
                else -> error("unknown direction")
            }
        }

        return Polygon(vertices).areaWithPerimeter()
    }
}

fun main() {
    val day18 = Day18()

    day18.input = readInput("Day18_test")
    checkAnswer(day18.part1(), 62L)
    checkAnswer(day18.part2(), 952408144115L)

    day18.input = readInput("Day18")
    checkAnswerAndPrint(day18.part1(), 56678L, "Part 1")
    checkAnswerAndPrint(day18.part2(), 79088855654037L, "Part 2")
}