package it.ldsoftware.k8bit.hardware.predefined

import it.ldsoftware.k8bit.Constants

/**
 * Processor memory, used to store program data (from address 0x200) and the rest is reserved for the
 * interpreter
 */
class Memory {
    companion object {
        const val totalSpace = 4096
        const val programStart = 0x200
    }

    private val backingArray = Array(totalSpace) { Constants.EMPTY }

    operator fun get(pos: Int) = backingArray[pos]

    operator fun set(pos: Int, value: Int) {
        backingArray[pos] = value
    }

    /**
     * Loads data into the reserverd memory
     */
    fun loadReserved(start: Int, data: Array<Int>) {
        for ((j, i) in (start until data.size).withIndex()) {
            backingArray[i] = data[j]
        }
    }

    fun load(program: Array<Int>) {
        for ((i, v) in program.withIndex()) {
            backingArray[i + programStart] = v
        }
    }

    fun subProgram(from: Int, to: Int): Array<Int> =
        backingArray.drop(from + programStart).take(to - from).toTypedArray()

    fun subSet(from: Int, to: Int): Array<Int> = backingArray.drop(from).take(to - from).toTypedArray()
}
