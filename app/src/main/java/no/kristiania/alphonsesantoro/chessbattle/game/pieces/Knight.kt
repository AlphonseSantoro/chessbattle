package no.kristiania.alphonsesantoro.chessbattle.game.pieces

import android.view.View
import android.widget.ImageView
import no.kristiania.alphonsesantoro.chessbattle.game.Game

class Knight(boardView: View, resource: Int, color: Game.Color, tag: Char, square: ImageView? = null) :
    Piece(boardView, resource, color, tag, square) {
    override fun showPossibleMoves(show: Boolean) {
        val coord = square!!.contentDescription.toString()
        val ids = arrayOf(
            "${coord[0] + 1}${coord[1] + 2}",
            "${coord[0] + 2}${coord[1] + 1}",
            "${coord[0] + 2}${coord[1] - 1}",
            "${coord[0] + 1}${coord[1] - 1}",
            "${coord[0] - 1}${coord[1] - 2}",
            "${coord[0] - 2}${coord[1] - 1}",
            "${coord[0] - 2}${coord[1] + 1}",
            "${coord[0] - 1}${coord[1] + 2}"
        )
        for (id in ids) {
            isLegalSquare(show, id)
        }
    }
}
