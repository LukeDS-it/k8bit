package it.ldsoftware.k8bit

import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import org.mockito.Mockito.`when`
import org.mockito.Mockito.mock
import kotlin.random.Random

class RegisterSpec {

    @Test
    fun `retval should set the return value on register i`() {
        val subject = Chip8Processor()
        subject.retVal(0x200)
        assertThat(subject.i).isEqualTo(0x200)
    }

    @Test
    fun `store should set the value of a register`() {
        val subject = Chip8Processor()

        val reg = 1
        val value = 0x5A
        val remaining = reg shl 8 or value

        subject.store(remaining)

        assertThat(subject.v[reg]).isEqualTo(value)
    }

    @Test
    fun `add should add a value to a register`() {
        val subject = Chip8Processor()
        subject.v[1] = 0b1111111111111111

        val reg = 1
        val value = 1
        val remaining = reg shl 8 or value

        subject.add(remaining)

        assertThat(subject.v[reg]).isEqualTo(0)
    }

    @Test
    fun `rnd should set a random value in a register`() {
        val random = mock(Random::class.java)
        val expected = 0xC140
        `when`(random.nextInt()).thenReturn(expected)

        val subject = Chip8Processor(random)

        val reg = 1
        val mask = 0xFF
        val remaining = reg shl 8 or mask

        subject.rnd(remaining)

        assertThat(subject.v[reg]).isEqualTo(0x40)
    }

}
