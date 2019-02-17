package no.kristiania.alphonsesantoro.chessbattle.game

import android.content.Context
import android.content.res.Resources
import android.view.View
import android.widget.ImageView
import androidx.fragment.app.FragmentActivity
import jstockfish.Uci
import jstockfish.Uci.newGame
import jstockfish.Uci.position
import kotlinx.android.synthetic.main.fragment_board.view.*
import no.kristiania.alphonsesantoro.chessbattle.R
import no.kristiania.alphonsesantoro.chessbattle.game.pieces.Pawn
import no.kristiania.alphonsesantoro.chessbattle.game.pieces.Piece
import no.kristiania.alphonsesantoro.chessbattle.game.pieces.ResourcePiece
import no.kristiania.alphonsesantoro.chessbattle.util.OutputListenerImpl
import no.kristiania.alphonsesantoro.chessbattle.util.asInt

class Game(val activity: FragmentActivity?, val boardView: View, val context: Context, val resources: Resources) {
    private var pieces : MutableList<Piece> = mutableListOf()
    private var squares : MutableList<ImageView> = mutableListOf()
    private var colorToMove = Color.WHITE
    private var enPassantTarget = '-'
    private var halfMoveClock = 0
    private var fullMoveNumber = 1
    private var canCastle = arrayOf('K', 'Q', 'k', 'q')
    private var selectedPiece: Piece? = null
    private var capturedPieces = mapOf<Color, MutableList<Piece>>(Color.WHITE to mutableListOf(), Color.BLACK to mutableListOf())
    private var uciListener : OutputListenerImpl

    init {
        newGame()
        uciListener = OutputListenerImpl
        Uci.setOutputListener(uciListener)
        position("startpos")
    }

    fun move(to: Any): Boolean {
        val toSquare = when(to){
            is ImageView -> to
            is Piece -> to.square
            is String -> {
                selectedPiece = pieces.first { it.square!!.contentDescription == to.substring(0, 2) }
                squares.first { it.contentDescription == to.substring(2) }
            }
            else -> throw IllegalArgumentException("Your doing it wrong")
        }
        var movePos = "${selectedPiece!!.square!!.contentDescription}${toSquare!!.contentDescription}"
        movePos += if(isPromotion(selectedPiece!!, toSquare)) getPromotePiece().tag.toUpperCase() else ""
        movePos = if(to is String) to else movePos
        val position = setUciPosition()
        val legal = Uci.isLegal(movePos)
        if (position && legal) {
            if(isPromotion(selectedPiece!!, toSquare)) {
                promote(toSquare, getPromotePiece())
            } else {
                drawMove(toSquare, selectedPiece!!.resource, selectedPiece!!.square!!.tag.toString())
            }
            if(to is Piece) {
                capturedPieces.getValue(colorToMove).add(to)
                pieces.remove(to)
            }
            colorToMove = if(colorToMove == Color.WHITE) Color.BLACK else Color.WHITE
            boardView.fenPosition.text = currentPositionFen()
            if(colorToMove == Color.BLACK) computerMove()
        }
        return false
    }

    fun capture(piece: Piece) : String {
        return "${selectedPiece!!.tag}x${piece.square!!.contentDescription}"
    }

    fun isPromotion(piece: Piece, toSquare: ImageView): Boolean {
        return toSquare.contentDescription[1].asInt == selectedPiece!!.promoteRank && piece is Pawn
    }

    fun drawMove(toSquare: ImageView, resource: Int, newTag: String){
        with(toSquare){
            selectedPiece!!.showPossibleMoves(false)
            tag = newTag
            setImageResource(resource)
            selectedPiece!!.square!!.setImageResource(R.drawable.ic_blank_tile)
            selectedPiece!!.square!!.tag = " "
            selectedPiece!!.square = toSquare
        }

    }

    fun promote(toSquare: ImageView, toPiece: Piece) {
        drawMove(toSquare, toPiece.resource, toPiece.tag.toString())
    }

    fun getPromotePiece(): Piece {
        val resourcePiece = promoteDialog()
        return Piece.getPiece(resourcePiece.tag(selectedPiece!!.color), selectedPiece!!.color, boardView)!!
    }

    fun promoteDialog(): ResourcePiece {
        // TODO: show dialog
        return ResourcePiece.ROOK
    }

    fun onSquareClick(square: ImageView) {
        val piece = Piece.getPieceFromSquare(boardView, square)
        if (selectedPiece != null) {
            if(piece == null) move(square) else move(piece)
            selectedPiece!!.showPossibleMoves(false)
            selectedPiece = null
        } else if (piece != null && colorToMove == piece.color) {
            selectedPiece = piece
            selectedPiece!!.showPossibleMoves(true)
        }
    }

    fun currentPositionFen(): String {
        var fen = ""
        for (c in 8 downTo 1) {
            var counter = 0
            for (r in 'a'..'h') {
                val id = resources.getIdentifier("$r$c", "id", context.packageName)
                val tile = boardView.findViewById<ImageView>(id)
                if (tile.tag.toString().isNotBlank()) {
                    if (counter > 0) fen += counter
                    fen += tile.tag.toString()
                    counter = 0
                } else counter++
            }
            if (counter > 0) fen += counter
            if (c != 1) fen += "/"
        }

        fen += " ${colorToMove.fen} ${canCastle.joinToString("")} $enPassantTarget $halfMoveClock $fullMoveNumber"
        return fen
    }

    fun drawBoard() {
        // TODO: Generate xml instead of hardcoding it.
        var green = true
        var color: Int
        for (r in 'a'..'h') {
            for (c in 1..8) {
                val id = resources.getIdentifier("$r$c", "id", context.packageName)
                val square = boardView.findViewById<ImageView>(id)
                squares.add(square)
                if(square.tag.toString().isNotBlank()) pieces.add(Piece.getPieceFromSquare(boardView, square)!!)
                square.isSelected = false
                square.setOnClickListener { onSquareClick(square) }
                color = if (green) {
                    R.color.colorBoardGreen
                } else {
                    R.color.colorBoardWhite
                }
                green = !green
                square.setBackgroundColor(resources.getColor(color, resources.newTheme()))
            }
            green = !green
        }

        boardView.fenPosition.text = currentPositionFen() // DEBUG!
    }

    fun computerMove() {
        setUciPosition()
        Uci.go("movetime 1000")
        Thread {
            Thread.sleep(1000)
            var bestMove = ""
            while (!bestMove.startsWith("bestmove")){
                bestMove = uciListener.output.last()
            }
            activity!!.runOnUiThread { move(bestMove.split(" ")[1]) }
        }.start()
    }

    fun setUciPosition() : Boolean {
        return Uci.position("fen ${currentPositionFen()}")
    }

    enum class Color {
        WHITE, BLACK;

        val fen : String
            get() =  if (this == WHITE) "w" else "b"
    }
}