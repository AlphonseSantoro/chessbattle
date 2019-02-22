package no.kristiania.alphonsesantoro.chessbattle.game.pieces

import no.kristiania.alphonsesantoro.chessbattle.game.Color
import no.kristiania.alphonsesantoro.chessbattle.game.Coordinate

class Knight(resource: Int, color: Color, tag: Char, coordinate: Coordinate) :
    Piece(resource, color, tag, coordinate) {
    override fun showPossibleMoves(show: Boolean) {
        val ids = arrayOf(
            "${coordinate.name[0] + 1}${coordinate.name[1] + 2}",
            "${coordinate.name[0] + 2}${coordinate.name[1] + 1}",
            "${coordinate.name[0] + 2}${coordinate.name[1] - 1}",
            "${coordinate.name[0] + 1}${coordinate.name[1] - 2}",
            "${coordinate.name[0] - 1}${coordinate.name[1] - 2}",
            "${coordinate.name[0] - 2}${coordinate.name[1] - 1}",
            "${coordinate.name[0] - 2}${coordinate.name[1] + 1}",
            "${coordinate.name[0] - 1}${coordinate.name[1] + 2}"
        )
        for (id in ids) {
            isLegalSquare(show, Coordinate.fromString(id))
        }
    }
}
