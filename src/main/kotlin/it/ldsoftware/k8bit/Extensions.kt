package it.ldsoftware.k8bit

fun Int.expand(): List<Int> = (0..7)
    .map {
        (this and (1 shl it)) shr it
    }
    .reversed()
