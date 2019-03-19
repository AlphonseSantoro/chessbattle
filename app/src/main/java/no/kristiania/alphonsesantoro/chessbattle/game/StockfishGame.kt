package no.kristiania.alphonsesantoro.chessbattle.game

import jstockfish.Uci
import java.lang.IllegalArgumentException

class StockfishGame(perspective: Color, gameStatus: GameStatus, onPieceMovedListener: OnPieceMovedListener) :
    Game(perspective, gameStatus, onPieceMovedListener = onPieceMovedListener) {
    override fun move(
        fromCoordinate: Coordinate,
        toCoordinate: Coordinate,
        promotePiece: Char?
    ): Boolean {
        if (super.move(fromCoordinate, toCoordinate, promotePiece) && perspective != colorToMove) {
            computerMove() // Stockfish is white, make a move
        }
        return true
    }

    internal fun computerMove() {
        if(perspective != colorToMove){
            Thread {
                try {
                    Uci.go("movetime 1000")
                    Thread.sleep(1020)
                    var bestMove = uciListener.output.last()
                    while (!bestMove.startsWith("bestmove")) {
                        bestMove = uciListener.output.last()
                    }
                    bestMove = bestMove.split(" ")[1]
                    val from = Coordinate.valueOf(bestMove.substring(0, 2))
                    val to = Coordinate.valueOf(bestMove.substring(2, 4))
                    move(from, to)
                } catch (e: IllegalArgumentException) {
                    computerMove()
                }
            }.start()
        }
    }
}