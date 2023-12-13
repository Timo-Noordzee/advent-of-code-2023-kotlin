package adventofcode

import kotlinx.benchmark.Benchmark
import kotlinx.benchmark.Setup
import kotlinx.benchmark.State
import kotlinx.benchmark.Warmup
import org.openjdk.jmh.annotations.Fork
import org.openjdk.jmh.annotations.Measurement
import org.openjdk.jmh.annotations.Scope
import java.util.concurrent.TimeUnit

private fun isPowerOfTwo(n: Int): Boolean {
    return n != 0 && ((n and (n - 1)) == 0)
}

private fun isOneBitDifference(x: Int, y: Int): Boolean {
    return isPowerOfTwo(x xor y)
}

@Suppress("DuplicatedCode")
@State(Scope.Benchmark)
@Fork(1)
@Warmup(iterations = 2, time = 2)
@Measurement(iterations = 10, time = 2, timeUnit = TimeUnit.SECONDS)
class Day13 {

    var input = emptyList<String>()

    @Setup
    fun setup() {
        input = readInput("Day13")
    }

    @Benchmark
    fun part1() = solve(false)

    @Benchmark
    fun part2() = solve(true)

    /**
     * Check of a line of reflection in an array of integers
     *
     * @param withSmudge used to determine whenever one of the mirrors contains smudge
     * @return the index of the right-hand side of the line of reflection or null when no line of reflection is found
     */
    private fun checkForLineOfReflection(values: IntArray, withSmudge: Boolean): Int? {
        val n = values.size
        outer@ for (i in 1 until n) {
            var hasSmudge = withSmudge
            var r = i
            var l = r - 1
            while (l >= 0 && r < n) {
                if (values[l] != values[r]) {
                    // If there is no smudge left or the values at index l and r differ by more than 1 bit there can't be a line of reflecting at index i
                    if (!hasSmudge || !isOneBitDifference(values[l], values[r])) {
                        continue@outer
                    }
                    hasSmudge = false
                }
                r++
                l--
            }

            // If there is no smudge left when a line of reflection it found, it is a valid line of reflection
            if (!hasSmudge) {
                // A line of reflection is found between index i-1 and index i
                return i
            }
        }

        // No line of reflection could be found in the given array of numbers
        return null
    }

    private fun solve(withSmudge: Boolean) = input.split { it.isBlank() }.sumOf { lines ->
        val m = lines.size
        val n = lines.first().length

        // Create an integer representing each row / column where:
        // - ash (.) = 0
        // - rock (#) = 1
        // For example, the row #.##..##. represents the number 101100110 in binary (281)
        val rows = IntArray(m)
        val columns = IntArray(n)
        for (i in lines.indices) {
            var rowValue = 0
            val line = lines[i]
            for (j in line.indices) {
                rowValue = rowValue shl 1
                columns[j] = columns[j] shl 1
                if (line[j] == '#') {
                    columns[j]++
                    rowValue++
                }
            }
            rows[i] = rowValue
        }

        val horizontalLineOfReflection = checkForLineOfReflection(rows, withSmudge)
        if (horizontalLineOfReflection != null) {
            return@sumOf horizontalLineOfReflection * 100
        }

        val verticalLineOfReflection = checkForLineOfReflection(columns, withSmudge)
        if (verticalLineOfReflection != null) {
            return@sumOf verticalLineOfReflection
        }

        error("no line of reflection found")
    }
}

fun main() {
    val day13 = Day13()

    day13.input = readInput("Day13_test")
    checkAnswer(day13.part1(), 405)
    checkAnswer(day13.part2(), 400)

    day13.input = readInput("Day13")
    println("Part 1: ${day13.part1()}")
    println("Part 2: ${day13.part2()}")
}