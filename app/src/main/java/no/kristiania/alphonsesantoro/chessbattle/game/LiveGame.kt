package no.kristiania.alphonsesantoro.chessbattle.game

import android.util.Log

class LiveGame(
    perspective: Color,
    gameStatus: GameStatus,
    onPieceMovedListener: OnPieceMovedListener
) :
    Game(perspective, gameStatus, onPieceMovedListener = onPieceMovedListener) {
}