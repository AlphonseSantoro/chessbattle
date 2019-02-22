package no.kristiania.alphonsesantoro.chessbattle.game.pieces

import android.view.View
import android.widget.ImageView
import no.kristiania.alphonsesantoro.chessbattle.game.Color
import no.kristiania.alphonsesantoro.chessbattle.game.Coordinate
import no.kristiania.alphonsesantoro.chessbattle.game.Game

class Bishop(resource: Int, color: Color, tag: Char, coordinate: Coordinate) :
    Piece(resource, color, tag, coordinate) {

    override fun showPossibleMoves(show: Boolean) {
        showDiagonals(show)
    }

}
