package adventofcode

import kotlinx.benchmark.Benchmark
import kotlinx.benchmark.Setup
import kotlinx.benchmark.State
import kotlinx.benchmark.Warmup
import org.openjdk.jmh.annotations.Fork
import org.openjdk.jmh.annotations.Measurement
import org.openjdk.jmh.annotations.Scope
import java.util.concurrent.TimeUnit

private class ConditionRecord(
    private val springs: String,
    private val groups: List<Int>
) {

    private val memo = mutableMapOf<Triple<Int, Int, Int>, Long>()

    private fun calculatePossibleArrangements(index: Int, groupIndex: Int, groupLength: Int): Long {
        val cacheKey = Triple(index, groupIndex, groupLength)

        return memo[cacheKey] ?: kotlin.run {
            // The required number of groups has been reached with remaining springs in the arrangement.
            // The remaining springs determine whenever the arrangement is valid. After reaching the required number
            // of contiguous groups of broken springs, none of the remaining springs may be broken for the arrangement to be valid.
            if (groupIndex == groups.size) {
                return if ((index until springs.length).none { i -> springs[i] == '#' }) 1L else 0L
            }

            // The end of the arrangement has been reached.
            // In certain conditions the last spring of a valid arrangement is a broken string '#'.
            // When the exact number of contiguous groups is met and the length of the last match matches the required length the arrangement is valid.
            if (index == springs.length) {
                // The required number of contiguous groups has been reached when reaching the end of the arrangement
                return if (groupIndex == groups.lastIndex && groupLength == groups.last()) 1L else 0L
            }

            val answer = when (springs[index]) {
                // In a valid arrangement an operational spring may only be possible when:
                // - The previous spring is also an operational spring (groupLength = 0)
                // - The group of contiguous springs matches the expected length
                '.' -> when (groupLength) {
                    0 -> calculatePossibleArrangements(index + 1, groupIndex, 0)
                    groups[groupIndex] -> calculatePossibleArrangements(index + 1, groupIndex + 1, 0)
                    else -> 0L
                }
                // In a valid arrangement a broken spring may only be possible when the length of the contiguous group
                // of broken springs is less than the expected length.
                '#' -> if (groupLength < groups[groupIndex]) {
                    calculatePossibleArrangements(index + 1, groupIndex, groupLength + 1)
                } else {
                    0L
                }
                // An unknown spring may either be an operational spring (.) or broken spring (#).
                '?' -> {
                    var answer = 0L

                    // An unknown spring may be replaced with an operational spring (.) when the previous spring is also
                    // an operational spring (groupLength = 0)
                    if (groupLength == 0) {
                        // Calculate the number of possible arrangements when replacing the unknown spring with an operational spring (.)
                        answer += calculatePossibleArrangements(index + 1, groupIndex, 0)
                    }

                    // An unknown spring may be replaced with an operational spring (.) when the previous spring is the end
                    // of a contiguous group of damaged springs with the expected length.
                    if (groupLength == groups[groupIndex]) {
                        // Calculate the number of possible arrangements when replacing the unknown spring with an operational spring (.)
                        answer += calculatePossibleArrangements(index + 1, groupIndex + 1, 0)
                    }

                    // An unknown spring may be replaced with a broken string (#) when the length of the
                    // contiguous group of damaged springs is smaller than the expected length.
                    if (groupLength < groups[groupIndex]) {
                        // Calculate the number of possible arrangements when replacing the unknown spring with a broken spring (#)
                        answer += calculatePossibleArrangements(index + 1, groupIndex, groupLength + 1)
                    }
                    answer
                }
                else -> error("unknown value ${springs[index]} in record")
            }

            answer.also { memo[cacheKey] = it }
        }
    }

    fun calculatePossibleArrangements(): Long {
        return calculatePossibleArrangements(0, 0, 0).toLong()
    }
}

@Suppress("DuplicatedCode")
@State(Scope.Benchmark)
@Fork(1)
@Warmup(iterations = 2, time = 2)
@Measurement(iterations = 10, time = 2, timeUnit = TimeUnit.SECONDS)
class Day12 {

    var input = emptyList<String>()

    @Setup
    fun setup() {
        input = readInput("Day12")
    }

    @Benchmark
    fun part1() = solve(1)

    @Benchmark
    fun part2() = solve(5)

    private fun solve(copies: Int) = input.sumOf { line ->
        val (springs, groups) = line.split(' ')
        val conditionRecord = ConditionRecord(
            springs.repeat(copies, "?"),
            groups.repeat(copies, ",").split(',').map { it.toInt() }
        )
        conditionRecord.calculatePossibleArrangements()
    }
}

fun main() {
    val day12 = Day12()

    day12.input = readInput("Day12_test")
    checkAnswer(day12.part1(), 21L)
    checkAnswer(day12.part2(), 525152L)

    day12.input = readInput("Day12")
    println("Part 1: ${day12.part1()}")
    println("Part 2: ${day12.part2()}")
}