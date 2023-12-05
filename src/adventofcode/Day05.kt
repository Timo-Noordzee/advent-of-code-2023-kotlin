package adventofcode

import kotlinx.benchmark.Benchmark
import kotlinx.benchmark.Setup
import kotlinx.benchmark.State
import kotlinx.benchmark.Warmup
import org.openjdk.jmh.annotations.Fork
import org.openjdk.jmh.annotations.Measurement
import org.openjdk.jmh.annotations.Scope
import java.util.concurrent.TimeUnit

private data class Range(val destination: Long, val source: Long, val length: Long) {
    val offset = destination - source
}

private data class Mapper(val to: String, val ranges: List<Range>) {

    fun map(value: Long): Long {
        ranges.forEach { range ->
            if (value > range.source && value < range.source + range.length) {
                return value + range.offset
            }
        }
        return value
    }

    /**
     * Convert a range from the source category to the destination category using the ranges in this mapper
     */
    fun map(value: LongRange): List<LongRange> {
        val rangesToCheck = ArrayList<LongRange>()
        val resultingRanges = ArrayList<LongRange>()

        ranges.forEach { range ->
            val maxStart = value.first.coerceAtLeast(range.source)
            val minEnd = value.last.coerceAtMost(range.source + range.length - 1)
            if (maxStart <= minEnd) {
                rangesToCheck.add(maxStart..minEnd)
                resultingRanges.add(maxStart + range.offset..minEnd + range.offset)
            }
        }

        rangesToCheck.sortBy { it.first }

        var current = value.first
        rangesToCheck.forEach { range ->
            if (range.first > current) {
                resultingRanges.add(current until range.first)
            }
            current = range.last + 1
        }

        if (current <= value.last) resultingRanges.add(current..value.last)

        return resultingRanges
    }
}

private data class Almanac(val seeds: List<Long>, val mappers: Map<String, Mapper>)

@Suppress("DuplicatedCode")
@State(Scope.Benchmark)
@Fork(1)
@Warmup(iterations = 2, time = 2)
@Measurement(iterations = 10, time = 2, timeUnit = TimeUnit.SECONDS)
class Day05 {

    var input = emptyList<String>()

    @Setup
    fun setup() {
        input = readInput("Day05")
    }

    private fun parseInput(input: List<String>): Almanac {
        val mappers = HashMap<String, Mapper>()
        val seeds = input[0].substringAfter(":").trim().split(" ").map { it.toLong() }

        // Split the input by blank lines
        val parts = input.drop(2).fold(mutableListOf(mutableListOf<String>())) { acc, line ->
            if (line.isBlank()) {
                acc.add(mutableListOf())
            } else {
                acc.last().add(line)
            }
            acc
        }

        // Convert each part to a mapper and save it in the mappers map with the 'from' value as key
        parts.forEach { part ->
            val (from, to) = part[0].removeSuffix(" map:").split("-to-")
            val ranges = part.drop(1).map { s ->
                val (destination, source, length) = s.split(" ").map { it.toLong() }
                Range(destination, source, length)
            }
            mappers[from] = Mapper(to, ranges)
        }

        return Almanac(seeds, mappers)
    }

    @Benchmark
    fun part1(): Long {
        val almanac = parseInput(input)
        val mappers = almanac.mappers

        return almanac.seeds.minOf { seed ->
            var category = "seed"
            var soilNumber = seed
            while (category != "location") {
                // Get the destination for the current category
                val mapper = mappers[category] ?: error("no mapper found for $category")

                // Update the soil number using the mapper for the current category
                soilNumber = mapper.map(soilNumber)

                // Update the current category to the destination resulting of the mapper
                category = mapper.to
            }

            soilNumber
        }
    }

    @Benchmark
    fun part2(): Long {
        val almanac = parseInput(input)
        val mappers = almanac.mappers

        return almanac.seeds.chunked(2).minOf { (start, length) ->
            var category = "seed"

            // Keep track of the ranges to check.
            // Initially it contains just a single range of seeds, but because one seed range may overlap with multiple
            // ranges in a mapper, the number of ranges to check may increase.
            var ranges = listOf(start until start + length)

            while (category != "location") {
                // Get the destination for the current category
                val mapper = mappers[category] ?: error("no mapper found for $category")

                // Convert the current list of ranges to a new list of ranges
                ranges = ranges.flatMap { range -> mapper.map(range) }

                // Update the current category to the destination resulting of the mapper
                category = mapper.to
            }

            // Get the lowest starting value of all possible location ranges
            ranges.minOf { it.first }
        }
    }
}

fun main() {
    val day05 = Day05()

    day05.input = readInput("Day05_test")
    check(day05.part1() == 35L)
    check(day05.part2() == 46L)

    day05.input = readInput("Day05")
    println("Part 1: ${day05.part1()}")
    println("Part 2: ${day05.part2()}")
}