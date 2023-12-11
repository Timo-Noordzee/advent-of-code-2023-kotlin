package adventofcode.model

enum class Direction(val value: Int) {
    UP(0),
    RIGHT(1),
    DOWN(2),
    LEFT(3);
}

operator fun Direction.unaryMinus() = when (this) {
    Direction.UP -> Direction.DOWN
    Direction.RIGHT -> Direction.LEFT
    Direction.DOWN -> Direction.UP
    Direction.LEFT -> Direction.RIGHT
}
