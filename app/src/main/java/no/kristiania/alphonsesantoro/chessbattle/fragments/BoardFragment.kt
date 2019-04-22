package no.kristiania.alphonsesantoro.chessbattle.fragments

import android.content.Context
import android.content.Context.SENSOR_SERVICE
import android.content.res.Configuration
import android.graphics.Point
import android.graphics.drawable.Drawable
import android.graphics.drawable.LayerDrawable
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.media.MediaPlayer
import android.os.Bundle
import android.util.FloatMath
import android.util.Log
import android.view.*
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat.getSystemService
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.gms.games.multiplayer.realtime.Room
import com.google.android.gms.games.multiplayer.realtime.RoomConfig
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.gson.Gson
import jstockfish.Uci
import jstockfish.Uci.fen
import kotlinx.android.synthetic.main.black_promote_popup.view.*
import kotlinx.android.synthetic.main.game_settings.view.*
import kotlinx.android.synthetic.main.game_settings_menu.view.*
import kotlinx.android.synthetic.main.landscape_board.view.*
import no.kristiania.alphonsesantoro.chessbattle.R
import no.kristiania.alphonsesantoro.chessbattle.adapters.MovesRecyclerAdapter
import no.kristiania.alphonsesantoro.chessbattle.database.GameLineModel
import no.kristiania.alphonsesantoro.chessbattle.database.GameLineRepository
import no.kristiania.alphonsesantoro.chessbattle.database.GameRepository
import no.kristiania.alphonsesantoro.chessbattle.game.*
import no.kristiania.alphonsesantoro.chessbattle.game.Color.*
import no.kristiania.alphonsesantoro.chessbattle.game.GameMode.*
import no.kristiania.alphonsesantoro.chessbattle.viewmodels.SharedViewModel


