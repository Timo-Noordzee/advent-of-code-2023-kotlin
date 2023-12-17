package adventofcode.model

@JvmInline
value class Direction(val value: Int) {

    val horizontalDelta
        get() = if (value and 1 == 1) 2 - value else 0

    val verticalDelta
        get() = if (value and 1 == 0) value - 1 else 0

    val opposite
        get() = Direction(value xor 2)

    companion object {
        val UP = Direction(0)
        val RIGHT = Direction(1)
        val DOWN = Direction(2)
        val LEFT = Direction(3)

        val ALL = arrayOf(UP, RIGHT, DOWN, LEFT)
    }
}