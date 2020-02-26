package it.ldsoftware.k8bit

class Memory {
    companion object {
        const val totalSpace = 4096
        const val programStart = 0x200
    }

    private val backingArray = Array(totalSpace) { Constants.EMPTY }

    operator fun invoke(pos: Int): Int = backingArray[pos]

    fun load(program: Array<Int>) {
        for ((i, v) in program.withIndex()) {
            backingArray[i + programStart] = v
        }
    }

    fun subset(from: Int, to: Int): Array<Int> = backingArray.drop(from + programStart).take(to - from).toTypedArray()
}
