package no.kristiania.alphonsesantoro.chessbattle.game.pieces

import jstockfish.Uci
import no.kristiania.alphonsesantoro.chessbattle.R
import no.kristiania.alphonsesantoro.chessbattle.game.Color
import no.kristiania.alphonsesantoro.chessbattle.game.Coordinate
import no.kristiania.alphonsesantoro.chessbattle.game.Game
import no.kristiania.alphonsesantoro.chessbattle.util.asInt

abstract class Piece(
    val resource: Int,
    val color: Color,
    val tag: Char,
    var coordinate: Coordinate,
    var promoteRank: Int? = null,
    private var blockingPiece: Boolean = false
) {

    init {
        promoteRank = if (color == Color.WHITE) 8 else 1
    }

    internal fun isLegalSquare(show: Boolean, coordinate: Coordinate?, promotePiece: Char? = null): Boolean {
        if(coordinate == null) return false
        val foregroundId = if (show) R.drawable.ic_square_suggestion else R.drawable.ic_blank_tile
        val square = Game.board[coordinate]!!
        if(square.coordinate == this.coordinate) return true // Same square, do nothing
        var move = "${this.coordinate.name}${coordinate.name}"
        if(promotePiece != null) move += promotePiece
        if(Uci.isLegal(move)){
            square.foregroundResource = foregroundId
            return true
        }
        return false
    }

    fun showDiagonals(show: Boolean) {
        blockingPiece = false
        // Find possible moves northeast
        var col = coordinate.name[0]
        var row = coordinate.name[1].asInt
        while (col <= 'h' && row <= 8){
            if (!isLegalSquare(show, Coordinate.fromString("${col++}${row++}"))) break
        }
        blockingPiece = false
        // Find possible moves southwest
        col = coordinate.name[0]
        row = coordinate.name[1].asInt
        while (col >= 'a' && row >= 1){
            if (!isLegalSquare(show, Coordinate.fromString("${col--}${row--}"))) break
        }

        blockingPiece = false
        // Find possible moves northwest
        col = coordinate.name[0]
        row = coordinate.name[1].asInt
        while (col >= 'a' && row <= 8){
            if (!isLegalSquare(show, Coordinate.fromString("${col--}${row++}"))) break
        }

        blockingPiece = false
        // Find possible moves southeast
        col = coordinate.name[0]
        row = coordinate.name[1].asInt
        while (col <= 'h' && row >= 1){
            if (!isLegalSquare(show, Coordinate.fromString("${col++}${row--}"))) break
        }
    }

    fun showVerticalAndHorizontal(show: Boolean){
        blockingPiece = false

        // Find possible moves up
        for (c in coordinate.name[1].asInt..8) {
            if (!isLegalSquare(show, Coordinate.fromString("${coordinate.name[0]}$c"))) break
        }

        blockingPiece = false
        // Find possible moves down
        for (c in coordinate.name[1].asInt downTo 1) {
            if (!isLegalSquare(show, Coordinate.fromString("${coordinate.name[0]}$c"))) break
        }

        blockingPiece = false
        // Find possible moves left
        for (c in coordinate.name[0]..'h') {
            if (!isLegalSquare(show, Coordinate.fromString("$c${coordinate.name[1]}"))) break
        }

        blockingPiece = false
        // Find possible moves right
        for (c in coordinate.name[0] downTo 'a') {
            if (!isLegalSquare(show, Coordinate.fromString("$c${coordinate.name[1]}"))) break
        }
    }

    fun promote(promotePiece: Char?): Piece {
        return when(promotePiece){
            'Q' -> {
                val resource = if(color == Color.WHITE) R.drawable.ic_white_queen else R.drawable.ic_white_queen
                Queen(resource, color, tag, coordinate)
            }
            'R' -> {
                val resource = if(color == Color.WHITE) R.drawable.ic_white_rook else R.drawable.ic_black_rook
                Rook(resource, color, tag, coordinate)
            }
            'N' -> {
                val resource = if(color == Color.WHITE) R.drawable.ic_white_knight else R.drawable.ic_black_knight
                Knight(resource, color, tag, coordinate)
            }
            'B' -> {
                val resource = if(color == Color.WHITE) R.drawable.ic_white_bishop else R.drawable.ic_black_bishop
                Bishop(resource, color, tag, coordinate)
            }
            else -> this
        }
    }

    abstract fun showPossibleMoves(show: Boolean)
}