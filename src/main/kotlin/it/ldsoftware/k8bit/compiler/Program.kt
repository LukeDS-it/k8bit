package it.ldsoftware.k8bit.compiler

import it.ldsoftware.k8bit.Constants

/**
 * This class provides a program editor that can be used to build programs without writing hex codes.
 */
class Program {

    /**
     * `0x00E0`, clear the screen
     */
    fun cls(): Program = addOperation(0x00, 0xE0)

    /**
     * `0x00EE`, returns from a subroutine, the equivalent of ending a function
     */
    fun ret(): Program = addOperation(0x00, 0xEE)

    /**
     * `0x1NNN`, jump to address NNN
     *
     * In the next cycle the execution will resume from address NNN
     *
     * @param address the memory address where to jump
     */
    fun jumpa(address: Int): Program = addOperation(0x10 + (address and 0xF00), address and 0x0FF)

    /**
     * `0x2NNN` calls subroutine at address NNN
     *
     * The equivalent of calling a function, the next cycle the execution will resume from NNN and usually the
     * subroutine will end with the ret() call
     *
     * @param address the memory address where the subroutine begins
     */
    fun call(address: Int): Program = addOperation(0x20 + (address and 0xF00), address and 0x0FF)

    /**
     * `0x3XNN` skips the next instruction if the value of register X equals NN
     *
     * @param register the register
     * @param value    the value to check against
     */
    fun eq(register: Int, value: Int): Program = addOperation(0x30 + (register and 0xF00), value)

    /**
     * `0x4XNN` skips the next instruction if the value of register X is not equal to NN
     *
     * @param register the register
     * @param value    the value to check against
     */
    fun neq(register: Int, value: Int): Program = addOperation(0x40 + (register and 0xF00), value)

    /**
     * `0x5XY0` skips the next instruction if the value of registers X and Y are equal
     *
     * @param x the first register
     * @param y the second register
     */
    fun eqreg(x: Int, y: Int): Program = addOperation(0x50 + x, y shl 4)

    /**
     * `0x6XNN` stores value 0xNN in the register X
     *
     * @param register the register where to store the value
     * @param value    the value to store
     */
    fun put(register: Int, value: Int): Program = addOperation(0x60 + register, value)

    /**
     * `0x7XNN` add the value NN to register X
     */
    fun add(register: Int, value: Int): Program = addOperation(0x70 + register, value)

    /**
     * `0x8XY0` store the value of Y into X
     *
     * @param from the register to copy from
     * @param to   the register to copy to
     */
    fun cp(from: Int, to: Int): Program = addOperation(0x80 + to, from shl 4)

    /**
     * `0x8XY1` store in X the value of X | Y
     *
     * @param x the first register
     * @param y the second register
     */
    fun or(x: Int, y: Int): Program = addOperation(0x80 + x, (y shl 4) + 0x1)

    /**
     * `0x8XY2` store in X the value of X & Y
     *
     * @param x the first register
     * @param y the second register
     */
    fun and(x: Int, y: Int): Program = addOperation(0x80 + x, (y shl 4) + 0x2)

    /**
     * `0x8XY3` store in X the value of X ^ Y
     *
     * @param x the first register
     * @param y the second register
     */
    fun xor(x: Int, y: Int): Program = addOperation(0x80 + x, (y shl 4) + 0x3)

    /**
     * `0x8XY4` store in X the value of X + Y, additionally the register 0xF will contain the carry out
     *
     * @param x the first register
     * @param y the second register
     */
    fun sum(x: Int, y: Int): Program = addOperation(0x80 + x, (y shl 4) + 0x4)

    /**
     * `0x8XY5` store in X the value of X - Y, additionally, the register 0xF will contain the borrow
     * (1 if borrow did not occur, 0 otherwise)
     *
     * @param x the first register
     * @param y the second register
     */
    fun sub(x: Int, y: Int): Program = addOperation(0x80 + x, (y shl 4) + 0x5)

    /**
     * `0x8XY6` store in X the value of X >> Y
     *
     * @param x the first register
     * @param y the second register
     */
    fun srl(x: Int, y: Int): Program = addOperation(0x80 + x, (y shl 4) + 0x6)

    /**
     * `0x8XY7` store in X the value of Y - X, additionally, the register 0xF will contain the borrow
     * (1 if borrow did not occur, 0 otherwise)
     *
     * @param x the first register
     * @param y the second register
     */
    fun subi(x: Int, y: Int): Program = addOperation(0x80 + x, (y shl 4) + 0x7)

    /**
     * `0x8XYE` store in X the value of X << Y
     *
     * @param x the first register
     * @param y the second register
     */
    fun sll(x: Int, y: Int): Program = addOperation(0x80 + x, (y shl 4) + 0xE)

    /**
     * `0x9XY0` skips the next instruction if the value of registers X and Y are not equal
     *
     * @param x the first register
     * @param y the second register
     */
    fun neqreg(x: Int, y: Int): Program = addOperation(0x90 + x, y shl 4)

