package no.kristiania.alphonsesantoro.chessbattle.game.pieces

import android.view.View
import android.widget.ImageView
import no.kristiania.alphonsesantoro.chessbattle.game.Game

class Bishop(boardView: View, resource: Int, color: Game.Color, tag: Char, square: ImageView? = null) :
    Piece(boardView, resource, color, tag, square) {

    override fun showPossibleMoves(show: Boolean) {
        showDiagonals(show)
    }

}
