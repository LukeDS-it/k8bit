package it.ldsoftware.k8bit

import kotlin.random.Random

class Chip8Processor(private val random: Random = Random.Default) {

    /**
     * Processor memory, used to store program data (from address 0x200) and the rest is reserved for the
     * interpreter
     */
    internal val memory = Memory()
    internal val gfx = GraphicMemory()
    internal val v = Array(16) { Constants.EMPTY }

    internal var i = 0
    internal var pc = Memory.programStart
    internal var delayTimer = 0
    internal var soundTimer = 0

    internal val stack = Array(16) { Constants.EMPTY }
    internal var sp = 0

    fun load(program: Array<Int>) {
        memory.load(program)
    }

    fun cycle() {
        val start = System.currentTimeMillis()

        val opcode = getOpCode()

        execute(opcode)

        if (delayTimer > 0) {
            delayTimer--
        }
        if (soundTimer > 0) {
            if (soundTimer == 1) {
                println("BEEP")
            }
            soundTimer--
        }

        pc += 2

        val total = System.currentTimeMillis() - start
        Thread.sleep(167 - total) // 60 cycles per second :)
    }

    fun getOpCode(): Int = memory(pc) shl 8 or memory(pc + 1)

    /**
     * Executes the opcode and returns true if the PC should be incremented, false otherwise
     */
    fun execute(opcode: Int): Boolean {
        val remaining = opcode and 0x0FFF
        return when (opcode and 0xF000 shr 12) {
            0x0 -> zeroOps(remaining)
            0x1 -> jump(remaining)
            0x2 -> call(remaining)
            0x3 -> eq(remaining)
            0x4 -> neq(remaining)
            0x5 -> eqreg(remaining)
            0x6 -> store(remaining)
            0x7 -> add(remaining)
            0x8 -> math(remaining)
            0x9 -> neqreg(remaining)
            0xA -> retVal(remaining)
            0xB -> jumprel(remaining)
            0xC -> rnd(remaining)
            0xD -> draw(remaining)
            0xE -> keys(remaining)
            0xF -> mixed(remaining)
            else -> true
        }
    }

    /**
     * 0x0NNN
     */
    private fun zeroOps(remaining: Int): Boolean {
        when (remaining) {
            0x00E0 -> gfx.clear()
            0x00EE -> ret()
            else -> println("Unknown opcode $remaining")
        }
        return true
    }

    internal fun ret() {
        sp--
        pc = stack[sp]
    }

    /**
     * 0x1NNN
     */
    internal fun jump(remaining: Int): Boolean {
        pc = remaining
        return false
    }

    /**
     * 0x2NNN
     */
    internal fun call(remaining: Int): Boolean {
        stack[sp] = pc
        sp++
        jump(remaining)
        return false
    }

    /**
     * 0x3XNN
     */
    internal fun eq(remaining: Int): Boolean {
        val register = getPos0(remaining)
        if (v[register] == remaining and 0x0FF) pc += 2
        return true
    }

    /**
     * 0x4XNN
     */
    internal fun neq(remaining: Int): Boolean {
        val register = getPos0(remaining)
        if (v[register] != remaining and 0x0FF) pc += 2
        return true
    }

    /**
     * 0x5XY0
     */
    internal fun eqreg(remaining: Int): Boolean {
        val reg1 = getPos0(remaining)
        val reg2 = getPos1(remaining)
        if (v[reg1] == v[reg2]) pc += 2
        return true
    }

    /**
     * 0x6XNN
     */
    internal fun store(remaining: Int): Boolean {
        val reg = getPos0(remaining)
        v[reg] = remaining and 0x0FF
        return true
    }

    /**
     * 0x7XNN
     */
    internal fun add(remaining: Int): Boolean {
        val reg = getPos0(remaining)
        v[reg] = (v[reg] + (remaining and 0x0FF)) and 0xFFFF
        return true
    }

