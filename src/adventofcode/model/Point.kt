package adventofcode.model

import kotlin.math.absoluteValue

data class Point(val x: Int, val y: Int)

infix fun Point.distanceTo(other: Point): Int {
    return (x - other.x).absoluteValue + (y - other.y).absoluteValue
}

operator fun Point.plus(direction: Direction) = copy(
    x = x + direction.horizontalDelta,
    y = y + direction.verticalDelta
)