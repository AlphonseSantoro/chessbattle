package no.kristiania.alphonsesantoro.chessbattle.game

class TwoPlayerGame(perspective: Color, gameStatus: GameStatus, onPieceMovedListener: OnPieceMovedListener) :
    Game(perspective, gameStatus, onPieceMovedListener = onPieceMovedListener)