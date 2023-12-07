package adventofcode

import kotlinx.benchmark.Benchmark
import kotlinx.benchmark.Setup
import kotlinx.benchmark.State
import kotlinx.benchmark.Warmup
import org.openjdk.jmh.annotations.Fork
import org.openjdk.jmh.annotations.Measurement
import org.openjdk.jmh.annotations.Scope
import java.util.*
import java.util.concurrent.TimeUnit

private class Hand(val type: Int, val bid: Int, val identity: Int) : Comparable<Hand> {

    override fun compareTo(other: Hand) = identity - other.identity

    companion object {
        const val TYPE_FIVE_OF_A_KIND = 6
        const val TYPE_FOUR_OF_A_KIND = 5
        const val TYPE_FULL_HOUSE = 4
        const val TYPE_THREE_OF_A_KIND = 3
        const val TYPE_TWO_PAIR = 2
        const val TYPE_ONE_PAIR = 1
        const val TYPE_HIGH_CARD = 0

        fun parse(line: String, withJokerCheck: Boolean = false): Hand {
            val (hand, bid) = line.split(' ')

            var value = 0
            var uniqueCardsMask = 0
            var oddCardsMask = 0
            var numberOfJokers = 5

            hand.forEach { card ->
                val cardValue = when (card) {
                    'A' -> 14
                    'K' -> 13
                    'Q' -> 12
                    'J' -> if (withJokerCheck) 0 else 11
                    'T' -> 10
                    else -> card - '0'
                }

                if (cardValue != 0) {
                    uniqueCardsMask = uniqueCardsMask or (1 shl cardValue)
                    oddCardsMask = oddCardsMask xor (1 shl cardValue)
                    numberOfJokers--
                }
                value = (value shl 4) + cardValue
            }

            // uniqueCardsMask.countOneBits() return the number of unique cards in the hand
            // oddCardsMask.countOneBits() returns the amount of cards which occur an odd amount of times
            val type = when (uniqueCardsMask.countOneBits()) {
                0 -> TYPE_FIVE_OF_A_KIND
                1 -> TYPE_FIVE_OF_A_KIND
                2 -> if (withJokerCheck && numberOfJokers != 0) {
                    if (oddCardsMask.countOneBits() != 0) {
                        TYPE_FOUR_OF_A_KIND
                    } else {
                        TYPE_FULL_HOUSE
                    }
                } else {
                    if (hand.groupBy { it }.any { it.value.size == 4 }) {
                        TYPE_FOUR_OF_A_KIND
                    } else {
                        TYPE_FULL_HOUSE
                    }
                }
                3 -> if (withJokerCheck && numberOfJokers != 0) {
                    TYPE_THREE_OF_A_KIND
                } else {
                    when (oddCardsMask.countOneBits()) {
                        3 -> TYPE_THREE_OF_A_KIND
                        1 -> TYPE_TWO_PAIR
                        else -> error("can't differentiate between THREE_OF_A_KIND and TWO_PAIR (${hand}, ${oddCardsMask.countOneBits()})")
                    }
                }
                4 -> TYPE_ONE_PAIR
                5 -> TYPE_HIGH_CARD
                else -> error("can't determine type of hand")
            }

            return Hand(type, bid.toInt(), value)
        }
    }
}

@Suppress("DuplicatedCode")
@State(Scope.Benchmark)
@Fork(1)
@Warmup(iterations = 2, time = 2)
@Measurement(iterations = 10, time = 2, timeUnit = TimeUnit.SECONDS)
class Day07 {

    var input = emptyList<String>()

    @Setup
    fun setup() {
        input = readInput("Day07")
    }

    private fun playGame(withJokerCheck: Boolean): Int {
        var totalWinnings = 0
        val types = Array(7) { mutableListOf<Hand>() }
        input.forEach { line ->
            val hand = Hand.parse(line, withJokerCheck)
            types[hand.type].add(hand)
        }

        var rank = 1
        types.forEach { hands ->
            hands.sort()
            hands.forEach { hand ->
                totalWinnings += rank * hand.bid
                rank++
            }
        }

        return totalWinnings
    }

    @Benchmark
    fun part1() = playGame(false)

    @Benchmark
    fun part2() = playGame(true)
}

fun main() {
    val day07 = Day07()

    day07.input = readInput("Day07_test")
    check(day07.part1() == 6440)
    check(day07.part2() == 5905)

    day07.input = readInput("Day07")
    println("Part 1: ${day07.part1()}")
    println("Part 2: ${day07.part2()}")
}