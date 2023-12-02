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
class Day02 {

    var input = emptyList<String>()

    @Setup
    fun setup() {
        input = readInput("Day02")
    }

    @Benchmark
    fun part1() = input.sumOf { game ->
        val start = game.indexOfFirst { it == ':' }
        val gameId = game.substring(5, start).toInt()

        var index = start + 2
        while (index < game.length) {
            if (game[index].isDigit()) {
                var numberOfCubes = game[index++] - '0'
                while (game[index].isDigit()) {
                    numberOfCubes = numberOfCubes * 10 + (game[index] - '0')
                    index++
                }

                index++

                // Check if it is possible to play the game with at only 12 red, 13 green and 14 blue cubes
                // If it is not possible, add 0 to the sum because this game should not be included
                when (game[index]) {
                    'r' -> {
                        // Check if it is possible to play the game with 12 red cubes
                        if (numberOfCubes > 12) return@sumOf 0
                        index += 4 // Skip to the start of the next digit
                    }
                    'g' -> {
                        // Check if it is possible to play the game with 13 green cubes
                        if (numberOfCubes > 13) return@sumOf 0
                        index += 6 // Skip to the start of the next digit
                    }
                    'b' -> {
                        // Check if it is possible to play the game with 14 blue cubes
                        if (numberOfCubes > 14) return@sumOf 0
                        index += 5 // Skip to the start of the next digit
                    }
                }
            }
            index++
        }

        // It is possible to play the game with at only 12 red, 13 green and 14 blue cubes.
        // Add the game ID to the sum
        gameId
    }

    @Benchmark
    fun part2() = input.sumOf { game ->
        // Keep track of the fewest number of cubes required to play the same
        var red = 0
        var green = 0
        var blue = 0

        var index = game.indexOfFirst { it == ':' } + 2

        while (index < game.length) {
            if (game[index].isDigit()) {
                var numberOfCubes = game[index++] - '0'
                while (game[index].isDigit()) {
                    numberOfCubes = numberOfCubes * 10 + (game[index] - '0')
                    index++
                }

                index++

                // Keep track of the fewest number maximum number of cubes of a color revealed at the same time
                when (game[index]) {
                    'r' -> {
                        // Keep track of the fewest number red cubes required to play the game
                        red = numberOfCubes.coerceAtLeast(red)
                        index += 4 // Skip to the start of the next digit
                    }
                    'g' -> {
                        // Keep track of the fewest number green cubes required to play the game
                        green = numberOfCubes.coerceAtLeast(green)
                        index += 6 // Skip to the start of the next digit
                    }
                    'b' -> {
                        // Keep track of the fewest number blue cubes required to play the game
                        blue = numberOfCubes.coerceAtLeast(blue)
                        index += 5 // Skip to the start of the next digit
                    }
                }
            }
            index++
        }

        // Calculate the required power by multiplying the fewest number of cubes required of each color
        red * green * blue
    }
}

fun main() {
    val day02 = Day02()

    day02.input = readInput("Day02_test")
    check(day02.part1() == 8)
    check(day02.part2() == 2286)

    day02.input = readInput("Day02")
    println("Part 1: ${day02.part1()}")
    println("Part 2: ${day02.part2()}")
}