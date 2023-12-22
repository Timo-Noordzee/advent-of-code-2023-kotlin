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
import kotlin.collections.ArrayDeque

private typealias Layer = Array<IntArray>

private fun emptyLayer() = Array(10) { IntArray(10) }

private fun Layer.canContain(brick: Brick): Boolean {
    return brick.points.all { (x, y) ->
        this[y][x] == 0
    }
}

private class Snapshot(private val bricks: List<Brick>) {

    val n = bricks.size + 1
    val supporting = Array(n) { mutableSetOf<Int>() }
    val supportedBy = Array(n) { mutableSetOf<Int>() }

    fun settle() {
        val layers = mutableMapOf<Int, Layer>()
        layers[0] = Array(10) { IntArray(10) { -1 } }

        var maxZ = 1
        bricks.forEach { brick ->
            var z = maxZ
            while (layers.getOrPut(z - 1) { emptyLayer() }.canContain(brick)) {
                z--
            }

            val drop = brick.start[2] - z
            brick.points.forEach { (x, y, z) ->
                layers.getOrPut(z - drop) { emptyLayer() }[y][x] = brick.id
            }

            maxZ = (brick.end[2] - drop + 1).coerceAtLeast(maxZ)
        }

        for (z in 1 until layers.size) {
            val layer = layers.getOrPut(z) { emptyLayer() }
            val nextLayer = layers.getOrPut(z + 1) { emptyLayer() }

            for (y in 0 until 10) {
                val line = layer[y]
                val nextLine = nextLayer[y]
                for (x in line.indices) {
                    val current = line[x]
                    val next = nextLine[x]
                    if (current != 0) {
                        if (next != 0 && next != current) {
                            supporting[current].add(next)
                            supportedBy[next].add(current)
                        }
                    }
                }
            }
        }
    }
}

private class Brick(val id: Int, val start: IntArray, val end: IntArray) : Comparable<Brick> {

    val points = buildList {
        for (x in start[0]..end[0]) {
            for (y in start[1]..end[1]) {
                for (z in start[2]..end[2]) {
                    add(intArrayOf(x, y, z))
                }
            }
        }
    }

    override fun compareTo(other: Brick): Int {
        return start[2].compareTo(other.start[2])
    }

    companion object {
        fun parse(id: Int, line: String) = Brick(
            id = id,
            start = line.substringBefore('~').split(',').map { it.toInt() }.toIntArray(),
            end = line.substringAfter('~').split(',').map { it.toInt() }.toIntArray(),
        )
    }
}

@Suppress("DuplicatedCode")
@State(Scope.Benchmark)
@Fork(1)
@Warmup(iterations = 2, time = 2)
@Measurement(iterations = 10, time = 2, timeUnit = TimeUnit.SECONDS)
class Day22 {

    var input = emptyList<String>()

    @Setup
    fun setup() {
        input = readInput("Day22")
    }

    @Benchmark
    fun part1(): Int {
        val bricks = input.mapIndexed { index, line -> Brick.parse(index + 1, line) }.sorted()
        val snapshot = Snapshot(bricks).apply { settle() }
        val disintegrated = mutableSetOf<Int>()
        for (block in 1 until snapshot.n) {
            if (snapshot.supporting[block].all { snapshot.supportedBy[it].size > 1 }) {
                disintegrated += block
            }
        }

        return disintegrated.size
    }

    @Benchmark
    fun part2(): Int {
        val bricks = input.mapIndexed { index, line -> Brick.parse(index + 1, line) }.sorted()
        val snapshot = Snapshot(bricks).apply { settle() }
        return (1 until snapshot.n).sumOf { disintegrate(it, snapshot.supporting, snapshot.supportedBy) }
    }

    fun disintegrate(brick: Int, supporting: Array<MutableSet<Int>>, supportedBy: Array<MutableSet<Int>>): Int {
        val queue = ArrayDeque<Int>()
        queue.add(brick)
        val fallenBricks = mutableSetOf<Int>()
        while (queue.isNotEmpty()) {
            val current = queue.removeFirst()
            fallenBricks += current
            supporting[current].forEach { next ->
                val remainingSupports = supportedBy[next] - fallenBricks
                if (remainingSupports.isEmpty()) {
                    queue += next
                }
            }
        }
        return fallenBricks.size - 1
    }
}

fun main() {
    val day22 = Day22()

    day22.input = readInput("Day22_test")
    checkAnswer(day22.part1(), 5)
    checkAnswer(day22.part2(), 7)

    day22.input = readInput("Day22")
    checkAnswerAndPrint(day22.part1(), 499, "Part 1")
    checkAnswerAndPrint(day22.part2(), 95059, "Part 2")
}