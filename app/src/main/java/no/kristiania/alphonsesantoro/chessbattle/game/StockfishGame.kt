package no.kristiania.alphonsesantoro.chessbattle.game

import android.app.Activity
import android.util.Log
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
//            computerMove() // Stockfish is white, make a move
        }
        return true
    }

    internal fun computerMove(activity: Activity) {
        Thread {
            var thinking = false
            while (gameStatus == GameStatus.INPROGRESS){
                if (perspective != colorToMove && !thinking) {
                    thinking = true
                    try {
                        Uci.go("depth 15")
                        var bestMove = uciListener.output.lastOrNull()
                        while (bestMove == null || !bestMove.startsWith("bestmove")) {
                            bestMove = uciListener.output.lastOrNull()
                        }
                        bestMove = bestMove.split(" ")[1]
                        val from = Coordinate.valueOf(bestMove.substring(0, 2))
                        val to = Coordinate.valueOf(bestMove.substring(2, 4))
                        activity.runOnUiThread { move(from, to) }
                        thinking = false
                    } catch (e: IllegalArgumentException) {
                        // Something went wrong with stockfish, couldnt capture the output
                        Log.w("Stockfish", "Failed to move: ${Uci.state()}")
                    }
                }
            }
        }.start()
    }
}