package adventofcode

import kotlinx.benchmark.Benchmark
import kotlinx.benchmark.Setup
import kotlinx.benchmark.State
import kotlinx.benchmark.Warmup
import org.openjdk.jmh.annotations.Fork
import org.openjdk.jmh.annotations.Measurement
import org.openjdk.jmh.annotations.Scope
import java.util.concurrent.TimeUnit

private fun Char.isSymbol() = this != '.' && !isDigit()

private fun List<String>.get(i: Int, j: Int): Char {
    if (i in indices) {
        if (j in this[i].indices) {
            return this[i][j]
        }
    }
    return '.'
}

@State(Scope.Benchmark)
@Fork(1)
@Warmup(iterations = 2, time = 2)
@Measurement(iterations = 10, time = 2, timeUnit = TimeUnit.SECONDS)
class Day03 {

    var input = emptyList<String>()

    @Setup
    fun setup() {
        input = readInput("Day03")
    }

    private inline fun processPartNumbers(action: (partNumber: Int, row: Int, range: IntRange) -> Unit) {
        var line: String
        for (i in input.indices) {
            line = input[i]
            var j = 0
            while (j < line.length) {
                if (line[j].isDigit()) {
                    val start = j
                    var number = line[j] - '0'
                    while (j + 1 < line.length && line[j + 1].isDigit()) {
                        j++
                        number = number * 10 + (line[j] - '0')
                    }

                    action(number, i, start..j)
                }
                j++
            }
        }
    }

    @Benchmark
    fun part1(): Int {
        var answer = 0

        processPartNumbers { partNumber, row, range ->
            row@ for (i in (row - 1)..(row + 1)) {
                column@ for (j in range.expand(1)) {
                    if (input.get(i, j).isSymbol()) {
                        answer += partNumber
                        break@row
                    }
                }
            }
        }

        return answer
    }

    @Benchmark
    fun part2(): Int {
        val possibleGears = mutableMapOf<Pair<Int, Int>, MutableList<Int>>()
        processPartNumbers { partNumber, row, range ->
            row@ for (i in (row - 1)..(row + 1)) {
                column@ for (j in range.expand(1)) {
                    if (input.get(i, j) == '*') {
                        possibleGears.getOrPut(Pair(i, j)) { mutableListOf() }.add(partNumber)
                        break@row
                    }
                }
            }
        }

        return possibleGears.values.sumOf { numbers ->
            if (numbers.size == 2) numbers[0] * numbers[1] else 0
        }
    }
}

fun main() {
    val day03 = Day03()

    day03.input = readInput("Day03_test")
    check(day03.part1() == 4361)
    check(day03.part2() == 467835)

    day03.input = readInput("Day03")
    println("Part 1: ${day03.part1()}")
    println("Part 2: ${day03.part2()}")
}