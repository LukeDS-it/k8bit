package it.ldsoftware.k8bit

import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

class PCSpec {

    @Test
    fun `jump should move the program counter to the specified memory address`() {
        val subject = Chip8Processor()

        subject.jumpa(0x520)

        assertThat(subject.pc).isEqualTo(0x520)
    }

    @Test
    fun `call should move the pc to the specified memory address, remembering the last location`() {
        val subject = Chip8Processor()

        subject.call(0x520)

        assertThat(subject.pc).isEqualTo(0x520)
        assertThat(subject.stack[subject.sp - 1]).isEqualTo(0x200)
    }

    @Test
    fun `ret should return the execution to where the program called the subroutine`() {
        val subject = Chip8Processor()

        subject.call(0x520)
        subject.ret()

        assertThat(subject.pc).isEqualTo(0x200)
        assertThat(subject.sp).isEqualTo(0)
    }

    @Test
    fun `eq should increment the pc if the condition is true`() {
        val subject = Chip8Processor()

        val register = 1
        val value = 0x20
        subject.v[register] = value

        val remaining = (register shl 8) or value

        subject.eq(remaining)
        assertThat(subject.pc).isEqualTo(0x202)
    }

    @Test
    fun `eq should not increment the pc if the condition is false`() {
        val subject = Chip8Processor()

        val register = 1
        val value = 0x20
        subject.v[register] = value

        val remaining = register shl 8 or 0x21

        subject.eq(remaining)
        assertThat(subject.pc).isEqualTo(0x200)
    }

    @Test
    fun `neq should increment the pc if the condition is false`() {
        val subject = Chip8Processor()

        val register = 1
        val value = 0x20
        subject.v[register] = value

        val remaining = (register shl 8) or 0x21

        subject.neq(remaining)
        assertThat(subject.pc).isEqualTo(0x202)
    }

    @Test
    fun `neq should not increment the pc if the condition is true`() {
        val subject = Chip8Processor()

        val register = 1
        val value = 0x20
        subject.v[register] = value

        val remaining = register shl 8 or value

        subject.neq(remaining)
        assertThat(subject.pc).isEqualTo(0x200)
    }

    @Test
    fun `eqreg should increment the pc if the two registers have the same value`() {
        val subject = Chip8Processor()

        val reg1 = 1
        val reg2 = 2
        val value = 0x20
        subject.v[reg1] = value
        subject.v[reg2] = value

        val remaining = (reg1 shl 8) or (reg2 shl 4)

        subject.eqreg(remaining)
        assertThat(subject.pc).isEqualTo(0x202)
    }

    @Test
    fun `eqreg should not increment the pc if the two registers do not have the same value`() {
        val subject = Chip8Processor()

        val reg1 = 1
        val reg2 = 2
        val value = 0x20
        subject.v[reg1] = value
        subject.v[reg2] = 0x21

        val remaining = (reg1 shl 8) or (reg2 shl 4)

        subject.eqreg(remaining)
        assertThat(subject.pc).isEqualTo(0x200)
    }

    @Test
    fun `neqreg should increment the pc if the two registers do not have the same value`() {
        val subject = Chip8Processor()

        val reg1 = 1
        val reg2 = 2
        val value = 0x20
        subject.v[reg1] = value
        subject.v[reg2] = 0x21

        val remaining = (reg1 shl 8) or (reg2 shl 4)

        subject.neqreg(remaining)
        assertThat(subject.pc).isEqualTo(0x202)
    }

    @Test
    fun `neqreg should not increment the pc if the two registers have the same value`() {
        val subject = Chip8Processor()

        val reg1 = 1
        val reg2 = 2
        val value = 0x20
        subject.v[reg1] = value
        subject.v[reg2] = value

        val remaining = (reg1 shl 8) or (reg2 shl 4)

        subject.neqreg(remaining)
        assertThat(subject.pc).isEqualTo(0x200)
    }

    @Test
    fun `jumpmem should jump the pc with the given value, plus the value in register v0`() {
        val subject = Chip8Processor()

        val value = 0x200
        subject.v[0] = 0x10

        subject.jumpv(value)
        assertThat(subject.pc).isEqualTo(0x210)
    }

    @Test
    fun `skipKeyEq should skip next instruction if the key in register x is pressed`() {
        val subject = Chip8Processor()
        subject.press(0xF)
        subject.v[3] = 0xF

        subject.skipKeyEq(3)

        assertThat(subject.keys[0xF]).isTrue()
        assertThat(subject.pc).isEqualTo(0x202)
    }

    @Test
    fun `skipKeyEq should not skip next instruction if the key in register x is not pressed`() {
        val subject = Chip8Processor()
        subject.press(0xF)
        subject.v[3] = 0xF

        subject.skipKeyEq(2)

        assertThat(subject.keys[0]).isFalse()
        assertThat(subject.pc).isEqualTo(0x200)
    }

    @Test
    fun `skipKeyNeq should skip next instruction if the key in register x is not pressed`() {
        val subject = Chip8Processor()
        subject.press(0xF)
        subject.v[3] = 0xF

        subject.skipKeyNeq(2)

        assertThat(subject.keys[0xF]).isTrue()
        assertThat(subject.pc).isEqualTo(0x202)
    }

    @Test
    fun `skipKeyNeq should not skip next instruction if the key in register x is pressed`() {
        val subject = Chip8Processor()
        subject.press(0xF)
        subject.v[3] = 0xF

        subject.skipKeyNeq(3)

        assertThat(subject.keys[0]).isFalse()
        assertThat(subject.pc).isEqualTo(0x200)
    }

    @Test
    fun `skipKeyEq should not skip next instruction if the key in register x was released`() {
        val subject = Chip8Processor()
        subject.press(0xF)
        subject.release(0xF)
        subject.v[3] = 0xF

        subject.skipKeyEq(3)

        assertThat(subject.keys[0xF]).isFalse()
        assertThat(subject.pc).isEqualTo(0x200)
    }

}
