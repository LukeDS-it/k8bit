package it.ldsoftware.k8bit

import it.ldsoftware.k8bit.compiler.Program
import it.ldsoftware.k8bit.hardware.Graphics
import it.ldsoftware.k8bit.hardware.OutputSystem
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test
import org.mockito.Mockito.*
import kotlin.random.Random

class Chip8Spec {

    @Test
    fun `should execute opcode 00E0 and clear the screen`() {
        val dummy = mock(Graphics::class.java)
        val subject = Chip8Processor(gfx = dummy)

        subject.load(arrayOf(0x00, 0xE0))
        subject.cycle()

        assertThat(subject.pc).isEqualTo(0x202)
        verify(dummy).clear()
    }

    @Test
    fun `should execute opcode 00EE and return from a subroutine recovering the PC`() {
        val subject = Chip8Processor()

        subject.load(arrayOf(0x00, 0xEE))
        subject.sp = 1
        subject.stack[0] = 0x230

        subject.cycle()

        assertThat(subject.pc).isEqualTo(0x232)
        assertThat(subject.sp).isEqualTo(0)
    }

    @Test
    fun `should execute opcode 1NNN and jump to address NNN without increasing PC`() {
        val subject = Chip8Processor()

        subject.load(arrayOf(0x12, 0x50))

        subject.cycle()

        assertThat(subject.pc).isEqualTo(0x250)
    }

    @Test
    fun `should execute opcode 2NNN and jump to subroutine at NNN saving the old PC`() {
        val subject = Chip8Processor()

        subject.load(arrayOf(0x22, 0x50))

        subject.cycle()

        assertThat(subject.pc).isEqualTo(0x250)
        assertThat(subject.sp).isEqualTo(1)
        assertThat(subject.stack[0]).isEqualTo(0x200)
    }

    @Test
    fun `should execute opcode 3xNN and skip an instruction if register X is equal to NN`() {
        val subject = Chip8Processor()

        subject.load(arrayOf(0x30, 0x00))

        subject.cycle()

        assertThat(subject.pc).isEqualTo(0x204)
    }

    @Test
    fun `should execute opcode 3xNN and execute next instruction if register X is not equal to NN`() {
        val subject = Chip8Processor()
        subject.v[0] = 3

        subject.load(arrayOf(0x30, 0x00))

        subject.cycle()

        assertThat(subject.pc).isEqualTo(0x202)
    }

    @Test
    fun `should execute opcode 4XNN and skip next instruction if register X is not equal to NN`() {
        val subject = Chip8Processor()
        subject.v[0] = 3

        subject.load(arrayOf(0x40, 0x00))

        subject.cycle()

        assertThat(subject.pc).isEqualTo(0x204)
    }

    @Test
    fun `should execute opcode 4XNN and execute next instruction if register X is equal to NN`() {
        val subject = Chip8Processor()

        subject.load(arrayOf(0x40, 0x00))

        subject.cycle()

        assertThat(subject.pc).isEqualTo(0x202)
    }

    @Test
    fun `should execute opcode 5XY0 and skip next instruction if register X is equal to register Y`() {
        val subject = Chip8Processor()
        subject.v[0] = 1
        subject.v[1] = 1

        subject.load(arrayOf(0x50, 0x10))

        subject.cycle()

        assertThat(subject.pc).isEqualTo(0x204)
    }

    @Test
    fun `should execute opcode 5XY0 and execute next instruction if register X is not equal to register Y`() {
        val subject = Chip8Processor()
        subject.v[0] = 1
        subject.v[1] = 2

        subject.load(arrayOf(0x50, 0x10))

        subject.cycle()

        assertThat(subject.pc).isEqualTo(0x202)
    }

    @Test
    fun `should execute opcode 6XNN and store number NN in register X`() {
        val subject = Chip8Processor()

        subject.load(arrayOf(0x62, 0x50))

        subject.cycle()

        assertThat(subject.pc).isEqualTo(0x202)
        assertThat(subject.v[2]).isEqualTo(0x50)
    }