    /**
     * 0x8---
     */
    private fun math(remaining: Int): Boolean {
        val x = getPos0(remaining)
        val y = getPos1(remaining)
        when (getPos2(remaining)) {
            0x0 -> mv(x, y)
            0x1 -> or(x, y)
            0x2 -> and(x, y)
            0x3 -> xor(x, y)
            0x4 -> sum(x, y)
            0x5 -> sub(x, y)
            0x6 -> srl(x)
            0x7 -> invSub(x, y)
            0xE -> sll(x)
            else -> println("invalid opcode $remaining")
        }
        return true
    }

    internal fun mv(x: Int, y: Int) {
        v[x] = v[y]
    }

    internal fun or(x: Int, y: Int) {
        v[x] = v[x] or v[y]
    }

    internal fun and(x: Int, y: Int) {
        v[x] = v[x] and v[y]
    }

    internal fun xor(x: Int, y: Int) {
        v[x] = v[x] xor v[y]
    }

    internal fun sum(x: Int, y: Int) {
        val nv = v[x] + v[y]
        v[0xF] = if (nv > 0xFFFF) 1 else 0
        v[x] = nv and 0xFFFF
    }

    internal fun sub(x: Int, y: Int) {
        val nv = v[x] - v[y]
        v[0xF] = if (nv < 0) 0 else 1
        v[x] = nv and 0xFFFF
    }

    /**
     * Note: in the original specification the function would shift y into x.
     * All new implementations I found ignore y instead.
     */
    internal fun srl(x: Int) {
        v[0xF] = v[x] and 0x1
        v[x] = v[x] shr 1
    }

    internal fun invSub(x: Int, y: Int) {
        val nv = v[y] - v[x]
        v[0xF] = if (nv < 0) 0 else 1
        v[x] = nv and 0xFFFF
    }

    /**
     * Note: in the original specification the function would shift y into x.
     * All new implementations I found ignore y instead.
     */
    internal fun sll(x: Int) {
        v[0xF] = (v[x] shr 8) and 0x1
        v[x] = v[x] shl 1 and 0xFFFF
    }

    /**
     * 0x9XY0
     */
    internal fun neqreg(remaining: Int): Boolean {
        val reg1 = getPos0(remaining)
        val reg2 = getPos1(remaining)
        if (v[reg1] != v[reg2]) pc += 2
        return true
    }

    /**
     * 0xANNN
     */
    internal fun retVal(remaining: Int): Boolean {
        i = remaining
        return true
    }

    /**
     * 0xBNNN
     */
    internal fun jumprel(remaining: Int): Boolean {
        pc += remaining + v[0]
        return false
    }

    /**
     * 0xCXNN
     */
    internal fun rnd(remaining: Int): Boolean {
        val reg = getPos0(remaining)
        v[reg] = random.nextInt() and (remaining and 0x0FF)
        return true
    }

    /**
     * 0xDXYN
     */
    internal fun draw(remaining: Int): Boolean {
        val x = getPos0(remaining)
        val y = getPos1(remaining)
        val n = getPos2(remaining)
        val sprite = memory.subset(i, i + n)
        v[0xF] = gfx.draw(v[x], v[y], sprite)
        return true
    }

    /**
     * 0xEX--
     */
    private fun keys(remaining: Int): Boolean {
        when (remaining and 0x0FF) {
            0x9E -> TODO()
            0xA1 -> TODO()
        }
        return true
    }

    /**
     * FX--
     */
    private fun mixed(remaining: Int): Boolean {
        when (remaining and 0x0FF) {
            0x07 -> TODO()
            0x0A -> TODO()
            0x15 -> TODO()
            0x18 -> TODO()
            0x1E -> TODO()
            0x29 -> TODO()
            0x33 -> TODO()
            0x55 -> TODO()
            0x65 -> TODO()
        }
        return true
    }

    private fun getPos0(remaining: Int): Int {
        return remaining and 0xF00 shr 8
    }

    private fun getPos1(remaining: Int): Int {
        return remaining and 0x0F0 shr 4
    }

    private fun getPos2(remaining: Int): Int {
        return remaining and 0x00F
    }

}
