package no.kristiania.alphonsesantoro.chessbattle.game

import androidx.lifecycle.MutableLiveData
import jstockfish.Uci
import jstockfish.Uci.fen
import jstockfish.Uci.position
import no.kristiania.alphonsesantoro.chessbattle.game.pieces.*
import no.kristiania.alphonsesantoro.chessbattle.util.OutputListenerImpl
import no.kristiania.alphonsesantoro.chessbattle.util.asInt

open class Game(val perspective: Color, var gameStatus: GameStatus, val onPieceMovedListener: OnPieceMovedListener?) {

    companion object {
        var board = mutableMapOf<Coordinate, Square>()
        var isChecked = false
        var isPromotion = false

        fun getBoardFromFen(currentFen: String): MutableMap<Coordinate, Square> {
            val board = hashMapOf<Coordinate, Square>()
            var row = 8
            currentFen.split(" ")[0].split("/").forEach {
                var col = 'a'
                it.forEach { c ->
                    // Starts on row 8. A to H
                    if (!c.isLetter()) {
                        for (i in c.asInt downTo 1) {
                            val coordinate = Coordinate.fromString("${col++}$row")!!
                            board[coordinate] = Square(coordinate)
                        }
                    } else {
                        if (col <= 'h') {
                            val coordinate = Coordinate.fromString("${col++}$row")!!
                            val piece = Piece.getPiece(c, coordinate)
                            board[coordinate] = Square(coordinate, piece)
                        }
                    }
                }
                row--
            }
            return board
        }
    }

    internal var gameId: Long = -1
    internal var colorToMove = Color.WHITE
    internal var uciListener = OutputListenerImpl
    internal var selectedPiece: Piece? = null
    internal var lastMovedPiece: Piece? = null
    internal var fromCoordinate: Coordinate? = null
    internal var toCoordinate: Coordinate? = null
    internal var lastMoveNr: Int = 0
    var turn = MutableLiveData<Color>(colorToMove)

    init {
        Uci.position("startpos")
        getBoardFromFen(fen())
        colorToMove = Color.fromFen()
        Uci.setOutputListener(uciListener)
    }

    open fun move(
        fromCoordinate: Coordinate,
        toCoordinate: Coordinate,
        promotePiece: Char? = null
    ): Boolean {
        var validMove = false
        var move = "${fromCoordinate.name}${toCoordinate.name}"
        if (promotePiece != null) move += promotePiece
        if (isPromotion(fromCoordinate, toCoordinate) && promotePiece == null) {
            isPromotion = true
            this.fromCoordinate = fromCoordinate
            this.toCoordinate = toCoordinate
            return true
        } else {
            isPromotion = false
        }
        if (colorToMove == board[fromCoordinate]?.piece?.color && position("fen ${fen()} moves $move")) {
            val fenTokens = Uci.fen().split(" ")
            castle(fenTokens[2], fromCoordinate, toCoordinate)
            enPassant(fenTokens[3], fromCoordinate, toCoordinate)
            movePiece(fromCoordinate, toCoordinate, promotePiece)
            onPieceMovedListener?.onPieceMoved(gameId, colorToMove, move)
            lastMovedPiece = board[toCoordinate]!!.piece
            colorToMove = Color.fromFen()
            turn.postValue(colorToMove)
            validMove = true
        }
        selectedPiece = null
        return validMove
    }

    private fun isPromotion(fromCoordinate: Coordinate, toCoordinate: Coordinate): Boolean {
        return board[fromCoordinate]!!.piece is Pawn && board[fromCoordinate]!!.piece!!.promoteRank == toCoordinate.name[1].asInt
    }

    private fun enPassant(enPassantPos: String, fromCoordinate: Coordinate, toCoordinate: Coordinate) {
        if (enPassantPos != "-" && board[fromCoordinate]!!.piece is Pawn) {
            val moveDir = if (colorToMove == Color.WHITE) 1 else -1
            val capturePieceCoord = "${toCoordinate.name[0]}${toCoordinate.name[1] - 1 * moveDir}"
            board[Coordinate.valueOf(capturePieceCoord)]!!.piece = null
        }
    }

    private fun castle(canCastle: String, fromCoordinate: Coordinate, toCoordinate: Coordinate) {
        val movePos = "${fromCoordinate.name}${toCoordinate.name}"
        if (colorToMove == Color.WHITE && !canCastle.contains(Regex("[QK]")) ||
            colorToMove == Color.BLACK && !canCastle.contains(Regex("[kq]"))
        )
            return
        val castleMoves = arrayOf("e1c1", "e1g1", "e8c8", "e8g8")
        if (board[fromCoordinate]!!.piece is King && castleMoves.contains(movePos)) {
            when (toCoordinate) {
                Coordinate.c1 -> movePiece(Coordinate.a1, Coordinate.d1)
                Coordinate.g1 -> movePiece(Coordinate.h1, Coordinate.f1)
                Coordinate.c8 -> movePiece(Coordinate.a8, Coordinate.d8)
                Coordinate.g8 -> movePiece(Coordinate.h8, Coordinate.f8)
            }
        }
    }

    private fun movePiece(fromCoordinate: Coordinate, toCoordinate: Coordinate, promotePiece: Char? = null) {
        board[fromCoordinate]!!.piece!!.showPossibleMoves(false, false)
        board[toCoordinate]!!.piece = board[fromCoordinate]!!.piece?.promote(promotePiece)
        board[toCoordinate]!!.piece!!.coordinate = toCoordinate
        board[fromCoordinate]!!.piece = null
    }

    interface OnPieceMovedListener {
        fun onPieceMoved(gameId: Long, colorToMove: Color, move: String)
    }
}