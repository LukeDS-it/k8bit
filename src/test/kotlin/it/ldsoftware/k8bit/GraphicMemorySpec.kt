package it.ldsoftware.k8bit

import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class GraphicMemorySpec {

    @Test
    fun `expand should correctly represent a binary string to zero-one array`() {
        val test = 0b10010010
        assertThat(test.expand()).isEqualTo(listOf(1, 0, 0, 1, 0, 0, 1, 0))
    }

    @Test
    fun `draw should draw the correct pixels on an empty memory and return no collision`() {
        val subject = GraphicMemory()

        val x = 16
        val y = 16
        val lines = arrayOf(0xF0, 0x10, 0x20, 0x40, 0x40)

        val collisions = subject.draw(x, y, lines)

        assertThat(collisions).isEqualTo(0)
        assertThat(subject.subset(1040, 1044)).isEqualTo(arrayOf(1, 1, 1, 1))
        assertThat(subject.subset(1104, 1108)).isEqualTo(arrayOf(0, 0, 0, 1))
        assertThat(subject.subset(1168, 1172)).isEqualTo(arrayOf(0, 0, 1, 0))
        assertThat(subject.subset(1232, 1236)).isEqualTo(arrayOf(0, 1, 0, 0))
        assertThat(subject.subset(1296, 1300)).isEqualTo(arrayOf(0, 1, 0, 0))
    }

    @Test
    fun `draw should return collisions`() {
        val subject = GraphicMemory()

        val x = 16
        val y = 16
        val lines = arrayOf(0xF0, 0x10, 0x20, 0x40, 0x40)

        subject.draw(x, y, lines)
        val collisions = subject.draw(x, y, lines)

        assertThat(collisions).isEqualTo(1)
        assertThat(subject.subset(1040, 1044)).isEqualTo(arrayOf(0, 0, 0, 0))
        assertThat(subject.subset(1104, 1108)).isEqualTo(arrayOf(0, 0, 0, 0))
        assertThat(subject.subset(1168, 1172)).isEqualTo(arrayOf(0, 0, 0, 0))
        assertThat(subject.subset(1232, 1236)).isEqualTo(arrayOf(0, 0, 0, 0))
        assertThat(subject.subset(1296, 1300)).isEqualTo(arrayOf(0, 0, 0, 0))
    }

    @Test
    fun `clear should empty the memory`() {
        val subject = GraphicMemory()

        val x = 16
        val y = 16
        val lines = arrayOf(0xF0, 0x10, 0x20, 0x40, 0x40)

        subject.draw(x, y, lines)
        subject.clear()

        assertThat(subject.backingArray).doesNotContain(1)
    }

}
