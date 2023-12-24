package adventofcode

import kotlinx.benchmark.Benchmark
import kotlinx.benchmark.Setup
import kotlinx.benchmark.State
import kotlinx.benchmark.Warmup
import org.openjdk.jmh.annotations.Fork
import org.openjdk.jmh.annotations.Measurement
import org.openjdk.jmh.annotations.Scope
import java.math.BigInteger
import java.util.*
import java.util.concurrent.TimeUnit
import kotlin.math.absoluteValue

private data class Hailstone(
    val px: Long,
    val py: Long,
    val pz: Long,
    val vx: Long,
    val vy: Long,
    val vz: Long
) {

    fun toLine(): LongArray {
        val c = Math.multiplyExact(vy, px) + Math.multiplyExact(-vx, py)
        return longArrayOf(vy, -vx, c)
    }

    fun xInFuture(x: Double) = if (vx >= 0) x >= px else x <= px

    fun yInFuture(y: Double) = if (vy >= 0) y >= py else y <= py
}

private fun det(a: Long, b: Long, c: Long, d: Long): Double {
    return a.toDouble() * d - b.toDouble() * c.toDouble()
}

private fun lcm(x: BigInteger, y: BigInteger) = x * y / x.gcd(y)

private fun BigInteger.floorDiv(d: BigInteger): BigInteger {
    return if (this >= BigInteger.ZERO) divide(d) else -(-this + d - BigInteger.ONE).divide(d)
}

private fun Long.floorDiv(d: BigInteger): BigInteger = toBigInteger().floorDiv(d)

private fun Long.mod(d: BigInteger): BigInteger = toBigInteger().mod(d)

private fun modRoundUp(x: Long, m: BigInteger, r: BigInteger): BigInteger {
    return (x.floorDiv(m) + if (x.mod(m) <= r) BigInteger.ZERO else BigInteger.ONE) * m + r
}

private fun modRoundDn(x: Long, m: BigInteger, r: BigInteger): BigInteger {
    return (x.floorDiv(m) - if (x.mod(m) >= r) BigInteger.ZERO else BigInteger.ONE) * m + r
}

@Suppress("DuplicatedCode")
@State(Scope.Benchmark)
@Fork(1)
@Warmup(iterations = 2, time = 2)
@Measurement(iterations = 10, time = 2, timeUnit = TimeUnit.SECONDS)
class Day24 {

    var isTest = false
    var input = emptyList<String>()

    @Setup
    fun setup() {
        input = readInput("Day24")
        isTest = false
    }

    @Benchmark
    fun part1(): Int {
        val hailstones = input.map { line ->
            val (position, velocity) = line.split(" @ ")
            val (x, y, z) = position.split(',').map { it.trim().toLong() }
            val (vx, vy, vz) = velocity.split(',').map { it.trim().toLong() }
            Hailstone(x, y, z, vx, vy, vz)
        }

        val range = if (isTest) {
            7.0..27.0
        } else {
            200000000000000.0..400000000000000.0
        }

        var intersections = 0
        for (i in hailstones.indices) {
            for (j in i + 1 until hailstones.size) {
                val a = hailstones[i]
                val b = hailstones[j]

                val lineA = a.toLine()
                val lineB = b.toLine()

                val d = det(lineA[0], lineA[1], lineB[0], lineB[1])
                if (d == 0.0) {
                    continue
                }

                val x = det(lineA[2], lineA[1], lineB[2], lineB[1]) / d
                val y = det(lineA[0], lineA[2], lineB[0], lineB[2]) / d

                if (x !in range || y !in range) {
                    continue
                }

                if (!a.xInFuture(x) || !a.yInFuture(y)) {
                    continue
                }

                if (!b.xInFuture(x) || !b.yInFuture(y)) {
                    continue
                }

                intersections++
            }
        }

        return intersections
    }

    @Benchmark
    // Inspired (mostly copied) by https://github.com/elizarov/AdventOfCode2023/blob/main/src/Day24_2.kt
    fun part2(): Long {
        val hailstones = input.map { line ->
            val (position, velocity) = line.split(" @ ")
            val (x, y, z) = position.split(',').map { it.trim().toLong() }
            val (vx, vy, vz) = velocity.split(',').map { it.trim().toLong() }
            Hailstone(x, y, z, vx, vy, vz)
        }

        fun solve(positions: List<Long>, velocities: List<Long>): Long {
            val n = positions.size
            check(n == velocities.size)

            val minV = -1000L
            val maxV = 1000L
            val minP = 0L
            val maxP = 1_000_000_000_000_000L

            check(minV < velocities.min() && maxV > velocities.max())
            check(minP < positions.min() && maxP > positions.max())

            val rs = ArrayList<Pair<LongRange, Long>>()
            val velocityPositionMap = velocities
                .zip(positions)
                .groupBy { it.first }
                .mapValues { e -> e.value.map { it.second }.toSet() }

            velocityLoop@ for (v in minV..maxV) {
                val p1 = velocities.withIndex().filter { v < it.value }.maxOfOrNull { positions[it.index] } ?: minP
                val p2 = velocities.withIndex().filter { v > it.value }.minOfOrNull { positions[it.index] } ?: maxP

                if (p1 > p2) {
                    continue
                }

                var pmod = BigInteger.ONE
                var prem = BigInteger.ZERO
                var p1r = p1
                var p2r = p2

                for (i in 0 until n) {
                    val pi = positions[i]
                    val vi = velocities[i]
                    if (v == vi) {
                        val p0 = velocityPositionMap[v]?.singleOrNull() ?: continue@velocityLoop
                        if (p0 !in p1r..p2r) continue@velocityLoop
                        p1r = p0
                        p2r = p0
                        continue
                    }

                    // t_meet = (p - pi) / (vi - v)
                    val d = (vi - v).absoluteValue.toBigInteger()
                    val r = pi.mod(d)
                    val pmod2 = lcm(pmod, d)
                    var prem2 = prem

                    while (prem2 < pmod2) {
                        if (prem2.remainder(d) == r) break
                        prem2 += pmod

                        if (prem2 >= pmod2) {
                            continue@velocityLoop
                        }

                        if (prem2 > p2r.toBigInteger()) {
                            continue@velocityLoop
                        }
                    }

                    pmod = pmod2
                    prem = prem2

                    val p1n = modRoundUp(p1r, pmod, prem)
                    val p2n = modRoundDn(p2r, pmod, prem)
                    if (p1n > p2n) {
                        continue@velocityLoop
                    }

                    check(p1n >= p1r.toBigInteger())
                    check(p2n <= p2r.toBigInteger())

                    p1r = p1n.toLong()
                    p2r = p2n.toLong()
                }

                rs += Pair(p1r..p2r, v)
            }

            val (range) = rs.single()
            return range.first
        }

        val x = solve(hailstones.map { it.px }, hailstones.map { it.vx })
        val y = solve(hailstones.map { it.py }, hailstones.map { it.vy })
        val z = solve(hailstones.map { it.pz }, hailstones.map { it.vz })

        return x + y + z
    }
}

fun main() {
    val day24 = Day24()

    day24.isTest = true
    day24.input = readInput("Day24_test")
    checkAnswer(day24.part1(), 2)
//    checkAnswer(day24.part2(), 47)

    day24.isTest = false
    day24.input = readInput("Day24")
    checkAnswerAndPrint(day24.part1(), 23760, "Part 1")
    checkAnswerAndPrint(day24.part2(), 888708704663413L, "Part 2")
}