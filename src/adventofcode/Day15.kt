package adventofcode

import kotlinx.benchmark.Benchmark
import kotlinx.benchmark.Setup
import kotlinx.benchmark.State
import kotlinx.benchmark.Warmup
import org.openjdk.jmh.annotations.Fork
import org.openjdk.jmh.annotations.Measurement
import org.openjdk.jmh.annotations.Scope
import java.util.concurrent.TimeUnit

private fun String.hash() = fold(0) { current, char ->
    ((current + char.code) * 17) and 0xFF
}

@Suppress("DuplicatedCode")
@State(Scope.Benchmark)
@Fork(1)
@Warmup(iterations = 2, time = 2)
@Measurement(iterations = 10, time = 2, timeUnit = TimeUnit.SECONDS)
class Day15 {

    var input = emptyList<String>()

    @Setup
    fun setup() {
        input = readInput("Day15")
    }

    @Benchmark
    fun part1() = input.first().split(',').sumOf { it.hash() }

    @Benchmark
    fun part2(): Int {
        val instructions = input.first()
        // Works because mutableMapOf uses a LinkedHashMap which preserves the order elements are inserted
        val boxes = Array(256) { mutableMapOf<String, Int>() }

        var index = 0
        var stepStart = 0 // Keep track of where the next instruction starts
        while (index < instructions.length) {
            when (instructions[index]) {
                '-' -> {
                    val label = instructions.substring(stepStart, index)
                    val box = label.hash()
                    boxes[box].remove(label)
                    index++ // skip the comma
                    stepStart = index + 1
                }
                '=' -> {
                    val label = instructions.substring(stepStart, index)
                    val focalLength = instructions[++index] - '0'
                    val box = label.hash()
                    boxes[box][label] = focalLength
                    index++ // skip the comma
                    stepStart = index + 1
                }
            }
            index++
        }

        // Calculate the focusing power of all the lenses
        return boxes.indices.sumOf { box ->
            boxes[box].values.withIndex().sumOf { (slot, focalPower) ->
                (box + 1) * (slot + 1) * focalPower
            }
        }
    }
}

fun main() {
    val day15 = Day15()

    day15.input = readInput("Day15_test")
    checkAnswer(day15.part1(), 1320)
    checkAnswer(day15.part2(), 145)

    day15.input = readInput("Day15")
    println("Part 1: ${day15.part1()}")
    println("Part 2: ${day15.part2()}")
}