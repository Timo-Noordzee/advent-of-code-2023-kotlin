package adventofcode.model

data class Point(val x: Int, val y: Int)

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