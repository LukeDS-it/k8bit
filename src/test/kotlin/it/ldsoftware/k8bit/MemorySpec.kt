package it.ldsoftware.k8bit

import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class MemorySpec {

    @Test
    fun `getOpCode should get the opcode from two adjacent memory addresses`() {
        val subject = Chip8Processor()
        subject.load(arrayOf(0x80, 0x73))
        assertThat(subject.getOpCode()).isEqualTo(0x8073)
    }

    @Test
    fun `storeBin should store in registers i to i + 3 the BCD representation of the value stored in register x`() {
        val subject = Chip8Processor()
        subject.v[1] = 129
        subject.i = 0x42

        subject.bcd(1)

        assertThat(subject.memory[0x42]).isEqualTo(0b0001)
        assertThat(subject.memory[0x43]).isEqualTo(0b0010)
        assertThat(subject.memory[0x44]).isEqualTo(0b1001)
    }

    @Test
    fun `storeAll should store values of register 0 to x in memory, starting from address i`() {
        val subject = Chip8Processor()
        subject.v[0] = 1
        subject.v[1] = 2
        subject.v[2] = 3
        subject.v[3] = 4
        subject.v[4] = 5
        subject.i = 0x50

        subject.storeAll(4)

        assertThat(subject.memory[0x50]).isEqualTo(1)
        assertThat(subject.memory[0x51]).isEqualTo(2)
        assertThat(subject.memory[0x52]).isEqualTo(3)
        assertThat(subject.memory[0x53]).isEqualTo(4)
        assertThat(subject.memory[0x54]).isEqualTo(5)
        assertThat(subject.i).isEqualTo(0x55)
    }

    @Test
    fun `loadAll should load into registers 0 to x values coming from memory addresses i to i + x`() {
        val subject = Chip8Processor()
        subject.memory[0x50] = 1
        subject.memory[0x51] = 2
        subject.memory[0x52] = 3
        subject.memory[0x53] = 4
        subject.memory[0x54] = 5
        subject.i = 0x50

        subject.loadAll(4)

        assertThat(subject.v[0]).isEqualTo(1)
        assertThat(subject.v[1]).isEqualTo(2)
        assertThat(subject.v[2]).isEqualTo(3)
        assertThat(subject.v[3]).isEqualTo(4)
        assertThat(subject.v[4]).isEqualTo(5)
        assertThat(subject.i).isEqualTo(0x55)
    }

}
