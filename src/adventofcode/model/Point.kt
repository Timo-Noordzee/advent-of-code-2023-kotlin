package adventofcode.model

import kotlin.math.absoluteValue

data class Point(val x: Int, val y: Int)

infix fun Point.distanceTo(other: Point): Int {
    return (x - other.x).absoluteValue + (y - other.y).absoluteValue
}

operator fun Point.plus(direction: Direction) = when (direction) {
    Direction.UP -> copy(
        x = x,
        y = y - 1
    )
    Direction.LEFT -> copy(
        x = x - 1,
        y = y
    )
    Direction.DOWN -> copy(
        x = x,
        y = y + 1,
    )
    Direction.RIGHT -> copy(
        x = x + 1,
        y = y
    )
}