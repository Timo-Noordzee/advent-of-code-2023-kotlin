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

private sealed class Module(val destinations: List<String>) {

    abstract fun reset()

    abstract fun receivePulse(sender: String, isHighPulse: Boolean): Pair<List<String>, Boolean>

    class Broadcaster(destinations: List<String>) : Module(destinations) {

        override fun reset() {}

        override fun receivePulse(sender: String, isHighPulse: Boolean) = destinations to false
    }

    class FlipFlop(private var on: Boolean = false, destinations: List<String>) : Module(destinations) {

        override fun reset() {
            on = false
        }

        override fun receivePulse(sender: String, isHighPulse: Boolean) = when (isHighPulse) {
            true -> emptyList()
            false -> destinations.also { on = !on }
        } to on
    }

    class Conjunction(
        val inputs: MutableMap<String, Boolean> = mutableMapOf(),
        destinations: List<String>
    ) : Module(destinations) {

        override fun reset() {
            inputs.keys.forEach {
                inputs[it] = false
            }
        }

        override fun receivePulse(sender: String, isHighPulse: Boolean): Pair<List<String>, Boolean> {
            inputs[sender] = isHighPulse
            return destinations to !inputs.values.all { it }
        }
    }
}

@Suppress("DuplicatedCode")
@State(Scope.Benchmark)
@Fork(1)
@Warmup(iterations = 2, time = 2)
@Measurement(iterations = 10, time = 2, timeUnit = TimeUnit.SECONDS)
class Day20 {

    var input = emptyList<String>()

    @Setup
    fun setup() {
        input = readInput("Day20")
    }

    @Benchmark
    fun part1(): Int {
        val modules = parseModules(input)
        val queue = ArrayDeque<Triple<String, String, Boolean>>()

        var lowPulses = 0
        var highPulses = 0

        repeat(1000) {
            queue.add(Triple("button", "broadcaster", false))
            lowPulses++

            while (queue.isNotEmpty()) {
                val (from, to, isHighPulse) = queue.removeFirst()
                val module = modules[to] ?: continue
                val (destinations, sendsHighPulse) = module.receivePulse(from, isHighPulse)

                destinations.forEach { destination ->
                    if (sendsHighPulse) highPulses++ else lowPulses++
                    queue.add(Triple(to, destination, sendsHighPulse))
                }
            }
        }

        return lowPulses * highPulses
    }

    @Benchmark
    fun part2(): Long {
        val modules = parseModules(input)

        val modulesSendingToRx = modules.values.filter { it.destinations.contains("rx") }
        check(modulesSendingToRx.size == 1)

        val toRx = modulesSendingToRx.first()
        check(toRx is Module.Conjunction)

        val conjunctions = toRx.inputs.keys

        return buildMap {
            var pushes = 1L
            val queue = ArrayDeque<Triple<String, String, Boolean>>()

            // Keep simulating as long as not all modules leading to the toRx conjunction have fired a high pulse
            simulation@ while (this.size < conjunctions.size) {
                queue.add(Triple("button", "broadcaster", false))
                queue@ while (queue.isNotEmpty()) {
                    val (sender, receiver, isHighPulse) = queue.removeFirst()

                    // If the module sending a high pulse is sending the pulse to the toRx module keep track of the
                    // number of times the button has been pressed
                    if (isHighPulse && sender in conjunctions) {
                        this[sender] = pushes
                    }

                    val module = modules[receiver] ?: continue@queue
                    val (destinations, isHigh) = module.receivePulse(sender, isHighPulse)
                    destinations.forEach { destination ->
                        queue.add(Triple(receiver, destination, isHigh))
                    }
                }
                pushes++
            }
        }.values.lcm()
    }

    private fun parseModules(input: List<String>): Map<String, Module> {
        val modules = mutableMapOf<String, Module>()

        input.forEach { line ->
            val (module, destinations) = line.split(" -> ")
            when (module.first()) {
                '%' -> modules[module.substring(1)] = Module.FlipFlop(
                    destinations = destinations.split(", ")
                )
                '&' -> modules[module.substring(1)] = Module.Conjunction(
                    destinations = destinations.split(", ")
                )
                'b' -> modules[module] = Module.Broadcaster(
                    destinations = destinations.split(", ")
                )
                else -> error("unknown module")
            }
        }

        modules.forEach { (id, module) ->
            module.destinations.forEach { destination ->
                val destinationModule = modules[destination]
                if (destinationModule is Module.Conjunction) {
                    destinationModule.inputs[id] = false
                }
            }
        }

        return modules
    }
}

fun main() {
    val day20 = Day20()

    day20.input = readInput("Day20_test_1")
    checkAnswer(day20.part1(), 32000000)
    day20.input = readInput("Day20_test_2")
    checkAnswer(day20.part1(), 11687500)

    day20.input = readInput("Day20")
    checkAnswerAndPrint(day20.part1(), 841763884, "Part 1")
    checkAnswerAndPrint(day20.part2(), 246006621493687L, "Part 2")
}