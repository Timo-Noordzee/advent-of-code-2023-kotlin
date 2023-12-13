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

/**
 * The cleaner shorthand for printing output.
 */
fun Any?.println() = println(this)


fun IntRange.expand(size: Int) = IntRange(first - size, last + size)

fun checkAnswer(answer: Any, expected: Any) {
    if (answer != expected) {
        throw IllegalStateException("expected $expected as answer, but got $answer")
    }
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