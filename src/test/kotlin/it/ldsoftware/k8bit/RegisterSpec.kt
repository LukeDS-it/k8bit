package it.ldsoftware.k8bit

import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import org.mockito.Mockito.`when`
import org.mockito.Mockito.mock
import kotlin.random.Random

class RegisterSpec {

    @Test
    fun `memp should set the memory pointer to specified address`() {
        val subject = Chip8Processor()
        subject.memp(0x200)
        assertThat(subject.i).isEqualTo(0x200)
    }

    @Test
    fun `store should set the value of a register`() {
        val subject = Chip8Processor()

        val reg = 1
        val value = 0x5A
        val remaining = reg shl 8 or value

        subject.put(remaining)

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

    @Test
    fun `storeDelay should store the delay timer in the x register`() {
        val subject = Chip8Processor()
        subject.delayTimer = 10

        subject.storeDelay(1)

        assertThat(subject.v[1]).isEqualTo(10)
    }

    @Test
    fun `recoverDelay should recover the delay timer from the x register`() {
        val subject = Chip8Processor()
        subject.v[1] = 60

        subject.recoverDelay(1)

        assertThat(subject.delayTimer).isEqualTo(60)
    }

    @Test
    fun `setSound should set the sound timer from the x register`() {
        val subject = Chip8Processor()
        subject.v[1] = 60

        subject.setSound(1)

        assertThat(subject.soundTimer).isEqualTo(60)
    }

    @Test
    fun `addi should add the content of the register x to register i`() {
        val subject = Chip8Processor()
        subject.i = 1
        subject.v[1] = 1

        subject.addi(1)

        assertThat(subject.i).isEqualTo(2)
    }

    @Test
    fun `charOf should set i to the memory address where the character representation of the data in vx is stored`() {
        val subject = Chip8Processor()
        subject.v[0] = 0
        subject.v[5] = 0xF

        subject.charOf(0)
        assertThat(subject.i).isEqualTo(0)

        subject.charOf(5)
        assertThat(subject.i).isEqualTo(75)
    }

    @Test
    fun `waitKey should return false while there is no keyboard press`() {
        val subject = Chip8Processor()

        assertThat(subject.waitKey(1)).isFalse()
    }

    @Test
    fun `waitKey should return true when a key was pressed and store the key code in vx`() {
        val subject = Chip8Processor()

        subject.press(0xA)

        assertThat(subject.waitKey(1)).isTrue()
        assertThat(subject.v[1]).isEqualTo(0xA)
    }

}
