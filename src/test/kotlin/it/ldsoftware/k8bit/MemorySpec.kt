package it.ldsoftware.k8bit

import it.ldsoftware.k8bit.Chip8Processor
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class MemorySpec {

    @Test
    fun `getOpCode should get the opcode from two adjacent memory addresses`() {
        val subject = Chip8Processor()
        subject.load(arrayOf(0x80, 0x73))
        assertThat(subject.getOpCode()).isEqualTo(0x8073)
    }

}