    @Test
    fun `should execute opcode 7XNN and add the value NN to register VX`() {
        val subject = Chip8Processor()
        subject.v[2] = 2

        subject.load(arrayOf(0x72, 0x50))

        subject.cycle()

        assertThat(subject.pc).isEqualTo(0x202)
        assertThat(subject.v[2]).isEqualTo(0x52)
    }

    @Test
    fun `should execute opcode 8XY0 and copy Y into X`() {
        val subject = Chip8Processor()
        subject.v[1] = 10

        subject.load(arrayOf(0x82, 0x10))

        subject.cycle()

        assertThat(subject.pc).isEqualTo(0x202)
        assertThat(subject.v[2]).isEqualTo(10)
    }

    @Test
    fun `should execute opcode 8XY1 and set X to X or Y`() {
        val subject = Chip8Processor()
        subject.v[0] = 0b0100
        subject.v[1] = 0b0101

        subject.load(arrayOf(0x80, 0x11))

        subject.cycle()

        assertThat(subject.pc).isEqualTo(0x202)
        assertThat(subject.v[0]).isEqualTo(0b0101)
    }

    @Test
    fun `should execute opcode 8XY2 and set X to X and Y`() {
        val subject = Chip8Processor()
        subject.v[0] = 0b0100
        subject.v[1] = 0b0101

        subject.load(arrayOf(0x80, 0x12))

        subject.cycle()

        assertThat(subject.pc).isEqualTo(0x202)
        assertThat(subject.v[0]).isEqualTo(0b0100)
    }

    @Test
    fun `should execute opcode 8XY3 and set X to X xor Y`() {
        val subject = Chip8Processor()
        subject.v[0] = 0b0100
        subject.v[1] = 0b0101

        subject.load(arrayOf(0x80, 0x13))

        subject.cycle()

        assertThat(subject.pc).isEqualTo(0x202)
        assertThat(subject.v[0]).isEqualTo(0b0001)
    }

    @Test
    fun `should execute opcode 8XY4 and add the value of Y to X, with no carry`() {
        val subject = Chip8Processor()
        subject.v[0] = 0b0100
        subject.v[1] = 0b0101

        subject.load(arrayOf(0x80, 0x14))

        subject.cycle()

        assertThat(subject.pc).isEqualTo(0x202)
        assertThat(subject.v[0]).isEqualTo(0b1001)
        assertThat(subject.v[0xF]).isEqualTo(0)
    }

    @Test
    fun `should execute opcode 8XY4 and add the value of Y to X, with carry`() {
        val subject = Chip8Processor()
        subject.v[0] = 0xFFFF
        subject.v[1] = 1

        subject.load(arrayOf(0x80, 0x14))

        subject.cycle()

        assertThat(subject.pc).isEqualTo(0x202)
        assertThat(subject.v[0]).isEqualTo(0)
        assertThat(subject.v[0xF]).isEqualTo(1)
    }

    @Test
    fun `should execute opcode 8XY5 and subtract Y to X without borrow`() {
        val subject = Chip8Processor()
        subject.v[0] = 0xFFFF
        subject.v[1] = 1

        subject.load(arrayOf(0x80, 0x15))

        subject.cycle()

        assertThat(subject.pc).isEqualTo(0x202)
        assertThat(subject.v[0]).isEqualTo(0xFFFE)
        assertThat(subject.v[0xF]).isEqualTo(1)
    }

    @Test
    fun `should execute opcode 8XY5 and subtract Y to X with borrow`() {
        val subject = Chip8Processor()
        subject.v[0] = 0
        subject.v[1] = 1

        subject.load(arrayOf(0x80, 0x15))

        subject.cycle()

        assertThat(subject.pc).isEqualTo(0x202)
        assertThat(subject.v[0]).isEqualTo(0xFFFF)
        assertThat(subject.v[0xF]).isEqualTo(0)
    }

    @Test
    fun `should execute opcode 8XY6 and shift right logic X with set lsb`() {
        val subject = Chip8Processor()
        subject.v[0] = 0b0000000000000101

        subject.load(arrayOf(0x80, 0x16))

        subject.cycle()

        assertThat(subject.pc).isEqualTo(0x202)
        assertThat(subject.v[0]).isEqualTo(0b0000000000000010)
        assertThat(subject.v[0xF]).isEqualTo(1)
    }

