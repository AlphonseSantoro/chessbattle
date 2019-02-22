package no.kristiania.alphonsesantoro.chessbattle.game.pieces

import no.kristiania.alphonsesantoro.chessbattle.game.Color
import no.kristiania.alphonsesantoro.chessbattle.game.Coordinate
import no.kristiania.alphonsesantoro.chessbattle.util.asInt

class Pawn(resource: Int, color: Color, tag: Char, coordinate: Coordinate) :
    Piece(resource, color, tag, coordinate) {

    override fun showPossibleMoves(show: Boolean) {
        val moveDir = if (color == Color.WHITE) 1 else -1
        if (isStartPos()) isLegalSquare(
            show,
            Coordinate.fromString("${coordinate.name[0]}${coordinate.name[1].asInt + (2 * moveDir)}")
        )
        isLegalSquare(
            show,
            Coordinate.fromString("${coordinate.name[0]}${coordinate.name[1].asInt + (1 * moveDir)}")
        )
        isLegalSquare(
            show,
            Coordinate.fromString("${coordinate.name[0] + 1}${coordinate.name[1].asInt + (1 * moveDir)}")
        )
        isLegalSquare(
            show,
            Coordinate.fromString("${coordinate.name[0] - 1}${coordinate.name[1].asInt + (1 * moveDir)}")
        )

        // check promotion
        val nextRank = coordinate.name[1] + 1 * moveDir
        isLegalSquare(show, Coordinate.fromString("${coordinate.name[0] - 1}$nextRank"), 'Q')
        isLegalSquare(show, Coordinate.fromString("${coordinate.name[0]}$nextRank"), 'Q')
        isLegalSquare(show, Coordinate.fromString("${coordinate.name[0] + 1}$nextRank"), 'Q')
    }

    private fun isStartPos(): Boolean {
        if (coordinate.name[1].asInt == 2 && color == Color.WHITE ||
            coordinate.name[1].asInt == 7 && color == Color.BLACK
        ) {
            return true
        }
        return false
    }
}
