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

private class UnionFind {

    val parent = mutableMapOf<String, String>()
    val groupSize = mutableMapOf<String, Int>()

    fun find(component: String): String {
        if (parent.getOrPut(component) { component } != component) parent[component] = find(parent[component]!!)
        return parent.getValue(component)
    }

    fun union(a: String, b: String): Boolean {
        val xSet = find(a)
        val ySet = find(b)

        if (xSet == ySet) return false

        val xSize = groupSize.getOrDefault(xSet, 1)
        val ySize = groupSize.getOrDefault(ySet, 1)

        if (xSize > ySize) {
            parent[ySet] = xSet
            groupSize[xSet] = xSize + ySize
        } else {
            parent[xSet] = ySet
            groupSize[ySet] = ySize + xSize
        }

        return true
    }
}

@Suppress("DuplicatedCode")
@State(Scope.Benchmark)
@Fork(1)
@Warmup(iterations = 2, time = 2)
@Measurement(iterations = 10, time = 2, timeUnit = TimeUnit.SECONDS)
class Day25 {

    var isTest = false
    var input = emptyList<String>()

    @Setup
    fun setup() {
        input = readInput("Day25")
        isTest = false
    }

    @Benchmark
    fun part1(): Int {
        val dsu = UnionFind()

//        println("Graph:")
//        input.forEach { line ->
//            val origin = line.substringBefore(':')
//            val dest = line.substringAfter(' ').split(' ').joinToString { "\"$it\"" }
//            println("\"$origin\" -> $dest")
//        }

        // Used Graphviz to manually find which wires to disconnect
        val removed = if (isTest) {
            mutableListOf(Pair("jqt", "nvd"), Pair("pzl", "hfx"), Pair("cmg", "bvb"))
        } else {
            mutableListOf(Pair("jxd", "bbz"), Pair("brd", "clb"), Pair("mxd", "glz"))
        }

        input.forEach { line ->
            val from = line.substringBefore(':')
            line.substringAfter(' ').split(' ').forEach { to ->
                val key = from to to
                if (key !in removed) {
                    dsu.union(from, to)
                }
            }
        }

        val (a, b) = dsu.groupSize.values.sortedDescending()
        return a * b
    }
}

fun main() {
    val day25 = Day25()

    day25.input = readInput("Day25_test")
    day25.isTest = true
    checkAnswer(day25.part1(), 54)

    day25.input = readInput("Day25")
    day25.isTest = false
    checkAnswerAndPrint(day25.part1(), 518391, "Part 1")
}