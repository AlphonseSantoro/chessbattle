package no.kristiania.alphonsesantoro.chessbattle.game.pieces

import android.view.View
import android.widget.ImageView
import no.kristiania.alphonsesantoro.chessbattle.R
import no.kristiania.alphonsesantoro.chessbattle.game.Game
import no.kristiania.alphonsesantoro.chessbattle.util.asInt

abstract class Piece(
    val boardView: View,
    val resource: Int,
    val color: Game.Color,
    val tag: Char,
    var square: ImageView? = null,
    var promoteRank: Int? = null
) {

    init {
        promoteRank = if (color == Game.Color.WHITE) 8 else 1
    }

    internal fun isLegalSquare(show: Boolean, identifier: String): Boolean {
        val foregroundId = if (show) R.drawable.ic_tile_suggestion else R.drawable.ic_blank_tile
        val id = boardView.resources.getIdentifier(identifier, "id", boardView.context!!.packageName)
        val otherSquare = boardView.findViewById<ImageView>(id) ?: return false
        if (otherSquare != square && isPieceBlocking(otherSquare, color, foregroundId)) return false
        if (otherSquare != square) {
            if(this is Pawn && otherSquare.contentDescription[0] != square!!.contentDescription[0]) return false
            otherSquare.foreground = boardView.resources.getDrawable(foregroundId, boardView.context.theme)
        }
        return true
    }

    fun isPieceBlocking(otherTile: ImageView, color: Game.Color, foregroundId: Int): Boolean {
        val piece = Piece.getPieceFromSquare(boardView, otherTile)
        if (piece != null) {
            if (color != piece.color) {
                otherTile.foreground = boardView.resources.getDrawable(foregroundId, boardView.context.theme)
            }
            return true
        }
        return false
    }

    fun showDiagonals(show: Boolean) {
        val coord = square!!.contentDescription.toString()
        // Find possible moves northeast
        var col = coord[0]
        var row = coord[1].asInt
        while (col <= 'h' || row <= 8){
            if (!isLegalSquare(show, "$col$row")) break
            col++
            row++
        }
        // Find possible moves southwest
        col = coord[0]
        row = coord[1].asInt
        while (col >= 'a' || row >= 1){
            if (!isLegalSquare(show, "$col$row")) break
            col--
            row--
        }
        // Find possible moves nortwest
        col = coord[0]
        row = coord[1].asInt
        while (col >= 'a' || row <= 8){
            if (!isLegalSquare(show, "$col$row")) break
            col--
            row++
        }
        // Find possible moves southeast
        col = coord[0]
        row = coord[1].asInt
        while (col >= 'h' || row >= 1){
            if (!isLegalSquare(show, "$col$row")) break
            col++
            row--
        }
    }

    fun showVerticalAndHorizontal(show: Boolean){
        val coord = square!!.contentDescription.toString()
        // Find possible moves up
        for (c in coord[1].asInt..8) {
            if (!isLegalSquare(show, "${coord[0].toLowerCase()}$c")) break
        }
        // Find possible moves down
        for (c in coord[1].asInt downTo 1) {
            if (!isLegalSquare(show, "${coord[0].toLowerCase()}$c")) break
        }
        // Find possible moves left
        for (c in coord[0].toLowerCase()..'h') {
            if (!isLegalSquare(show, "$c${coord[1]}")) break
        }
        // Find possible moves right
        for (c in coord[0].toLowerCase() downTo 'a') {
            if (!isLegalSquare(show, "$c${coord[1]}")) break
        }
    }

    abstract fun showPossibleMoves(show: Boolean)

    companion object {
        fun getPieceFromSquare(boardView: View, square: ImageView): Piece? {
            val isWhite = square.tag.toString()[0].isUpperCase()
            return getPiece(
                square.tag.toString()[0],
                if (isWhite) Game.Color.WHITE else Game.Color.BLACK,
                boardView,
                square
            )
        }

        fun getPiece(
            tag: Char,
            color: Game.Color,
            boardView: View,
            square: ImageView? = null
        ): Piece? {
            return when (tag.toLowerCase()) {
                'p' -> Pawn(boardView, ResourcePiece.PAWN.resource(color), color, tag, square)
                'r' -> Rook(boardView, ResourcePiece.ROOK.resource(color), color, tag, square)
                'n' -> Knight(boardView, ResourcePiece.KNIGHT.resource(color), color, tag, square)
                'b' -> Bishop(boardView, ResourcePiece.BISHOP.resource(color), color, tag, square)
                'q' -> Queen(boardView, ResourcePiece.QUEEN.resource(color), color, tag, square)
                'k' -> King(boardView, ResourcePiece.KING.resource(color), color, tag, square)
                else -> null
            }
        }
    }
}

enum class ResourcePiece(private val whiteResource: Int, private val blackResource: Int, private val tag: Char) {
    ROOK(R.drawable.ic_white_rook, R.drawable.ic_black_rook, 'R'),
    BISHOP(R.drawable.ic_white_bishop, R.drawable.ic_black_bishop, 'B'),
    KNIGHT(R.drawable.ic_white_knight, R.drawable.ic_black_knight, 'N'),
    KING(R.drawable.ic_white_king, R.drawable.ic_black_king, 'K'),
    QUEEN(R.drawable.ic_white_queen, R.drawable.ic_black_queen, 'Q'),
    PAWN(R.drawable.ic_white_pawn, R.drawable.ic_black_pawn, 'P');

    fun resource(color: Game.Color): Int {
        return if (color == Game.Color.WHITE) whiteResource else blackResource
    }

    fun tag(color: Game.Color): Char {
        return if (color == Game.Color.WHITE) tag.toUpperCase() else tag.toLowerCase()
    }
}