package adventofcode

import java.math.BigInteger
import java.security.MessageDigest
import kotlin.io.path.Path
import kotlin.io.path.readLines

/**
 * Reads lines from the given input txt file.
 */
fun readInput(name: String) = Path("src/adventofcode/$name.txt").readLines()

/**
 * Converts string to md5 hash.
 */
fun String.md5() = BigInteger(1, MessageDigest.getInstance("MD5").digest(toByteArray()))
    .toString(16)
    .padStart(32, '0')


fun IntRange.expand(size: Int) = IntRange(first - size, last + size)

fun checkAnswer(answer: Any, expected: Any?) {
    if (expected != null && answer != expected) {
        throw IllegalStateException("expected $expected as answer, but got $answer")
    }
}

fun checkAnswerAndPrint(answer: Any, expected: Any?, prefix: String) {
    if (expected != null) {
        checkAnswer(answer, expected)
    }
    println("$prefix: $answer")
}

/**
 * Split the original IntRange into a pair of IntRange
 *
 * The *first* IntRange is an IntRange containing all values smaller than the [maxValue] or null when all values in
 * the original IntRange are greater than [maxValue]
 *
 * The *second* IntRange is an IntRange containing all values greater than or equal to the [maxValue] or null when
 * all values in the original IntRange are smaller than the [maxValue]
 *
 * @param maxValue the value used to partition the original IntRange
 * @return a pair containing the IntRanges smaller than (first) the [maxValue], and greater than or equal to (second) the [maxValue]
 */
fun IntRange.partition(maxValue: Int): Pair<IntRange?, IntRange?> = when {
    maxValue < first -> Pair(null, this)
    maxValue > last -> Pair(this, null)
    else -> Pair(start until maxValue, maxValue..last)
}

/**
 * Read and append all numbers in a String to a List
 *
 * Example: "{x=787,m=2655,a=1222,s=2876}" results in a list containing [787, 2655, 1222, 2876]
 *
 * @return a list containing all numbers found in the string
 */
fun String.readNumbers(): List<Int> {
    val numbers = mutableListOf<Int>()
    var index = 0
    while (index < length) {
        if (this[index].isDigit()) {
            var num = this[index++] - '0'
            while (index < length && this[index].isDigit()) {
                num = num * 10 + (this[index++] - '0')
            }
            numbers.add(num)
        }
        index++
    }
    return numbers
}

fun String.repeat(amount: Int, separator: String): String {
    if (amount == 1) return this

    val stringBuilder = StringBuilder()
    repeat(amount - 1) {
        stringBuilder.append(this)
        stringBuilder.append(separator)
    }
    stringBuilder.append(this)
    return stringBuilder.toString()
}

inline fun <T> List<T>.split(isDelimiter: (T) -> Boolean): List<List<T>> {
    return fold(mutableListOf(mutableListOf<T>())) { acc, entry ->
        if (isDelimiter(entry)) {
            acc.add(mutableListOf())
        } else {
            acc.last().add(entry)
        }
        acc
    }
}