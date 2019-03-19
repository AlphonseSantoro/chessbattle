package no.kristiania.alphonsesantoro.chessbattle.util

val Char.asInt : Int
    get() = this.toString().toInt()

val Any?.asDouble: Double
    get() = this.toString().toDouble()