package it.ldsoftware.k8bit

import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class GraphicSpec {

    @Test
    fun `draw should give correct inputs to the graphic memory`() {
        val subject = Chip8Processor()
        subject.load(arrayOf(0x10, 0x02, 0x3C, 0xC3, 0xFF))
        subject.v[0] = 16
        subject.i = 2

        val xPos = 0
        val yPos = 0
        val lines = 3

        val remaining = (xPos shl 8) or (yPos shl 4) or lines

        subject.draw(remaining)

        assertThat(subject.v[0xF]).isEqualTo(0)
        assertThat(subject.gfx.subset(1040, 1048)).isEqualTo(arrayOf(0, 0, 1, 1, 1, 1, 0, 0))
        assertThat(subject.gfx.subset(1104, 1112)).isEqualTo(arrayOf(1, 1, 0, 0, 0, 0, 1, 1))
        assertThat(subject.gfx.subset(1168, 1176)).isEqualTo(arrayOf(1, 1, 1, 1, 1, 1, 1, 1))
    }

    @Test
    fun `draw should signal if there is a collision`() {
        val subject = Chip8Processor()
        subject.load(arrayOf(0x10, 0x02, 0x3C, 0xC3, 0xFF))
        subject.v[0] = 16
        subject.i = 2

        val xPos = 0
        val yPos = 0
        val lines = 3

        val remaining = (xPos shl 8) or (yPos shl 4) or lines

        subject.draw(remaining)
        subject.draw(remaining)

        assertThat(subject.v[0xF]).isEqualTo(1)
    }

}
