package it.ldsoftware.k8bit.hardware

/**
 * Draws a sprite starting from x, y. Lines will contain the lines of the sprite. The sprite will be 8 bits wide
 * and n bits high, where n is the length of the lines array.
 *
 * It will return 1 if the sprite has collided with some existing sprite on the screen.
 */
interface Graphics {
    fun clear()
    fun getScreen(): Array<Int>
    fun subset(from: Int, to: Int): Array<Int>
    fun draw(x: Int, y: Int, lines: Array<Int>): Int
}
