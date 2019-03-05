package no.kristiania.alphonsesantoro.chessbattle.game.pieces

import jstockfish.Uci
import no.kristiania.alphonsesantoro.chessbattle.game.Color
import no.kristiania.alphonsesantoro.chessbattle.game.Coordinate
import no.kristiania.alphonsesantoro.chessbattle.game.Game
import no.kristiania.alphonsesantoro.chessbattle.util.asInt

class King(resource: Int, color: Color, tag: Char, coordinate: Coordinate) :
    Piece(resource, color, tag, coordinate) {

    override fun showPossibleMoves(show: Boolean, check: Boolean) {
        super.showPossibleMoves(show, check)
        val ids = arrayOf(
            "${coordinate.name[0] - 1}${coordinate.name[1] + 1}",
            "${coordinate.name[0]}${coordinate.name[1] + 1}",
            "${coordinate.name[0] + 1}${coordinate.name[1] + 1}",
            "${coordinate.name[0] + 1}${coordinate.name[1]}",
            "${coordinate.name[0] + 1}${coordinate.name[1] - 1}",
            "${coordinate.name[0]}${coordinate.name[1] - 1}",
            "${coordinate.name[0] - 1}${coordinate.name[1] - 1}",
            "${coordinate.name[0] - 1}${coordinate.name[1]}"
        )

        for (id in ids) isLegalSquare(show, Coordinate.fromString(id))
        canCastle(Color.WHITE, Regex("[QK]"), show, Coordinate.c1)
        canCastle(Color.WHITE, Regex("[QK]"), show, Coordinate.g1)
        canCastle(Color.BLACK, Regex("[qk]"), show, Coordinate.c8)
        canCastle(Color.BLACK, Regex("[qk]"), show, Coordinate.g8)
    }

    private fun canCastle(color: Color, regex: Regex, show: Boolean, coordinate: Coordinate) {
        if (this.color == color && Uci.fen().split(" ")[2].contains(regex)) {
            isLegalSquare(show, coordinate)
        }
    }
}
