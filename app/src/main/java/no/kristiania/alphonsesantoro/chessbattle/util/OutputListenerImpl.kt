package no.kristiania.alphonsesantoro.chessbattle.util

import jstockfish.OutputListener

object OutputListenerImpl : OutputListener {

    val output : MutableList<String> = mutableListOf()

    override fun onOutput(output: String?) {
        if (output != null) this.output.add(output)
    }

}