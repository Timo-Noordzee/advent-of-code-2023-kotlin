package adventofcode

import kotlinx.benchmark.Benchmark
import kotlinx.benchmark.Setup
import kotlinx.benchmark.State
import kotlinx.benchmark.Warmup
import org.openjdk.jmh.annotations.Fork
import org.openjdk.jmh.annotations.Measurement
import org.openjdk.jmh.annotations.Scope
import java.util.concurrent.TimeUnit

private class NetworkNode(val id: String, var left: NetworkNode? = null, var right: NetworkNode? = null) {

    fun traverse(moveLeft: Boolean) = if (moveLeft) left else right

}

private class NetworkMap(val moves: BooleanArray, private val nodesMap: Map<String, NetworkNode>) {

    val nodes
        get() = nodesMap.values

    fun calculateNumberOfSteps(
        start: String,
        isEnd: (node: NetworkNode) -> Boolean
    ): Int {
        var steps = 0
        var current = nodesMap[start] ?: error("cannot find start node $start")
        val numberOfMoves = moves.size
        while (!isEnd(current)) {
            val moveLeft = moves[steps % numberOfMoves]
            current = current.traverse(moveLeft)!!
            steps++
        }
        return steps
    }
}

@Suppress("DuplicatedCode")
@State(Scope.Benchmark)
@Fork(1)
@Warmup(iterations = 2, time = 2)
@Measurement(iterations = 10, time = 2, timeUnit = TimeUnit.SECONDS)
class Day08 {

    var input = emptyList<String>()

    @Setup
    fun setup() {
        input = readInput("Day08")
    }

    private fun parseMap(input: List<String>): NetworkMap {
        val movesText = input[0]
        val moves = BooleanArray(movesText.length) { i -> movesText[i] == 'L' }
        val nodes = mutableMapOf<String, NetworkNode>()
        input.drop(2).forEach { line ->
            val nodeId = line.substring(0..2)
            val node = nodes.getOrPut(nodeId) { NetworkNode(nodeId) }

            val leftId = line.substring(7..9)
            node.left = nodes.getOrPut(leftId) { NetworkNode(leftId) }

            val rightId = line.substring(12..14)
            node.right = nodes.getOrPut(rightId) { NetworkNode(rightId) }
        }
        return NetworkMap(moves, nodes)
    }

    fun findLCM(a: Long, b: Long): Long {
        val larger = if (a > b) a else b
        val maxLcm = a * b
        var lcm = larger
        while (lcm <= maxLcm) {
            if (lcm % a == 0L && lcm % b == 0L) {
                return lcm
            }
            lcm += larger
        }
        return maxLcm
    }

    @Benchmark
    fun part1(): Int {
        val networkMap = parseMap(input)
        return networkMap.calculateNumberOfSteps("AAA") { it.id == "ZZZ" }
    }

    @Benchmark
    fun part2(): Long {
        val networkMap = parseMap(input)
        return networkMap.nodes
            .filter { it.id.last() == 'A' }
            .map { startNode -> networkMap.calculateNumberOfSteps(startNode.id) { it.id.last() == 'Z' }.toLong() }
            .reduce { acc, l -> findLCM(acc, l) }
    }
}

fun main() {
    val day08 = Day08()

    day08.input = readInput("Day08_test_1")
    check(day08.part1().also { println(it) } == 6)
    day08.input = readInput("Day08_test_2")
    check(day08.part2().also { println(it) } == 6L)

    day08.input = readInput("Day08")
    println("Part 1: ${day08.part1()}")
    println("Part 2: ${day08.part2()}")
}