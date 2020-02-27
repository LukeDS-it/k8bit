package it.ldsoftware.k8bit.hardware.predefined

import it.ldsoftware.k8bit.Constants
import it.ldsoftware.k8bit.expand
import it.ldsoftware.k8bit.hardware.Graphics

/**
 * Graphic memory, used to store pixel data and calculate collisions
 */
class GraphicMemory : Graphics {

    companion object {
        const val width = 64
        const val height = 32
    }

    private val backingArray = Array(width * height) { Constants.EMPTY }

    override fun getScreen(): Array<Int> = backingArray.clone()

    override fun clear() {
        backingArray.fill(Constants.EMPTY)
    }

    /**
     * Draws a sprite starting from x, y. Lines will contain the lines of the sprite. The sprite will be 8 bits wide
     * and n bits high, where n is the length of the lines array.
     *
     * It will return 1 if the sprite has collided with some existing sprite on the screen.
     */
    override fun draw(x: Int, y: Int, lines: Array<Int>): Int {
        var collision = 0

        val bitmap = lines.flatMap { it.expand() }
        var drawn = 0

        for (rows in y until (lines.size + y)) {
            for (cols in x until x + 8) {
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

    override fun subset(from: Int, to: Int): Array<Int> = backingArray.drop(from).take(to - from).toTypedArray()

}