    @Test
    fun `should execute opcode 8XY7 and subtract X to Y without borrow`() {
        val subject = Chip8Processor()
        subject.v[0] = 1
        subject.v[1] = 0xFFFF

        subject.load(arrayOf(0x80, 0x17))

        subject.cycle()

        assertThat(subject.pc).isEqualTo(0x202)
        assertThat(subject.v[0]).isEqualTo(0xFFFE)
        assertThat(subject.v[0xF]).isEqualTo(1)
    }

    @Test
    fun `should execute opcode 8XY7 and subtract X to Y with borrow`() {
        val subject = Chip8Processor()
        subject.v[0] = 1
        subject.v[1] = 0

        subject.load(arrayOf(0x80, 0x17))

        subject.cycle()

        assertThat(subject.pc).isEqualTo(0x202)
        assertThat(subject.v[0]).isEqualTo(0xFFFF)
        assertThat(subject.v[0xF]).isEqualTo(0)
    }

    @Test
    fun `should execute opcode 8XYE and shift left logic X with set msb`() {
        val subject = Chip8Processor()
        subject.v[0] = 0b1000000000000101

        subject.load(arrayOf(0x80, 0x1E))

        subject.cycle()

        assertThat(subject.pc).isEqualTo(0x202)
        assertThat(subject.v[0]).isEqualTo(0b0000000000001010)
        assertThat(subject.v[0xF]).isEqualTo(1)
    }

    @Test
    fun `should execute opcode 9XY0 and skip an instruction if two registers are not equal`() {
        val subject = Chip8Processor()
        subject.v[0] = 1
        subject.v[1] = 2

        subject.load(arrayOf(0x90, 0x10))

        subject.cycle()
        assertThat(subject.pc).isEqualTo(0x204)
    }

    @Test
    fun `should execute opcode 9XY0 and execute an instruction if two registers are equal`() {
        val subject = Chip8Processor()
        subject.v[0] = 2
        subject.v[1] = 2

        subject.load(arrayOf(0x90, 0x10))

        subject.cycle()
        assertThat(subject.pc).isEqualTo(0x202)
    }

    @Test
    fun `should execute opcode ANNN and store memory address NNN in register i`() {
        val subject = Chip8Processor()

        subject.load(arrayOf(0xA1, 0x23))

        subject.cycle()
        assertThat(subject.i).isEqualTo(0x123)
    }

    @Test
    fun `should execute opcode BNNN and jump to address NNN + v0`() {
        val subject = Chip8Processor()
        subject.v[0] = 0x005

        subject.load(arrayOf(0xB1, 0x23))

        subject.cycle()
        assertThat(subject.pc).isEqualTo(0x128)
    }

    @Test
    fun `should execute opcode CXNN and set X to a random number with mask of NN`() {
        val rnd = mock(Random::class.java)

        val rndVal = 0b01011101
        val maskNN = 0b01100101
        val result = 0b01000101

        `when`(rnd.nextInt()).thenReturn(rndVal)
        val subject = Chip8Processor(random = rnd)

        subject.load(arrayOf(0xC0, maskNN))

        subject.cycle()
        assertThat(subject.v[0]).isEqualTo(result)
    }

    @Test
    fun `should execute opcode DXYN and draw a sprite at vX, vY with N lines of sprite, with no collisions`() {
        val gfx = mock(Graphics::class.java)
        val x = 0
        val y = 0
        val zero = arrayOf(0xF0, 0x90, 0x90, 0x90, 0xF0)
        `when`(gfx.draw(x, y, zero)).thenReturn(0)

        val subject = Chip8Processor(gfx = gfx)

        subject.load(arrayOf(0xD0, 0x05))

        subject.cycle()
        assertThat(subject.v[0xF]).isEqualTo(0)
        verify(gfx).draw(x, y, zero)
    }

