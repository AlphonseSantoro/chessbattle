package no.kristiania.alphonsesantoro.chessbattle.game

import com.google.gson.Gson
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import jstockfish.Uci
import no.kristiania.alphonsesantoro.chessbattle.R
import no.kristiania.alphonsesantoro.chessbattle.game.pieces.Piece
import java.lang.IllegalArgumentException
import java.lang.NullPointerException

data class Square(
    val coordinate: Coordinate,
    var piece: Piece? = null,
    val emptySquareRes: Int = R.drawable.ic_blank_tile,
    var foregroundResource: Int = R.drawable.ic_blank_tile
)

enum class Color {

    WHITE, BLACK;

    val fen: String
        get() = if (this == WHITE) "w" else "b"

    companion object {
        fun fromFen(): Color {
            return if (Uci.fen().split(" ")[1] == "w") WHITE else BLACK
        }

        fun fromString(coordinate: String?): Color? {
            return try {
                Color.valueOf(coordinate!!)
            } catch (e: IllegalArgumentException) {
                null
            } catch (e: NullPointerException) {
                null
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

enum class ResourcePiece(val resource: Int) {
    W_ROOK(R.drawable.ic_white_rook),
    B_ROOK(R.drawable.ic_black_rook),
    W_BISHOP(R.drawable.ic_white_bishop),
    B_BISHOP(R.drawable.ic_black_bishop),
    W_KNIGHT(R.drawable.ic_white_knight),
    B_KNIGHT(R.drawable.ic_black_knight),
    W_KING(R.drawable.ic_white_king),
    B_KING(R.drawable.ic_black_king),
    W_QUEEN(R.drawable.ic_white_queen),
    B_QUEEN(R.drawable.ic_black_queen),
    W_PAWN(R.drawable.ic_white_pawn),
    B_PAWN(R.drawable.ic_black_pawn);
}

data class Move(val fromCoordinate: Coordinate, val toCoordinate: Coordinate, val promotePiece: Char? = null) {
    val toJson: String
        get() = Gson().toJson(this)
}

val JsonObject.toMap: Map<String, String>
    get() {
        val map = mutableMapOf<String, String>()
        this.entrySet().forEach {
            map[it.key] = it.value.asString
        }
        return map
    }