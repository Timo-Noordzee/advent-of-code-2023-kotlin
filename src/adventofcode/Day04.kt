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
class Day04 {

    var input = emptyList<String>()

    @Setup
    fun setup() {
        input = readInput("Day04")
    }

    @Benchmark
    fun part1(): Int {
        // Keep track of the last game ID a number was a winning number
        // winningNumbers[1] = n, means the number 1 was a winning number in game n
        val winningNumbers = IntArray(100) { -1 }

        // Keep track of where the numbers start
        val startIndex = input[0].indexOfFirst { it == ':' } + 2

        return input.withIndex().sumOf { (cardIndex, card) ->
            var index = startIndex
            while (index < card.length) {
                // Get the next winning number in the line
                val number = when (card[index]) {
                    // The next winning number consists of 1 digit
                    ' ' -> {
                        index++
                        card[index++] - '0'
                    }
                    // When reaching the '|' symbol, break from the loop
                    // The next numbers will no longer be winning numbers, but my own numbers
                    '|' -> {
                        index += 2
                        break
                    }
                    // The next winning number consists of 2 digits
                    else -> (card[index++] - '0') * 10 + (card[index++] - '0')
                }

                index++
                winningNumbers[number] = cardIndex
            }

            // Keep track of the number of winning numbers on the current card
            var numberOfWinningNumbers = 0

            while (index < card.length) {
                // Get the next number in the line
                val number = if (card[index] == ' ') {
                    // The number consists of just one digit
                    index++
                    card[index++] - '0'
                } else {
                    // The number consists of 2 digits
                    (card[index++] - '0') * 10 + (card[index++] - '0')
                }
                index++

                // If the number is a winning number on the current card, increment the number of winning numbers
                if (winningNumbers[number] == cardIndex) {
                    numberOfWinningNumbers++
                }
            }

            if (numberOfWinningNumbers > 0) 1 shl (numberOfWinningNumbers - 1) else 0
        }
    }

    @Benchmark
    fun part2(): Int {
        val n = input.size

        // Keep track of the number of copies of each card
        val numberOfCopies = IntArray(n) { 1 }

        // Keep track of the last game ID a number was a winning number
        // winningNumbers[1] = n, means the number 1 was a winning number in game n
        val winningNumbers = IntArray(100) { -1 }

        // Keep track of where the numbers start
        val startIndex = input[0].indexOfFirst { it == ':' } + 2

        return input.withIndex().sumOf { (cardIndex, card) ->
            var index = startIndex
            while (index < card.length) {
                // Get the next winning number in the line
                val number = when (card[index]) {
                    // The next winning number consists of 1 digit
                    ' ' -> {
                        index++
                        card[index++] - '0'
                    }
                    // When reaching the '|' symbol, break from the loop
                    // The next numbers will no longer be winning numbers, but my own numbers
                    '|' -> {
                        index += 2
                        break
                    }
                    // The next winning number consists of 2 digits
                    else -> (card[index++] - '0') * 10 + (card[index++] - '0')
                }

                index++
                winningNumbers[number] = cardIndex
            }

            // Keep track of the number of winning numbers on the current card
            var numberOfWinningNumbers = 0

            while (index < card.length) {
                // Get the next number in the line
                val number = if (card[index] == ' ') {
                    // The number consists of just one digit
                    index++
                    card[index++] - '0'
                } else {
                    // The number consists of 2 digits
                    (card[index++] - '0') * 10 + (card[index++] - '0')
                }
                index++

                // If the number is a winning number on the current card, increment the number of winning numbers
                if (winningNumbers[number] == cardIndex) {
                    numberOfWinningNumbers++
                }
            }

            val copies = numberOfCopies[cardIndex]

            // If this card has at lease one winning number, increment the amount of copies for the next <numberOfWinningNumbers> games
            if (numberOfWinningNumbers != 0) {
                for (i in (cardIndex + 1)..(cardIndex + numberOfWinningNumbers).coerceAtMost(n)) {
                    numberOfCopies[i] += copies
                }
            }

            copies
        }
    }
}

fun main() {
    val day04 = Day04()

    day04.input = readInput("Day04_test")
    check(day04.part1() == 13)
    check(day04.part2() == 30)

    day04.input = readInput("Day04")
    println("Part 1: ${day04.part1()}")
    println("Part 2: ${day04.part2()}")
}