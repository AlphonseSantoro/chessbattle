package no.kristiania.alphonsesantoro.chessbattle.game

import com.google.gson.JsonObject
import jstockfish.Uci
import no.kristiania.alphonsesantoro.chessbattle.R
import no.kristiania.alphonsesantoro.chessbattle.game.pieces.Piece
import java.lang.IllegalArgumentException

data class Square(
    val coordinate: Coordinate,
    var piece: Piece? = null,
    val emptySquareRes: Int = R.drawable.ic_blank_tile,
    var foregroundResource: Int = R.drawable.ic_blank_tile,
    var showForeground: Boolean = false
)

enum class Color {
    WHITE, BLACK;

    val fen: String
        get() = if (this == WHITE) "w" else "b"

    companion object {
        val random: Color
            get() = arrayOf(WHITE, BLACK).random()

        fun fromFen(): Color {
            return if (Uci.fen().split(" ")[1] == "w") WHITE else BLACK
        }

        fun fromChar(char: Char): Color {
            return if (char.isUpperCase()) WHITE else BLACK
        }
    }
}

enum class GameMode {
    STOCKFISH, TWO_PLAYER, LIVE, UNKNOWN
}

enum class GameStatus(val result: Int) {
    WHITE_MATE(0), BLACK_MATE(1), STALE_MATE(2), DRAW(3), RESIGNED(4), INPROGRESS(-1);

    companion object {
        fun fromInt(int: Int): GameStatus {
            return when (int) {
                0 -> WHITE_MATE
                1 -> BLACK_MATE
                2 -> STALE_MATE
                3 -> DRAW
                4 -> RESIGNED
                else -> INPROGRESS
            }
        }
    }
}

enum class Coordinate {
    a1, a2, a3, a4, a5, a6, a7, a8,
    b1, b2, b3, b4, b5, b6, b7, b8,
    c1, c2, c3, c4, c5, c6, c7, c8,
    d1, d2, d3, d4, d5, d6, d7, d8,
    e1, e2, e3, e4, e5, e6, e7, e8,
    f1, f2, f3, f4, f5, f6, f7, f8,
    g1, g2, g3, g4, g5, g6, g7, g8,
    h1, h2, h3, h4, h5, h6, h7, h8;

    companion object {
        fun fromString(coordinate: String): Coordinate? {
            return try {
                Coordinate.valueOf(coordinate)
            } catch (e: IllegalArgumentException) {
                null
            }
        }
    }
}

val JsonObject.toMap: Map<String, String>
    get() {
        val map = mutableMapOf<String, String>()
        this.entrySet().forEach {
            map[it.key] = it.value.asString
        }
        return map
    }