    /**
     * `0xANNN` sets I to NNN
     *
     * I usually stores a memory address
     *
     * @param address the address to put into the registry I
     */
    fun memp(address: Int): Program = addOperation(0xA0 + (address and 0xF00), address and 0x0FF)

    /**
     * `0xBNNN` jump to address NNN plus the value of register 0
     *
     * @param address the memory address to jump to
     */
    fun jumpv(address: Int): Program = addOperation(0xB0 + (address and 0xF00), address and 0x0FF)

    /**
     * `0xCXNN` sets register x to a random value, interpolated with mask n, e.g.:
     *
     *     random = 00101011
     *     mask   = 00110001
     *     result = 00100001
     *
     * @param register the register into which the result will be stored
     * @param mask     the mask for interpolation
     */
    fun rnd(register: Int, mask: Int): Program = addOperation(0xC0 + register, mask)

    /**
     * `0xDXYN` Draws an 8xN sprite in position X, Y. The sprite will be fetched from the memory address I until I + N
     *
     * @param x     top left x position of the sprite
     * @param y     top left y position of the sprite
     * @param lines number of lines to draw. This will specify how many memory addresses will be read from the
     * program memory, starting from I
     */
    fun draw(x: Int, y: Int, lines: Int): Program = addOperation(0xD0 + x, (y shl 4) + lines)

    /**
     * `0xEX9E` Skips the next instruction if the key stored in register x is pressed
     *
     * @param register the register that contains the key to check against
     */
    fun skipKeyEq(register: Int): Program = addOperation(0xE0 + register, 0x9E)

    /**
     * `0xEXA1` Skips the next instruction if the key stored in register x is not pressed
     *
     * @param register the register that contains the key to check against
     */
    fun skipKeyNeq(register: Int): Program = addOperation(0xE0 + register, 0xA1)

    /**
     * `0xFX07` stores the current delay in register X
     *
     * @param register the register
     */
    fun storeDly(register: Int): Program = addOperation(0xF0 + register, 0x07)

    /**
     * `0xFX0A` waits for a keypress (pc does not increment) and stores it in the given register
     *
     * @param register the register
     */
    fun key(register: Int): Program = addOperation(0xF0 + register, 0x0A)

    /**
     * `0xFX15` restores the delay from register X
     *
     * @param register the register
     */
    fun delay(register: Int): Program = addOperation(0xF0 + register, 0x15)

    /**
     * `0xFX18` Sets the sound timer to the value stored in the register X
     *
     * @param register the register where the information is stored
     */
    fun setSound(register: Int): Program = addOperation(Operation(0xF0 + register, 0x18))

    /**
     * `0xFX1E` adds the contents of register x to i, carry out is recorded in register 0xF
     *
     * @param register the register
     */
    fun addi(register: Int): Program = addOperation(0xF0 + register, 0x1E)

    /**
     * `0xFX29` loads into I the location of t he sprite for the character in register X
     *
     * @param register the register
     */
    fun chr(register: Int): Program = addOperation(0xF0 + register, 0x29)

    /**
     * `0xFX33` stores the BCD representation of value in register X into I, I + 1 and I + 2. E.g.:
     *
     *     value      = 129
     *
     *     digits     =    1    2    9
     *     BCD        = 0001 0010 1001
     *     mem[I]     = 0001
     *     mem[I + 1] = 0010
     *     mem[I + 2] = 1001
     *
     * @param register the register
     */
    fun bcd(register: Int): Program = addOperation(0xF0 + register, 0x33)

    /**
     * `0xFX55` backups registers from 0 to X in memory, from address I. At the end of the process, I has
     * a value of I + X + 1
     *
     * @param register the register
     */
    fun bkp(register: Int): Program = addOperation(0xF0 + register, 0x55)

    /**
     * `0xFX65` restores the values in registers 0 to X from memory starting from address I. At the end
     * of the process, I has a value of I + X + 1
     *
     * @param register the register
     */
    fun rest(register: Int): Program = addOperation(0xF0 + register, 0x65)

    /**
     * Does nothing. Technically not a recognized command.
     */
    fun nop(): Program = addOperation(0xF0, 0x00)

    /**
     * Draws a single character on screen. This is **not** an opcode, but a shortcut, so be sure to backup
     * all your important variables before making this call.
     *
     * @param x    the x coordinate of the character
     * @param y    the y coordinate of the character
     * @param char the character (0 - F) to draw
     */
    fun drawChar(x: Int, y: Int, char: Int): Program = put(0, char).chr(0).draw(x, y, Constants.CHAR_HEIGHT)

    private fun addOperation(op: Operation): Program {
        code.add(op.part1)
        code.add(op.part2)
        operations.add(op)
        return this
    }

    private fun addOperation(part1: Int, part2: Int): Program = addOperation(Operation(part1, part2))

    /**
     * Returns the program in a format that can be interpreted by the Chip-8 interpreter
     */
    fun compiled(): Array<Int> = code.toTypedArray()

    private val code = ArrayList<Int>()
    private val operations = ArrayList<Operation>()
}
