package it.ldsoftware.k8bit.hardware.predefined

import it.ldsoftware.k8bit.hardware.OutputSystem

class DummyOutputSystem: OutputSystem {
    override fun beep() {
        println("Beep")
    }
}
