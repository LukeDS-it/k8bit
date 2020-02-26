package it.ldsoftware.k8bit

class GraphicMemory {

    companion object {
        const val width = 64
        const val height = 32
    }

    val backingArray = Array(width * height) { Constants.EMPTY }

    fun clear() {
        backingArray.fill(Constants.EMPTY)
    }

    /**
     * Draws a sprite starting from x, y. Lines will contain the lines of the sprite. The sprite will be 8 bits wide
     * and n bits high, where n is the length of the lines array.
     *
     * It will return 1 if the sprite has collided with some existing sprite on the screen.
     */
    fun draw(x: Int, y: Int, lines: Array<Int>): Int {
        var collision = 0

        val bitmap = lines.flatMap { it.expand() }
        var drawn = 0

        for (rows in y until (lines.size + y)) {
            for (cols in x..x + 7) {
                val cell = cols + rows * width
                val curr = backingArray[cell]
                val px = bitmap[drawn]
                if (px == 1 && curr == px) {
                    collision = 1
                }
                backingArray[cell] = curr xor px
                drawn++
            }
        }

        return collision
    }

    fun subset(from: Int, to: Int): Array<Int> = backingArray.drop(from).take(to - from).toTypedArray()

}