class BoardFragment : BaseFragment(), Game.OnPieceMovedListener, SharedViewModel.OnMessageReceivedListener,
    SensorEventListener {

    private val TAG = "Board"

    private var recyclerView: RecyclerView? = null
    private var viewAdapter: RecyclerView.Adapter<MovesRecyclerAdapter.MovesViewHolder>? = null
    private var viewManager: RecyclerView.LayoutManager? = null

    private lateinit var boardView: View
    private var gameMode: GameMode = STOCKFISH
    private var perspective: Color = WHITE
    private var orientation: Int = -1
    private var currentMoveIndex: Int? = -1
    private var currentPerspectiveLayout: Int = 0
    internal var white: String? = null
    internal var black: String? = null
    private var gameId: Long = -1L
    private var currentUser: FirebaseUser? = null
    internal var game: Game? = null
    private lateinit var gameRepository: GameRepository
    private lateinit var gameLineRepository: GameLineRepository
    lateinit var moveSound: MediaPlayer
    private var mShakeTimestamp: Long = System.currentTimeMillis()
    private var displayingHint = false

    private lateinit var playerTwoKey: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        moveSound = MediaPlayer()
        Uci.newGame()
        arguments?.let {
            gameMode = it.get("gameMode") as GameMode
            white = it.getString("white")
            black = it.getString("black")
            gameId = it.getLong("gameId")
            if (it.get("perspective") != null) perspective = it.get("perspective") as Color
        }
        currentPerspectiveLayout = if (perspective == WHITE) R.layout.white_perspective else R.layout.black_perspective

        playerTwoKey = if (perspective == WHITE) {
            arguments?.getString("black").toString()
        } else {
            arguments?.getString("white").toString()
        }
        sharedViewModel.init(this)

        val sensorManager = activity?.getSystemService(SENSOR_SERVICE) as SensorManager
        sensorManager.registerListener(this,
            sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),
            SensorManager.SENSOR_DELAY_GAME)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        if (arguments != null) outState.putAll(arguments)
        outState.putString("currentFen", fen())
        outState.putBoolean("restartGame", false)
        outState.putLong("gameId", game!!.gameId)
        super.onSaveInstanceState(outState)
    }

    override fun onViewStateRestored(savedInstanceState: Bundle?) {
        super.onViewStateRestored(savedInstanceState)
        setupGame()
        setRecyclerView()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        orientation = if (resources.configuration.orientation == Configuration.ORIENTATION_PORTRAIT) {
            R.layout.portrait_board
        } else {
            R.layout.landscape_board
        }
        val view = inflater.inflate(orientation, container, false)
        boardView = view
        setupLayout(view, inflater, container)
        setupNames(view)
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setRecyclerView()
        setupSettingButtons()
    }

    override fun onMessageReceived(gameState: GameState) {
        Log.d(TAG, gameState.toString())
        if (gameState.fromCoordinate != null && gameState.toCoordinate != null) {
            game!!.move(gameState.fromCoordinate!!, gameState.toCoordinate!!, gameState.promotePiece)
        }
    }

    private fun setupSettingButtons() {
        with(view!!) {
            val inflater = LayoutInflater.from(context)
            gameSettings.setOnClickListener {
                val alert = AlertDialog.Builder(ContextThemeWrapper(activity, R.style.AppTheme))
                val settingsView = inflater.inflate(R.layout.game_settings_menu, null)
                alert.setView(settingsView)
                val dialog = alert.create()
                dialog.show()
                if (gameMode != LIVE) settingsView?.offer_draw?.visibility = View.GONE
                settingsView?.findViewById<LinearLayout>(R.id.resign_game)?.setOnClickListener {
                    Thread {
                        saveGame(GameStatus.RESIGNED)
                        game!!.gameStatus = GameStatus.RESIGNED
                        activity?.runOnUiThread {
                            dialog.dismiss()
                            showGameOver()
                        }
                    }.start()
                }
            }

            flip_board.setOnClickListener {
                boardFrame.removeAllViews()

                currentPerspectiveLayout = if (currentPerspectiveLayout == R.layout.white_perspective) {
                    R.layout.black_perspective
                } else {
                    R.layout.white_perspective
                }
                boardFrame.addView(inflater.inflate(currentPerspectiveLayout, null, false))
                drawPosition()
            }

            showMoveBack.setOnClickListener {
                if (currentMoveIndex != null) currentMoveIndex = currentMoveIndex!! - 1
                drawSpecificPosition()
            }

            showMoveForward.setOnClickListener {
                if (currentMoveIndex != null) currentMoveIndex = currentMoveIndex!! + 1
                drawSpecificPosition()
            }
        }
    }

    fun drawSpecificPosition() {
        Thread {
            val moveList = mutableListOf("rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1")
            gameLineRepository.all(game!!.gameId).forEach {
                if (it.whiteFen != null) moveList.add(it.whiteFen!!)
                if (it.blackFen != null) moveList.add(it.blackFen!!)
            }
            if (currentMoveIndex == null || currentMoveIndex!! > moveList.size - 1) currentMoveIndex = moveList.size - 1
            if (currentMoveIndex!! < 0) currentMoveIndex = 0

            activity?.runOnUiThread {
                drawPosition(Game.getBoardFromFen(moveList[currentMoveIndex!!]))
            }
        }.start()
    }


    private fun onPostSetupGame() {
        if (game != null) {
            with(game!!) {
                turn.observe(this@BoardFragment, Observer {
                    drawPosition()
                    isGameOver()
                })
                gameLineRepository.getGameLines().observe(this@BoardFragment, Observer {
                    if (it != null) (viewAdapter as MovesRecyclerAdapter).setData(it)
                })
                colorToMove = Color.fromFen()
                if (gameMode == STOCKFISH) (this as StockfishGame).computerMove(activity!!)
            }
        }
    }

    fun drawPosition(board: MutableMap<Coordinate, Square> = Game.board) {
        if (view != null) {
            board.forEach { coordinate, square ->
                val id = boardView.resources.getIdentifier(coordinate.name, "id", boardView.context.packageName)
                val squareView = boardView.findViewById<ImageView>(id)
                if (square.piece != null) {
                    squareView.setImageResource(square.piece!!.resource)
                } else squareView.setImageResource(square.emptySquareRes)
                val drawables = mutableListOf<Drawable>()
                if (square.showForeground) {
                    if (game?.selectedPiece == square.piece) {
                        drawables.add(
                            boardView.resources.getDrawable(
                                R.drawable.ic_selected_square,
                                boardView.context.theme
                            )
                        )
                    }
                    drawables.add(boardView.resources.getDrawable(square.foregroundResource, boardView.context.theme))
                    squareView.foreground = LayerDrawable(drawables.toTypedArray())
                } else {
                    squareView.foreground =
                        boardView.resources.getDrawable(R.drawable.ic_blank_tile, boardView.context.theme)
                }
                if (square.piece != null && square.piece == game?.lastMovedPiece) {
                    drawables.add(
                        boardView.resources.getDrawable(
                            R.drawable.ic_selected_square,
                            boardView.context.theme
                        )
                    )
                    squareView.foreground = LayerDrawable(drawables.toTypedArray())
                }
                squareView.setOnClickListener { onSquareClick(squareView) }
            }
        }
    }

    fun onSquareClick(squareView: ImageView) {
        if (game!!.gameStatus != GameStatus.INPROGRESS) return
        currentMoveIndex = null
        if (game != null) {
            with(game!!) {
                val coord = Coordinate.valueOf(squareView.contentDescription.toString())
                if (perspective != colorToMove && gameMode != TWO_PLAYER) return // TODO: enable premoving
                val square = Game.board[coord]!!
                if (selectedPiece != null && move(selectedPiece!!.coordinate, coord) && Game.isPromotion) {
                    showPromotePopup(fromCoordinate!!, toCoordinate!!)
                    return
                }
                if (square.piece == null) {
                    selectedPiece?.showPossibleMoves(show = false, check = false)
                    selectedPiece = null
                } else if (square.piece?.color == colorToMove) {
                    selectedPiece = square.piece
                    selectedPiece?.showPossibleMoves(show = true, check = false)
                }
                if (selectedPiece?.legalMovesCount == 0) {
                    selectedPiece?.showPossibleMoves(show = false, check = false)
                    selectedPiece = null
                }
            }
        }
        drawPosition()
        isGameOver()
    }

    private fun showGameOver() {
        val alert = AlertDialog.Builder(ContextThemeWrapper(activity, R.style.AppTheme))
        alert.setTitle("Game Over")
        val win =
            if (game?.colorToMove == perspective && Game.isChecked || game?.gameStatus == GameStatus.RESIGNED) {
                "You lost"
            } else if (game?.colorToMove != perspective && Game.isChecked) {
                "You win"
            } else "Stale mate"
        alert.setMessage(win)
        alert.show()
    }

    private fun isGameOver() {
        var legalMovesCounter = 0
        Game.isChecked = false
        Game.board.forEach { coordinate, square ->
            if (square.piece != null) {
                square.piece!!.showPossibleMoves(false, false)
                legalMovesCounter += square.piece!!.legalMovesCount
                square.piece!!.showPossibleMoves(false, true)
            }
        }
        if (legalMovesCounter == 0) {
            showGameOver()
        }
    }

    private fun showPromotePopup(fromCoordinate: Coordinate, toCoordinate: Coordinate) {
        val inflater = activity!!.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val coord = view!!.findViewById<ImageView>(
            boardView.resources.getIdentifier(
                toCoordinate.name,
                "id",
                boardView.context.packageName
            )
        )
        val point = Point()
        activity!!.windowManager.defaultDisplay.getSize(point)
        val layout = if (perspective == WHITE) R.layout.white_promote_popup else R.layout.black_promote_popup
        val contentView = inflater.inflate(layout, coord.parent as ViewGroup, false)
        val pw = PopupWindow(contentView, point.x / 8, point.x / 2, true)
        pw.showAsDropDown(coord, Gravity.CENTER, 0, 0)
        contentView.promote_queen.setOnClickListener {
            game?.move(fromCoordinate, toCoordinate, 'Q')
            drawPosition()
            pw.dismiss()
        }
        contentView.promote_rook.setOnClickListener {
            game?.move(fromCoordinate, toCoordinate, 'R')
            drawPosition()
            pw.dismiss()
        }
        contentView.promote_knight.setOnClickListener {
            game?.move(fromCoordinate, toCoordinate, 'N')
            drawPosition()
            pw.dismiss()
        }
        contentView.promote_bishop.setOnClickListener {
            game?.move(fromCoordinate, toCoordinate, 'B')
            drawPosition()
            pw.dismiss()
        }
    }

    fun setupNames(view: View) {
        when (gameMode) {
            LIVE -> {
                setNames(view, white!!, black!!)
            }
            STOCKFISH -> {
                if (perspective == WHITE) {
                    setNames(view, sharedViewModel.user!!.userName, "Stockfish")
                } else {
                    setNames(view, "Stockfish", sharedViewModel.user!!.userName)
                }
            }
            TWO_PLAYER -> {
                if (perspective == WHITE) {
                    setNames(view, sharedViewModel.user!!.userName, getString(R.string.playerTwo))
                } else {
                    setNames(view, getString(R.string.playerTwo), sharedViewModel.user!!.userName)
                }
            }
        }
    }

    fun setNames(view: View, whiteName: String, blackName: String) {
        white = whiteName
        black = blackName
        view.findViewById<TextView>(R.id.nameWhite)?.text = whiteName
        view.findViewById<TextView>(R.id.nameBlack)?.text = blackName
    }

    private fun setupLayout(view: View, inflater: LayoutInflater, container: ViewGroup?) {
        val layout: Int
        val topBlock: Int
        val bottomBlock: Int
        if (perspective == WHITE) {
            layout = R.layout.white_perspective
            topBlock = R.layout.black_name_block
            bottomBlock = R.layout.white_name_block
        } else {
            layout = R.layout.black_perspective
            bottomBlock = R.layout.black_name_block
            topBlock = R.layout.white_name_block
        }
        view.findViewById<FrameLayout>(R.id.topBlock)
            .addView(inflater.inflate(topBlock, container, false))
        view.findViewById<FrameLayout>(R.id.bottomBlock)
            .addView(inflater.inflate(bottomBlock, container, false))

        val boardFrame = view.findViewById<FrameLayout>(R.id.boardFrame)
        boardFrame.addView(inflater.inflate(layout, null, false))
    }

    private fun setupGame() {
        Thread {
            initializeGame()
            saveGame()
            setUciFenFromMoves()
            Game.board = Game.getBoardFromFen(Uci.fen())
            setRecyclerView()
            activity?.runOnUiThread { onPostSetupGame() }
        }.start()
    }

    private fun setUciFenFromMoves() {
        val moves = gameLineRepository.all(game!!.gameId)
        if (moves.isNotEmpty()) {
            var position = "startpos moves"
            moves.forEach { position += " ${it.whiteMove} ${it.blackMove}" }
            Uci.position(position)
        } else {
            Uci.position("startpos")
        }
    }

    private fun setRecyclerView() {
        viewManager = LinearLayoutManager(context)
        viewAdapter = MovesRecyclerAdapter(emptyList())
        recyclerView = view?.findViewById(R.id.moveList)
        if (recyclerView != null) {
            recyclerView!!.layoutManager = viewManager
            recyclerView!!.adapter = viewAdapter
        }
    }

    private fun initializeGame() {
        when (gameMode) {
            LIVE -> {
                currentUser = FirebaseAuth.getInstance().currentUser
                game = LiveGame(perspective, GameStatus.INPROGRESS, onPieceMovedListener = this)
            }
            STOCKFISH -> {
                game = StockfishGame(perspective, GameStatus.INPROGRESS, onPieceMovedListener = this)
                if (perspective == WHITE) {
                    white = sharedViewModel.user!!.userName
                    black = STOCKFISH.name.capitalize()
                } else {
                    white = STOCKFISH.name.capitalize()
                    black = sharedViewModel.user!!.userName
                }
            }
            TWO_PLAYER -> {
                game = TwoPlayerGame(perspective, GameStatus.INPROGRESS, onPieceMovedListener = this)
                white = sharedViewModel.user!!.userName
                black = "Player two"
            }
        }
        game!!.gameId = gameId
    }

    private fun saveGame(status: GameStatus? = null) {
        gameRepository = GameRepository(activity!!.application, sharedViewModel.user!!.id!!)
        with(game!!) {
            android.util.Log.i("GameViewModel", gameId.toString())
            if (gameId == -1L) {
                val gameModel = no.kristiania.alphonsesantoro.chessbattle.database.GameModel(
                    white = white!!,
                    black = black!!,
                    userId = sharedViewModel.user!!.id!!,
                    status = no.kristiania.alphonsesantoro.chessbattle.game.GameStatus.INPROGRESS,
                    type = gameMode.name,
                    perspective = perspective.name
                )
                game!!.gameId = gameRepository.insert(gameModel)

            } else if (status != null) {
                val gameModel = gameRepository.find(gameId)!!
                gameModel.status = status
                gameRepository.update(gameModel)
            }
            gameLineRepository = no.kristiania.alphonsesantoro.chessbattle.database.GameLineRepository(
                activity!!.application,
                game!!.gameId
            )
        }
    }


    override fun onPieceMoved(gameId: Long, colorToMove: Color, move: String) {
        moveSound = MediaPlayer.create(context!!, R.raw.move_piece)
        moveSound.start()
        Thread {
            if (colorToMove == WHITE) {
                gameLineRepository.insert(GameLineModel(whiteMove = move, gameId = gameId, whiteFen = fen()))
            } else {
                val gameLine = gameLineRepository.getGameLines().value!!.last()
                gameLine.blackMove = move
                gameLine.blackFen = Uci.fen()
                gameLineRepository.update(gameLine)
            }
            Log.d(TAG, "Move: $move")
            if(game is LiveGame){
                val promotePiece = if (move.length > 4) move.last() else null
                val gameState = GameState(
                    Uci.fen(),
                    Coordinate.fromString(move.substring(0, 2)),
                    Coordinate.fromString(move.substring(2, 4)),
                    promotePiece,
                    white!!,
                    black!!,
                    sharedViewModel.whiteId!!,
                    sharedViewModel.blackId!!,
                    sharedViewModel.roomId
                )
                sharedViewModel.mRealTimeMultiplayerClient.sendReliableMessage(
                    Gson().toJson(gameState).toByteArray(),
                    sharedViewModel.roomId!!,
                    (if (perspective == WHITE) sharedViewModel.blackId else sharedViewModel.whiteId)!!,
                    sharedViewModel.handleMessageSentCallback
                ).addOnCompleteListener {
                    sharedViewModel.recordMessageToken(it.result!!)
                }
            }
        }.start()
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        // Ignore
    }

    // https://stackoverflow.com/questions/5271448/how-to-detect-shake-event-with-android
    override fun onSensorChanged(event: SensorEvent?) {
        if (event?.sensor?.type == Sensor.TYPE_ACCELEROMETER) {
            val x = event.values[0]
			val y = event.values[1]
			val z = event.values[2]

			val gX = x / SensorManager.GRAVITY_EARTH
			val gY = y / SensorManager.GRAVITY_EARTH
			val gZ = z / SensorManager.GRAVITY_EARTH

			// gForce will be close to 1 when there is no movement.
			val gForce = Math.sqrt((gX * gX + gY * gY + gZ * gZ).toDouble())

			if (gForce > SHAKE_THRESHOLD_GRAVITY) {
				val now = System.currentTimeMillis()
				// ignore shake events too close to each other (500ms)
				if (mShakeTimestamp + SHAKE_SLOP_TIME_MS > now) {
					return
				}
				mShakeTimestamp = now
                onShake()
			}
          }
    }

    private fun onShake() {
        if(!displayingHint){
            displayingHint = true
            Thread {
                Game.board.forEach { _, s ->
                    s.foregroundResource = R.drawable.ic_blank_tile
                    s.showForeground = false
                }
                game!!.uciListener.output.clear()
                Uci.go("depth 7")
                var bestMove: String?
                while (true) {
                    bestMove = game!!.uciListener.output.lastOrNull()
                    if(bestMove != null){
                        if(bestMove.startsWith("bestmove")){
                            bestMove = bestMove.split(" ")[1]
                            Uci.stop()
                            break
                        } else if(bestMove.startsWith("info depth 7 currmove") && bestMove.endsWith("currmovenumber 1")){
                            bestMove = bestMove.split(" ")[4]
                            Uci.stop()
                            break
                        }
                    }
                    Log.d(TAG, "Bestmove: $bestMove")
                }
                val from = Coordinate.valueOf(bestMove!!.substring(0, 2))
                val to = Coordinate.valueOf(bestMove.substring(2, 4))
                with(Game.board[from]!!){
                    showForeground = true
                    foregroundResource = R.drawable.ic_square_hint
                }
                with(Game.board[to]!!){
                    showForeground = true
                    foregroundResource = R.drawable.ic_square_hint
                }
                activity?.runOnUiThread {
                    Log.d(TAG, "Displaying hint: $bestMove")
                    drawPosition()
                    displayingHint = false
                }
            }.start()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        moveSound.release()
        Log.i("Board", "Stopping stockfish")
        Uci.stop() // Need to make sure that stockfish stops thinking so that it doesn't crash
        // No guarantees, need to modify the native code to catch exception as a java exception instead of JNI exception
    }

    companion object {
        private const val SHAKE_THRESHOLD_GRAVITY = 2.7F
        private const val SHAKE_SLOP_TIME_MS = 500
    }
}
