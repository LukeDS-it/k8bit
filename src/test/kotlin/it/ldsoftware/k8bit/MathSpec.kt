package it.ldsoftware.k8bit

import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

/**
 * Tests the 0x8--- opcodes
 */
class MathSpec {

    @Test
    fun `cp should set the value of register x to the value of register y`() {
        val subject = Chip8Processor()
        subject.v[1] = 1
        subject.cp(0, 1)

        assertThat(subject.v[0]).isEqualTo(1)
    }

    @Test
    fun `or should set register x to x | y`() {
        val subject = Chip8Processor()
        subject.v[0] = 0b0010010101101011
        subject.v[1] = 0b0110000110000010
        val expected = 0b0110010111101011

        subject.or(0, 1)

        assertThat(subject.v[0]).isEqualTo(expected)
    }

    @Test
    fun `and should set register x to x & y`() {
        val subject = Chip8Processor()
        subject.v[0] = 0b0010010101101011
        subject.v[1] = 0b0110000110000010
        val expected = 0b0010000100000010

        subject.and(0, 1)

        assertThat(subject.v[0]).isEqualTo(expected)
    }

    @Test
    fun `xor should set register x to x ^ y`() {
        val subject = Chip8Processor()
        subject.v[0] = 0b0010010101101011
        subject.v[1] = 0b0110000110000010
        val expected = 0b0100010011101001

        subject.xor(0, 1)

        assertThat(subject.v[0]).isEqualTo(expected)
    }

    @Test
    fun `sum should set register x to x + y with no carry`() {
        val subject = Chip8Processor()
        subject.v[0] = 0b0010010101101011
        subject.v[1] = 0b0110000110000010
        val expected = 0b1000011011101101

        subject.sum(0, 1)

        assertThat(subject.v[0]).isEqualTo(expected)
        assertThat(subject.v[0xF]).isEqualTo(0)
    }

    @Test
    fun `sum should set register x to x + y with carry`() {
        val subject = Chip8Processor()
        subject.v[0] = 0b0010010101101011
        subject.v[1] = 0b1110000110000010
        val expected = 0b0000011011101101

        subject.sum(0, 1)

        assertThat(subject.v[0]).isEqualTo(expected)
        assertThat(subject.v[0xF]).isEqualTo(1)
    }

    @Test
    fun `sub should set register x to x - y without borrow`() {
        val subject = Chip8Processor()
        subject.v[0] = 0b0010010101101011
        subject.v[1] = 0b0010000110000010
        val expected = 0b0000001111101001

        subject.sub(0, 1)

        assertThat(subject.v[0]).isEqualTo(expected)
        assertThat(subject.v[0xF]).isEqualTo(1)
    }

    @Test
    fun `sub should set register x to x - y with borrow`() {
        val subject = Chip8Processor()
        subject.v[0] = 0b0000111111111111
        subject.v[1] = 0b1000111111111111
        val expected = 0b1000000000000000

        subject.sub(0, 1)

        assertThat(subject.v[0]).isEqualTo(expected)
        assertThat(subject.v[0xF]).isEqualTo(0)
    }

    @Test
    fun `srl should shift right x by 1 and return the previous lsb`() {
        val subject = Chip8Processor()
        subject.v[0] = 0b0000000100000001
        val expected = 0b0000000010000000

        subject.srl(0)

        assertThat(subject.v[0]).isEqualTo(expected)
        assertThat(subject.v[0xF]).isEqualTo(1)
    }

    @Test
    fun `subi should set register x to y - x without borrow`() {
        val subject = Chip8Processor()
        subject.v[0] = 0b0010000110000010
        subject.v[1] = 0b0010010101101011
        val expected = 0b0000001111101001

        subject.subi(0, 1)

        assertThat(subject.v[0]).isEqualTo(expected)
        assertThat(subject.v[0xF]).isEqualTo(1)
    }

    @Test
    fun `subi should set register x to y - x with borrow`() {
        val subject = Chip8Processor()
        subject.v[0] = 0b1000111111111111
        subject.v[1] = 0b0000111111111111
        val expected = 0b1000000000000000

        subject.subi(0, 1)

        assertThat(subject.v[0]).isEqualTo(expected)
        assertThat(subject.v[0xF]).isEqualTo(0)
    }

    @Test
    fun `sll should shift left y by 1 into x returning the previous msb`() {
        val subject = Chip8Processor()
        subject.v[0] = 0b1000000000000101
        val expected = 0b0000000000001010

        subject.sll(0)

        assertThat(subject.v[0]).isEqualTo(expected)
        assertThat(subject.v[0xF]).isEqualTo(1)
    }

}
