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
class Day06 {

    var input = emptyList<String>()

    @Setup
    fun setup() {
        input = readInput("Day06")
    }

    @Benchmark
    fun part1(): Int {
        val whitespaceRegex = Regex("\\s+")
        val time = input[0].substringAfter(':').trim().split(whitespaceRegex).map { it.toInt() }
        val distance = input[1].substringAfter(':').trim().split(whitespaceRegex).map { it.toInt() }

        var answer = 1
        for (i in time.indices) {
            val raceDuration = time[i]
            val recordDistance = distance[i]

            // Calculate the minimum number of seconds the button needs to be hold for to beat the record distance
            var left = 0
            var right = raceDuration shr 1
            while (left <= right) {
                val mid = (left + right) / 2
                val totalDistance = mid * (raceDuration - mid)
                when {
                    totalDistance > recordDistance -> right = mid - 1
                    else -> left = mid + 1
                }
            }

            // Because the total distance travelled is a parabola, the right bound can be calculated from just the left bound
            // For example, time=7 results in a distance travelled of [0,6,10,12,10,6,0]
            answer *= if (raceDuration and 1 == 0) {
                ((raceDuration shr 1) - left) * 2 + 1
            } else {
                ((raceDuration shr 1) - left) * 2 + 2
            }
        }

        return answer
    }

    @Benchmark
    fun part2(): Long {
        val whitespaceRegex = Regex("\\s+")
        val raceDuration = input[0].substringAfter(':').replace(whitespaceRegex, "").toLong()
        val recordDistance = input[1].substringAfter(':').replace(whitespaceRegex, "").toLong()

        // Calculate the minimum number of seconds the button needs to be hold for to beat the record distance
        var left = 0L
        var right = raceDuration shr 1
        while (left <= right) {
            val mid = (left + right) / 2
            val totalDistance = mid * (raceDuration - mid)
            when {
                totalDistance > recordDistance -> right = mid - 1
                else -> left = mid + 1
            }
        }

        return if (raceDuration and 1L == 0L) {
            ((raceDuration shr 1) - left) * 2 + 1
        } else {
            ((raceDuration shr 1) - left) * 2 + 2
        }
    }
}

fun main() {
    val day06 = Day06()

    day06.input = readInput("Day06_test")
    check(day06.part1() == 288)
    check(day06.part2() == 71503L)

    day06.input = readInput("Day06")
    println("Part 1: ${day06.part1()}")
    println("Part 2: ${day06.part2()}")
}