    @Test
    fun `should execute opcode DXYN and draw a sprite at vX, vY with N lines of sprite, with collisions`() {
        val gfx = mock(Graphics::class.java)
        val x = 0
        val y = 0
        val zero = arrayOf(0xF0, 0x90, 0x90, 0x90, 0xF0)
        `when`(gfx.draw(x, y, zero)).thenReturn(1)

        val subject = Chip8Processor(gfx = gfx)

        subject.load(arrayOf(0xD0, 0x05))

        subject.cycle()
        assertThat(subject.v[0xF]).isEqualTo(1)
        verify(gfx).draw(x, y, zero)
    }

    @Test
    fun `should execute opcode EX9E and skip an instruction if a key is pressed`() {
        val subject = Chip8Processor()
        subject.v[0] = 0xC

        subject.load(arrayOf(0xE0, 0x9E))

        subject.press(0xC)
        subject.cycle()

        assertThat(subject.pc).isEqualTo(0x204)
    }

    @Test
    fun `should execute opcode EX9E and not skip an instruction if a key is not pressed`() {
        val subject = Chip8Processor()
        subject.v[0] = 0xC

        subject.load(arrayOf(0xE0, 0x9E))

        subject.press(0xD)
        subject.cycle()

        assertThat(subject.pc).isEqualTo(0x202)
    }

    @Test
    fun `should execute opcode EXA1 and skip an instruction if a key is not pressed`() {
        val subject = Chip8Processor()
        subject.v[0] = 0xC

        subject.load(arrayOf(0xE0, 0xA1))

        subject.press(0xD)
        subject.cycle()

        assertThat(subject.pc).isEqualTo(0x204)
    }

    @Test
    fun `should execute opcode EXA1 and not skip an instruction if a key is pressed`() {
        val subject = Chip8Processor()
        subject.v[0] = 0xC

        subject.load(arrayOf(0xE0, 0xA1))

        subject.press(0xC)
        subject.cycle()

        assertThat(subject.pc).isEqualTo(0x202)
    }

    @Test
    fun `should execute opcode FX07 and save the delay in X`() {
        val subject = Chip8Processor()
        subject.delayTimer = 12

        subject.load(arrayOf(0xF0, 0x07))

        subject.cycle()

        assertThat(subject.pc).isEqualTo(0x202)
        assertThat(subject.v[0]).isEqualTo(12)
    }

    @Test
    fun `should execute opcode FX0A and not increment the PC if the key is not pressed`() {
        val subject = Chip8Processor()

        subject.load(arrayOf(0xF0, 0x0A))

        subject.cycle()
        subject.cycle()
        subject.cycle()

        assertThat(subject.pc).isEqualTo(0x200)
    }

    @Test
    fun `should execute opcode FX0A and increment the PC if the key is pressed`() {
        val subject = Chip8Processor()

        subject.load(arrayOf(0xF1, 0x0A))

        subject.cycle()
        subject.press(0xC)
        subject.cycle()

        assertThat(subject.pc).isEqualTo(0x202)
        assertThat(subject.v[1]).isEqualTo(0xC)
    }

    @Test
    fun `should execute opcode FX15 and set the delay timer to the value of vX`() {
        val subject = Chip8Processor()
        subject.v[1] = 15

        subject.load(arrayOf(0xF1, 0x15))

        subject.cycle()

        assertThat(subject.pc).isEqualTo(0x202)
        assertThat(subject.delayTimer).isEqualTo(14)
    }

    @Test
    fun `should execute opcode FX18 and set the sound timer to the value of vX`() {
        val subject = Chip8Processor()
        subject.v[1] = 15

        subject.load(arrayOf(0xF1, 0x18))

        subject.cycle()

        assertThat(subject.pc).isEqualTo(0x202)
        assertThat(subject.soundTimer).isEqualTo(14)
    }

    @Test
    fun `should execute opcode FX1E and add the value of X to i`() {
        val subject = Chip8Processor()
        subject.v[1] = 5
        subject.i = 4

        subject.load(arrayOf(0xF1, 0x1E))

        subject.cycle()

        assertThat(subject.pc).isEqualTo(0x202)
        assertThat(subject.i).isEqualTo(9)
    }

