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

private data class PartRanges(
    val x: IntRange,
    val m: IntRange,
    val a: IntRange,
    val s: IntRange,
) {
    val distinctCombinations: Long
        get() = (1L + x.last - x.first) * (1L + m.last - m.first) * (1L + a.last - a.first) * (1L + s.last - s.first)

    operator fun get(category: Int) = when (category) {
        0 -> x
        1 -> m
        2 -> a
        3 -> s
        else -> error("unknown category $category")
    }

    fun copyWith(category: Int, value: IntRange) = when (category) {
        0 -> copy(x = value)
        1 -> copy(m = value)
        2 -> copy(a = value)
        3 -> copy(s = value)
        else -> error("unknown category $category")
    }
}

private class WorkflowRule(
    val category: Int,
    val operator: Char,
    val value: Int,
    val result: String
) {

    operator fun invoke(part: List<Int>): String? {
        val rating = part[category]
        val matchesCondition = when (operator) {
            '>' -> rating > value
            '<' -> rating < value
            else -> error("unknown operator $operator")
        }

        return if (matchesCondition) result else null
    }
}

private class Workflow(
    private val workflowRules: List<WorkflowRule>,
    private val defaultResult: String
) {

    operator fun invoke(part: List<Int>) = workflowRules.firstNotNullOfOrNull { it.invoke(part) } ?: defaultResult

    fun split(partRanges: PartRanges): List<Pair<PartRanges, String>> {
        var currentRanges = partRanges
        val newRanges = mutableListOf<Pair<PartRanges, String>>()
        workflowRules.forEach { rule ->
            when (rule.operator) {
                '>' -> {
                    val (left, right) = currentRanges[rule.category].partition(rule.value + 1)
                    if (right != null) {
                        newRanges.add(currentRanges.copyWith(rule.category, right) to rule.result)
                    }

                    if (left == null) {
                        return newRanges
                    }

                    currentRanges = currentRanges.copyWith(rule.category, left)
                }
                '<' -> {
                    val (left, right) = currentRanges[rule.category].partition(rule.value)
                    if (left != null) {
                        newRanges.add(currentRanges.copyWith(rule.category, left) to rule.result)
                    }

                    if (right == null) {
                        return newRanges
                    }

                    currentRanges = currentRanges.copyWith(rule.category, right)
                }
                else -> error("unknown operator ${rule.operator}")
            }
        }

        newRanges.add(currentRanges to defaultResult)
        return newRanges
    }
}

@Suppress("DuplicatedCode")
@State(Scope.Benchmark)
@Fork(1)
@Warmup(iterations = 2, time = 2)
@Measurement(iterations = 10, time = 2, timeUnit = TimeUnit.SECONDS)
class Day19 {

    var input = emptyList<String>()

    @Setup
    fun setup() {
        input = readInput("Day19")
    }

    @Benchmark
    fun part1(): Int {
        val workflows = parseWorkflows(input)
        val parts = input.takeLastWhile { it.isNotEmpty() }.map { it.readNumbers() }

        return parts.filter { part ->
            // Keep sending the part to the next workflow as long as the part isn't accepted (A) or rejected (R)
            val result = generateSequence("in") { name -> workflows.getValue(name).invoke(part) }.first {
                it == "A" || it == "R"
            }

            result == "A"
        }.sumOf { it.sum() }
    }

    @Benchmark
    fun part2(): Long {
        val workflows = parseWorkflows(input)
        val queue = ArrayDeque<Pair<PartRanges, String>>()

        // Start with just a single item where each category contains the full range of possible values
        val initial = PartRanges(
            x = 1..4000,
            m = 1..4000,
            a = 1..4000,
            s = 1..4000,
        )

        queue.add(Pair(initial, "in"))

        var distinctCombinations = 0L
        while (queue.isNotEmpty()) {
            val (partRange, id) = queue.removeFirst()

            workflows.getValue(id).split(partRange).forEach { (range, result) ->
                when (result) {
                    "R" -> {} // Ignore rejected ranges
                    "A" -> distinctCombinations += range.distinctCombinations
                    else -> queue.add(Pair(range, result))
                }
            }
        }

        return distinctCombinations
    }

    private fun parseWorkflows(input: List<String>): Map<String, Workflow> {
        return input.takeWhile { it.isNotEmpty() }.associate { workflow ->
            val (id, rules) = workflow.split('{')
            val workflowRules = rules.split(',').dropLast(1).map { rule ->
                WorkflowRule(
                    category = when (rule[0]) {
                        'x' -> 0
                        'm' -> 1
                        'a' -> 2
                        's' -> 3
                        else -> error("unknown category")
                    },
                    operator = rule[1],
                    value = rule.substring(2).substringBefore(':').toInt(),
                    result = rule.substringAfterLast(':')
                )
            }

            id to Workflow(
                workflowRules = workflowRules,
                defaultResult = rules.substringAfterLast(',').removeSuffix("}")
            )
        }
    }
}

fun main() {
    val day19 = Day19()

    day19.input = readInput("Day19_test")
    checkAnswer(day19.part1(), 19114)
    checkAnswer(day19.part2(), 167409079868000L)

    day19.input = readInput("Day19")
    checkAnswerAndPrint(day19.part1(), 368964, "Part 1")
    checkAnswerAndPrint(day19.part2(), 127675188176682L, "Part 2")
}