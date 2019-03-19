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
    var legalMovesCount: Int = 0
) {
    private var blockingPiece: Boolean = false

    init {
        promoteRank = if (color == Color.WHITE) 8 else 1
    }

    internal fun isLegalSquare(show: Boolean, coordinate: Coordinate?, promotePiece: Char? = null): Boolean {
        if (coordinate == null) return false
        val square = Game.board[coordinate]!!
        square.showForeground = show
        val foregroundId = if (show) R.drawable.ic_square_suggestion else R.drawable.ic_blank_tile
        if (square.coordinate == this.coordinate) return true // Same square, do nothing
        var move = "${this.coordinate.name}${coordinate.name}"
        if (promotePiece != null) move += promotePiece
        if (Uci.isLegal(move)) {
            legalMovesCount++
            square.foregroundResource = foregroundId
            return true
        }
        return false
    }

    /**
     * Shows possible squares this piece can move to.
     * If check is set, we loop until a blocking piece, but check for checks
     */
    fun loopDiagonals(show: Boolean, check: Boolean) {
        blockingPiece = false
        // Find possible moves northeast
        var col = coordinate.name[0]
        var row = coordinate.name[1].asInt
        while (col <= 'h' && row <= 8) {
            val coord = Coordinate.fromString("${col++}${row++}")
            if (check) {
                isInCheck(coord)
                if (blockingPiece) break
            } else {
                if (!isLegalSquare(show, coord)) break
            }
        }
        blockingPiece = false
        // Find possible moves southwest
        col = coordinate.name[0]
        row = coordinate.name[1].asInt
        while (col >= 'a' && row >= 1) {
            val coord = Coordinate.fromString("${col--}${row--}")
            if (check) {
                isInCheck(coord)
                if (blockingPiece) break
            } else {
                if (!isLegalSquare(show, coord)) break
            }
        }

        blockingPiece = false
        // Find possible moves northwest
        col = coordinate.name[0]
        row = coordinate.name[1].asInt
        while (col >= 'a' && row <= 8) {
            val coord = Coordinate.fromString("${col--}${row++}")
            if (check) {
                isInCheck(coord)
                if (blockingPiece) break
            } else {
                if (!isLegalSquare(show, coord)) break
            }
        }

        blockingPiece = false
        // Find possible moves southeast
        col = coordinate.name[0]
        row = coordinate.name[1].asInt
        while (col <= 'h' && row >= 1) {
            val coord = Coordinate.fromString("${col++}${row--}")
            if (check) {
                isInCheck(coord)
                if (blockingPiece) break
            } else {
                if (!isLegalSquare(show, coord)) break
            }
        }
    }

    fun loopHorizontalAndVerticals(show: Boolean, check: Boolean) {
        blockingPiece = false

        // Find possible moves up
        for (c in coordinate.name[1].asInt..8) {
            val coord = Coordinate.fromString("${coordinate.name[0]}$c")
            if (check) {
                isInCheck(coord)
                if (blockingPiece) break
            } else {
                if (!isLegalSquare(show, coord)) break
            }
        }

        blockingPiece = false
        // Find possible moves down
        for (c in coordinate.name[1].asInt downTo 1) {
            val coord = Coordinate.fromString("${coordinate.name[0]}$c")
            if (check) {
                isInCheck(coord)
                if (blockingPiece) break
            } else {
                if (!isLegalSquare(show, coord)) break
            }
        }

        blockingPiece = false
        // Find possible moves left
        for (c in coordinate.name[0]..'h') {
            val coord = Coordinate.fromString("$c${coordinate.name[1]}")
            if (check) {
                isInCheck(coord)
                if (blockingPiece) break
            } else {
                if (!isLegalSquare(show, coord)) break
            }
        }

        blockingPiece = false
        // Find possible moves right
        for (c in coordinate.name[0] downTo 'a') {
            val coord = Coordinate.fromString("$c${coordinate.name[1]}")
            if (check) {
                isInCheck(coord)
                if (blockingPiece) break
            } else {
                if (!isLegalSquare(show, coord)) break
            }
        }
    }

    internal fun isInCheck(coord: Coordinate?) {
        if (coord != this.coordinate) {
            if (Game.board[coord]?.piece != null) blockingPiece = true
            if (Game.board[coord]?.piece is King && Game.board[coord]?.piece?.color != color) {
                Game.isChecked = true
            }
        }
    }

    fun promote(promotePiece: Char?): Piece {
        return when (promotePiece) {
            'Q' -> {
                val resource = if (color == Color.WHITE) R.drawable.ic_white_queen else R.drawable.ic_black_queen
                Queen(resource, color, tag, coordinate)
            }
            'R' -> {
                val resource = if (color == Color.WHITE) R.drawable.ic_white_rook else R.drawable.ic_black_rook
                Rook(resource, color, tag, coordinate)
            }
            'N' -> {
                val resource = if (color == Color.WHITE) R.drawable.ic_white_knight else R.drawable.ic_black_knight
                Knight(resource, color, tag, coordinate)
            }
            'B' -> {
                val resource = if (color == Color.WHITE) R.drawable.ic_white_bishop else R.drawable.ic_black_bishop
                Bishop(resource, color, tag, coordinate)
            }
            else -> this
        }
    }

    internal open fun showPossibleMoves(show: Boolean, check: Boolean) {
        legalMovesCount = 0
    }

    companion object {
        fun getPiece(char: Char, coordinate: Coordinate) : Piece? {
            return when(char){
                'p' -> Pawn(R.drawable.ic_black_pawn, Color.fromChar(char), char, coordinate)
                'r' -> Rook(R.drawable.ic_black_rook, Color.fromChar(char), char, coordinate)
                'b' -> Bishop(R.drawable.ic_black_bishop, Color.fromChar(char), char, coordinate)
                'n' -> Knight(R.drawable.ic_black_knight, Color.fromChar(char), char, coordinate)
                'q' -> Queen(R.drawable.ic_black_queen, Color.fromChar(char), char, coordinate)
                'k' -> King(R.drawable.ic_black_king, Color.fromChar(char), char, coordinate)
                'P' -> Pawn(R.drawable.ic_white_pawn, Color.fromChar(char), char, coordinate)
                'R' -> Rook(R.drawable.ic_white_rook, Color.fromChar(char), char, coordinate)
                'B' -> Bishop(R.drawable.ic_white_bishop, Color.fromChar(char), char, coordinate)
                'N' -> Knight(R.drawable.ic_white_knight, Color.fromChar(char), char, coordinate)
                'Q' -> Queen(R.drawable.ic_white_queen, Color.fromChar(char), char, coordinate)
                'K' -> King(R.drawable.ic_white_king, Color.fromChar(char), char, coordinate)
                else -> null
            }
        }
    }
}