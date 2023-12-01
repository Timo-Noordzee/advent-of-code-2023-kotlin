package adventofcode

import kotlinx.benchmark.Benchmark
import kotlinx.benchmark.Setup
import kotlinx.benchmark.State
import kotlinx.benchmark.Warmup
import org.openjdk.jmh.annotations.Fork
import org.openjdk.jmh.annotations.Measurement
import org.openjdk.jmh.annotations.Scope
import java.util.concurrent.TimeUnit

@State(Scope.Benchmark)
@Fork(1)
@Warmup(iterations = 0)
@Measurement(iterations = 10, time = 2, timeUnit = TimeUnit.SECONDS)
class Day01 {

    var input: List<String> = emptyList()

    @Setup
    fun setup() {
        input = readInput("Day01")
    }

    @Benchmark
    fun part1() = input.sumOf { line ->
        val firstDigit = line.first { it.isDigit() } - '0'
        val lastDigit = line.last { it.isDigit() } - '0'
        firstDigit * 10 + lastDigit
    }

    @Benchmark
    fun part2(): Int {
        // Keep track of all possible words grouped by the first character
        val dictionary = listOf(
            "one" to 1,
            "two" to 2,
            "three" to 3,
            "four" to 4,
            "five" to 5,
            "six" to 6,
            "seven" to 7,
            "eight" to 8,
            "nine" to 9
        ).groupBy { (key, _) -> key.first() }

        return input.sumOf { line ->
            val firstDigit = getDigit(line, line.indices, dictionary)
            val lastDigit = getDigit(line, line.indices.reversed(), dictionary)
            firstDigit * 10 + lastDigit
        }
    }

    private fun getDigit(
        line: String,
        range: IntProgression,
        dictionary: Map<Char, List<Pair<String, Int>>>
    ): Int {
        for (i in range) {
            // If the character at index i is a digit, return its numerical value
            if (line[i].isDigit()) return line[i] - '0'

            // Get a list of possible words starting with the character at index i
            // When there are no words starting with the character at index i, continue with the next character
            val possibleWords = dictionary[line[i]] ?: continue

            // Check for each possible word if the substring starting at index i equals the word
            possibleWords.forEach { (word, value) ->
                if (line.startsWith(word, i)) {
                    return value
                }
            }
        }
        error("no digit found")
    }
}

fun main() {
    val day01 = Day01()

    day01.input = readInput("Day01_test_1")
    check(day01.part1() == 142)

    day01.input = readInput("Day01_test_2")
    check(day01.part2() == 281)

    day01.input = readInput("Day01")
    println("Part 1: ${day01.part1()}")
    println("Part 2: ${day01.part2()}")
}