    @Test
    fun `should execute opcode FX29 and load in i the address of character in vX`() {
        val subject = Chip8Processor()
        subject.v[1] = 1

        subject.load(arrayOf(0xF1, 0x29))

        subject.cycle()

        assertThat(subject.pc).isEqualTo(0x202)
        assertThat(subject.i).isEqualTo(5)
    }

    @Test
    fun `should execute opcode FX33 and store the BCD of register X in i, i+1, i+2`() {
        val subject = Chip8Processor()
        subject.v[1] = 129
        subject.i = 0x050

        subject.load(arrayOf(0xF1, 0x33))

        subject.cycle()

        assertThat(subject.pc).isEqualTo(0x202)
        assertThat(subject.memory[0x50]).isEqualTo(0b0001)
        assertThat(subject.memory[0x51]).isEqualTo(0b0010)
        assertThat(subject.memory[0x52]).isEqualTo(0b1001)
    }

    @Test
    fun `should execute opcode FX55 and backup in memory registers 0 to x starting from i`() {
        val subject = Chip8Processor()
        subject.v[0] = 1
        subject.v[1] = 2
        subject.v[2] = 3
        subject.v[3] = 4
        subject.v[4] = 5
        subject.i = 0x030

        subject.load(arrayOf(0xF4, 0x55))

        subject.cycle()

        assertThat(subject.pc).isEqualTo(0x202)
        assertThat(subject.memory[0x030]).isEqualTo(1)
        assertThat(subject.memory[0x031]).isEqualTo(2)
        assertThat(subject.memory[0x032]).isEqualTo(3)
        assertThat(subject.memory[0x033]).isEqualTo(4)
        assertThat(subject.memory[0x034]).isEqualTo(5)
        assertThat(subject.i).isEqualTo(0x035)
    }

    @Test
    fun `should execute opcode FX65 and restore from memory registers 0 to x starting from i`() {
        val subject = Chip8Processor()
        subject.memory[0x030] = 1
        subject.memory[0x031] = 2
        subject.memory[0x032] = 3
        subject.memory[0x033] = 4
        subject.memory[0x034] = 5
        subject.i = 0x030

        subject.load(arrayOf(0xF4, 0x65))

        subject.cycle()

        assertThat(subject.pc).isEqualTo(0x202)
        assertThat(subject.v[0]).isEqualTo(1)
        assertThat(subject.v[1]).isEqualTo(2)
        assertThat(subject.v[2]).isEqualTo(3)
        assertThat(subject.v[3]).isEqualTo(4)
        assertThat(subject.v[4]).isEqualTo(5)
        assertThat(subject.i).isEqualTo(0x035)
    }

    @Test
    fun `should ignore unknown opcodes`() {
        val subject = Chip8Processor()

        val program = arrayOf(0xF4, 0xFF, 0x00, 0x00, 0x80, 0x08, 0xE0, 0x00)
        subject.load(program)

        val iters = program.size / 2

        for (i in 0 until iters)
            subject.cycle()

        assertThat(subject.pc).isEqualTo(0x200 + program.size)
    }

    @Test
    fun `should provide outputs when the timer ends`() {
        val output = mock(OutputSystem::class.java)
        val subject = Chip8Processor(out = output)

        val program = Program()
            .store(0, 2)
            .setSound(0)
            .cls()
            .compiled()

        subject.load(program)

        subject.cycle() // Loads 2 in v0
        subject.cycle() // Loads v0 in timer count, timer goes to 1
        subject.cycle() // ignores opcode, timer goes to zero

        assertThat(subject.pc).isEqualTo(0x206)
        verify(output).beep()
    }

    @Test
    fun `should limit cycle speed to ~60Hz`() {
        val subject = Chip8Processor(limitSpeed = true)

        val start = System.currentTimeMillis()
        for (i in 0..60)
            subject.cycle()
        val end = System.currentTimeMillis()

        assertThat(end - start).isGreaterThanOrEqualTo(1000)
    }

}
