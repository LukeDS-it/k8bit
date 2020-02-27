package it.ldsoftware.k8bit

import it.ldsoftware.k8bit.hardware.Graphics
import it.ldsoftware.k8bit.hardware.OutputSystem
import it.ldsoftware.k8bit.hardware.predefined.DummyOutputSystem
import it.ldsoftware.k8bit.hardware.predefined.GraphicMemory
import it.ldsoftware.k8bit.hardware.predefined.Memory
import kotlin.random.Random

class Chip8Processor(
    private val random: Random = Random.Default,
    private val out: OutputSystem = DummyOutputSystem(),
    internal val memory: Memory = Memory(),
    internal val gfx: Graphics = GraphicMemory(),
    private val limitSpeed: Boolean = false
) {

    internal val v = Array(16) { Constants.EMPTY }
    internal val keys = Array(16) { false }
    internal val stack = Array(16) { Constants.EMPTY }

    internal var i = 0
    internal var sp = 0
    internal var pc = Memory.programStart
    internal var delayTimer = 0
    internal var soundTimer = 0

    init {
        memory.loadReserved(0, Constants.CHARACTERS)
    }

    fun press(key: Int) {
        keys[key] = true
    }

    fun release(key: Int) {
        keys[key] = false
    }

    fun load(program: Array<Int>) {
        memory.load(program)
    }

    fun cycle() {
        val start = System.currentTimeMillis()

        val opcode = getOpCode()

        val shouldIncrement = execute(opcode)

        handleTimers()

        if (shouldIncrement)
            pc += 2

        if (limitSpeed) {
            val total = System.currentTimeMillis() - start
            Thread.sleep(17 - total) // 60 cycles per second :)
        }
    }

    fun handleTimers() {
        if (delayTimer > 0) {
            delayTimer--
        }
        if (soundTimer > 0) {
            if (soundTimer == 1) {
                out.beep()
            }
            soundTimer--
        }

    }

    fun getOpCode(): Int = memory[pc] shl 8 or memory[pc + 1]

    /**
     * Executes the opcode and returns true if the PC should be incremented, false otherwise
     */
    private fun execute(opcode: Int): Boolean {
        val remaining = opcode and 0x0FFF
        return when (opcode and 0xF000 shr 12) {
            0x0 -> zeroOps(remaining)
            0x1 -> jumpa(remaining)
            0x2 -> call(remaining)
            0x3 -> eq(remaining)
            0x4 -> neq(remaining)
            0x5 -> eqreg(remaining)
            0x6 -> store(remaining)
            0x7 -> add(remaining)
            0x8 -> math(remaining)
            0x9 -> neqreg(remaining)
            0xA -> memp(remaining)
            0xB -> jumpv(remaining)
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
    internal fun jumpa(remaining: Int): Boolean {
        pc = remaining
        return false
    }

    /**
     * 0x2NNN
     */
    internal fun call(remaining: Int): Boolean {
        stack[sp] = pc
        sp++
        return jumpa(remaining)
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
            0x0 -> cp(x, y)
            0x1 -> or(x, y)
            0x2 -> and(x, y)
            0x3 -> xor(x, y)
            0x4 -> sum(x, y)
            0x5 -> sub(x, y)
            0x6 -> srl(x)
            0x7 -> subi(x, y)
            0xE -> sll(x)
            else -> println("invalid opcode $remaining")
        }
        return true
    }

    internal fun cp(x: Int, y: Int) {
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

    internal fun subi(x: Int, y: Int) {
        val nv = v[y] - v[x]
        v[0xF] = if (nv < 0) 0 else 1
        v[x] = nv and 0xFFFF
    }

    /**
     * Note: in the original specification the function would shift y into x.
     * All new implementations I found ignore y instead.
     */
    internal fun sll(x: Int) {
        v[0xF] = (v[x] shr 15) and 0x1
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
    internal fun memp(remaining: Int): Boolean {
        i = remaining
        return true
    }

    /**
     * 0xBNNN
     */
    internal fun jumpv(remaining: Int): Boolean {
        pc = remaining + v[0]
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
        val sprite = memory.subSet(i, i + n)
        v[0xF] = gfx.draw(v[x], v[y], sprite)
        return true
    }

    /**
     * 0xEX--
     */
    private fun keys(remaining: Int): Boolean {
        when (remaining and 0x0FF) {
            0x9E -> skipKeyEq(getPos0(remaining))
            0xA1 -> skipKeyNeq(getPos0(remaining))
        }
        return true
    }

    internal fun skipKeyEq(reg: Int) {
        if (keys[v[reg]]) pc += 2
    }

    internal fun skipKeyNeq(reg: Int) {
        if (!keys[v[reg]]) pc += 2
    }

    /**
     * FX--
     */
    private fun mixed(remaining: Int): Boolean {
        return when (remaining and 0x0FF) {
            0x07 -> storeDelay(getPos0(remaining))
            0x0A -> waitKey(getPos0(remaining))
            0x15 -> recoverDelay(getPos0(remaining))
            0x18 -> setSound(getPos0(remaining))
            0x1E -> addi(getPos0(remaining))
            0x29 -> charOf(getPos0(remaining))
            0x33 -> bcd(getPos0(remaining))
            0x55 -> storeAll(getPos0(remaining))
            0x65 -> loadAll(getPos0(remaining))
            else -> return true
        }
    }

    internal fun storeDelay(x: Int): Boolean {
        v[x] = delayTimer
        return true
    }

    internal fun waitKey(x: Int): Boolean {
        var pressed = false
        keys.forEachIndexed { index, b ->
            if (b) {
                pressed = true
                v[x] = index
            }
        }
        return pressed
    }

    internal fun recoverDelay(x: Int): Boolean {
        delayTimer = v[x]
        return true
    }

    internal fun setSound(x: Int): Boolean {
        soundTimer = v[x]
        return true
    }

    internal fun addi(x: Int): Boolean {
        i = (i + v[x]) and 0xFFFF
        return true
    }

    internal fun charOf(x: Int): Boolean {
        i = v[x] * 5
        return true
    }

    internal fun bcd(x: Int): Boolean {
        memory[i] = v[x] / 100
        memory[i + 1] = (v[x] / 10) % 10
        memory[i + 2] = (v[x] % 100) % 10
        return true
    }

    internal fun storeAll(x: Int): Boolean {
        for (iter in 0..x) {
            memory[i + iter] = v[iter]
        }
        i += x + 1
        return true
    }

    internal fun loadAll(x: Int): Boolean {
        for (iter in 0..x) {
            v[iter] = memory[i + iter]
        }
        i += x + 1
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
