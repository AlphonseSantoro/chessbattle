package no.kristiania.alphonsesantoro.chessbattle.game.pieces

import android.view.View
import android.widget.ImageView
import no.kristiania.alphonsesantoro.chessbattle.game.Game
import no.kristiania.alphonsesantoro.chessbattle.util.asInt

class Queen(boardView: View, resource: Int, color: Game.Color, tag: Char, square: ImageView? = null) :
    Piece(boardView, resource, color, tag, square) {

    override fun showPossibleMoves(show: Boolean) {
        showVerticalAndHorizontal(show)
        showDiagonals(show)
    }

}
