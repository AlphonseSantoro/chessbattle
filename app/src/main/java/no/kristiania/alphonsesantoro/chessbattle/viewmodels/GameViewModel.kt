package no.kristiania.alphonsesantoro.chessbattle.viewmodels

import android.app.Application
import android.os.Bundle
import androidx.lifecycle.AndroidViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import jstockfish.Uci
import no.kristiania.alphonsesantoro.chessbattle.database.*
import no.kristiania.alphonsesantoro.chessbattle.game.*
import no.kristiania.alphonsesantoro.chessbattle.game.Color.*
import no.kristiania.alphonsesantoro.chessbattle.game.GameMode.*

class GameViewModel(application: Application) : AndroidViewModel(application), Game.OnPieceMovedListener {
    lateinit var user: UserModel
    private var gameMode = UNKNOWN
    internal var white: String? = null
    internal var black: String? = null
    private var perspective: Color = WHITE
    private var gameId: Long = -1L
    private var gameKey: String? = null
    private var currentUser: FirebaseUser? = null
    private var otherUserName: String? = null
    internal var game: Game? = null
    private lateinit var gameRepository: GameRepository
    internal lateinit var gameLineRepository: GameLineRepository

    fun setupGame() {
        when (gameMode) {
            LIVE -> {
                currentUser = FirebaseAuth.getInstance().currentUser
                game = LiveGame(perspective, gameKey, GameStatus.INPROGRESS, onPieceMovedListener = this)
            }
            STOCKFISH -> {
                game = StockfishGame(perspective, GameStatus.INPROGRESS, onPieceMovedListener = this)
                if(perspective == WHITE){
                    white = user.userName
                    black = STOCKFISH.name.capitalize()
                } else {
                    white = STOCKFISH.name.capitalize()
                    black = user.userName
                }
            }
            TWO_PLAYER -> {
                game = TwoPlayerGame(perspective, GameStatus.INPROGRESS, onPieceMovedListener = this)
                    white = user.userName
                    black = "Player two"
            }
        }
        game!!.gameId = gameId
    }

    fun bundle(arguments: Bundle?) {
        arguments?.let {
            gameMode = it.get("gameMode") as GameMode
            white = it.getString("white")
            black = it.getString("black")
            gameKey = it.getString("gameKey")
            gameId = it.getLong("gameId")
            if (it.get("perspective") != null) {
                perspective = it.get("perspective") as Color
            }
            otherUserName = it.getString("other_username")
        }
    }

    override fun onPieceMoved(gameId: Long, colorToMove: Color, move: String) {
        Thread {
            if (colorToMove == WHITE) {
                gameLineRepository.insert(GameLineModel(whiteMove = move, gameId = gameId, whiteFen = Uci.fen()))
            } else {
                val gameLine = gameLineRepository.getGameLines().value!!.last()
                gameLine.blackMove = move
                gameLine.blackFen = Uci.fen()
                gameLineRepository.update(gameLine)
            }
        }.start()
    }

    internal fun saveGame(status: GameStatus? = null) {
        gameRepository = GameRepository(getApplication(), user.id!!)
        with(game!!) {
            if(gameId == -1L){
                val gameModel = GameModel(
                    white = white!!,
                    black = black!!,
                    userId = user.id!!,
                    status = GameStatus.INPROGRESS,
                    type = gameMode.name,
                    perspective = perspective.name
                )
                game!!.gameId = gameRepository.insert(gameModel)

            } else if(status != null) {
                val gameModel = gameRepository.find(gameId)!!
                gameModel.status = status
                gameRepository.update(gameModel)
            }
            gameLineRepository = GameLineRepository(getApplication(), game!!.gameId)
        }
    }
}