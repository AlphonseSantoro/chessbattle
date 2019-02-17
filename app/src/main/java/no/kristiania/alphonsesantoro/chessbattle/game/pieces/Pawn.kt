package no.kristiania.alphonsesantoro.chessbattle.game.pieces

import android.view.View
import android.widget.ImageView
import no.kristiania.alphonsesantoro.chessbattle.game.Game
import no.kristiania.alphonsesantoro.chessbattle.util.asInt

class Pawn(boardView: View, resource: Int, color: Game.Color, tag: Char, square: ImageView? = null) :
    Piece(boardView, resource, color, tag, square) {

    override fun showPossibleMoves(show: Boolean) {
        if (square!!.contentDescription[1].asInt == promoteRank) return
        val coord = square!!.contentDescription.toString()
        isLegalSquare(show, "${coord[0]}${coord[1].asInt + 1}")
        if (isStartPos()) isLegalSquare(show, "${coord[0]}${coord[1].asInt + 2}")
        isLegalSquare(show, "${coord[0] + 1}${coord[1].asInt + 1}")
        isLegalSquare(show, "${coord[0] - 1}${coord[1].asInt + 1}")
    }

    private fun isStartPos(): Boolean {
        if (square!!.contentDescription[1].asInt == 2 && color == Game.Color.WHITE ||
            square!!.contentDescription[1].asInt == 7 && color == Game.Color.BLACK
        ) {
            return true
        }
        return false
    }
}
