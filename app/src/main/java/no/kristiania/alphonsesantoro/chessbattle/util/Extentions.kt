package no.kristiania.alphonsesantoro.chessbattle.util

import com.google.gson.Gson
import com.google.gson.JsonParser
import no.kristiania.alphonsesantoro.chessbattle.game.Move

val Char.asInt : Int
    get() = this.toString().toInt()

val Any?.asDouble: Double
    get() = this.toString().toDouble()

val String.fromJson : Move
    get() = Gson().fromJson(this, Move::class.java)