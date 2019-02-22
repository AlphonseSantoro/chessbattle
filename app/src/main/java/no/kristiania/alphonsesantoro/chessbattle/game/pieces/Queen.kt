package no.kristiania.alphonsesantoro.chessbattle.game.pieces

import no.kristiania.alphonsesantoro.chessbattle.game.Color
import no.kristiania.alphonsesantoro.chessbattle.game.Coordinate

class Queen(resource: Int, color: Color, tag: Char, coordinate: Coordinate) :
    Piece(resource, color, tag, coordinate) {

    override fun showPossibleMoves(show: Boolean) {
        showVerticalAndHorizontal(show)
        showDiagonals(show)
    }

}
