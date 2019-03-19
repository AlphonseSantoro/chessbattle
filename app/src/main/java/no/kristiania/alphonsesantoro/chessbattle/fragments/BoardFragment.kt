package no.kristiania.alphonsesantoro.chessbattle.fragments

import android.content.Context
import android.content.res.Configuration
import android.graphics.Point
import android.graphics.drawable.Drawable
import android.graphics.drawable.LayerDrawable
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import jstockfish.Uci
import jstockfish.Uci.fen
import kotlinx.android.synthetic.main.black_promote_popup.view.*
import kotlinx.android.synthetic.main.game_settings.view.*
import kotlinx.android.synthetic.main.game_settings_menu.view.*
import kotlinx.android.synthetic.main.landscape_board.view.*
import no.kristiania.alphonsesantoro.chessbattle.R
import no.kristiania.alphonsesantoro.chessbattle.adapters.MovesRecyclerAdapter
import no.kristiania.alphonsesantoro.chessbattle.game.*
import no.kristiania.alphonsesantoro.chessbattle.game.Color.*
import no.kristiania.alphonsesantoro.chessbattle.game.GameMode.*
import no.kristiania.alphonsesantoro.chessbattle.viewmodels.GameViewModel


class BoardFragment : BaseFragment() {
    private var recyclerView: RecyclerView? = null
    private var viewAdapter: RecyclerView.Adapter<MovesRecyclerAdapter.MovesViewHolder>? = null
    private var viewManager: RecyclerView.LayoutManager? = null

    private lateinit var viewModel: GameViewModel
    private var gameMode: GameMode = STOCKFISH
    private var perspective: Color = WHITE
    private var otherUsername: String? = null
    private lateinit var boardView: View
    private var orientation: Int = -1
    private var currentPerspectiveLayout: Int = 0
    private var currentMoveIndex: Int? = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Uci.newGame()
        arguments?.let {
            gameMode = it.get("gameMode") as GameMode
            if (it.get("perspective") != null) perspective = it.get("perspective") as Color
            otherUsername = it.getString("other_username")
        }
        currentPerspectiveLayout = if (perspective == WHITE) R.layout.white_perspective else R.layout.black_perspective
        viewModel = ViewModelProviders.of(this).get(GameViewModel::class.java)
        viewModel.user = sharedViewModel.user!!
        viewModel.bundle(arguments)
        setupGame()
        drawPosition()
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
                        viewModel.saveGame(GameStatus.RESIGNED)
                        viewModel.game!!.gameStatus = GameStatus.RESIGNED
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
            viewModel.gameLineRepository.all(viewModel.game!!.gameId).forEach {
                if (it.whiteFen != null) moveList.add(it.whiteFen!!)
                if (it.blackFen != null) moveList.add(it.blackFen!!)
            }
            if (currentMoveIndex!! > moveList.size - 1 || currentMoveIndex == null) currentMoveIndex = moveList.size - 1
            if (currentMoveIndex!! < 0) currentMoveIndex = 0

            activity?.runOnUiThread {
                drawPosition(Game.getBoardFromFen(moveList[currentMoveIndex!!]))
            }
        }.start()
    }


    fun onPostSetupGame() {
        viewModel.game?.turn?.observe(this, Observer {
            drawPosition()
            isGameOver()
        })
        viewModel.gameLineRepository.getGameLines().observe(this, Observer {
            if (it != null) (viewAdapter as MovesRecyclerAdapter).setData(it)
        })
        if (gameMode == STOCKFISH) (viewModel.game as StockfishGame).computerMove()
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

    override fun onSaveInstanceState(outState: Bundle) {
        if (arguments != null) outState.putAll(arguments)
        outState.putString("currentFen", fen())
        outState.putBoolean("restartGame", false)
        outState.putLong("gameId", viewModel.game!!.gameId)
        super.onSaveInstanceState(outState)
    }

    override fun onViewStateRestored(savedInstanceState: Bundle?) {
        super.onViewStateRestored(savedInstanceState)
        viewModel.bundle(savedInstanceState)
        setupGame()
        setRecyclerView(savedInstanceState)
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
                    if (viewModel.game?.selectedPiece == square.piece) {
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
                if (square.piece != null && square.piece == viewModel.game?.lastMovedPiece) {
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
        if (viewModel.game!!.gameStatus != GameStatus.INPROGRESS) return
        currentMoveIndex = null
        if (viewModel.game != null) {
            with(viewModel.game!!) {
                val coord = Coordinate.valueOf(squareView.contentDescription.toString())
                if (perspective != colorToMove && gameMode != TWO_PLAYER) return // TODO: enable premoving
                val square = Game.board[coord]!!
                if (selectedPiece != null && move(selectedPiece!!.coordinate, coord) && Game.isPromotion) {
                    showPromotePopup(viewModel.game?.fromCoordinate!!, viewModel.game?.toCoordinate!!)
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
            if (viewModel.game?.colorToMove == perspective && Game.isChecked || viewModel.game?.gameStatus == GameStatus.RESIGNED) {
                "You lost"
            } else if (viewModel.game?.colorToMove != perspective && Game.isChecked) {
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
            viewModel.game?.move(fromCoordinate, toCoordinate, 'Q')
            drawPosition()
            pw.dismiss()
        }
        contentView.promote_rook.setOnClickListener {
            viewModel.game?.move(fromCoordinate, toCoordinate, 'R')
            drawPosition()
            pw.dismiss()
        }
        contentView.promote_knight.setOnClickListener {
            viewModel.game?.move(fromCoordinate, toCoordinate, 'N')
            drawPosition()
            pw.dismiss()
        }
        contentView.promote_bishop.setOnClickListener {
            viewModel.game?.move(fromCoordinate, toCoordinate, 'B')
            drawPosition()
            pw.dismiss()
        }
    }

    fun setupNames(view: View) {
        when (gameMode) {
            LIVE -> {
                // TODO: Get game names
                if (perspective == WHITE) {
                    setNames(view, viewModel.user.userName, "")
                } else {
                    setNames(view, "", viewModel.user.userName)
                }
            }
            STOCKFISH -> {
                if (perspective == WHITE) {
                    setNames(view, viewModel.user.userName, "Stockfish")
                } else {
                    setNames(view, "Stockfish", viewModel.user.userName)
                }
            }
            TWO_PLAYER -> {
                if (perspective == WHITE) {
                    setNames(view, viewModel.user.userName, getString(R.string.playerTwo))
                } else {
                    setNames(view, getString(R.string.playerTwo), viewModel.user.userName)
                }
            }
        }
    }

    fun setNames(view: View, whiteName: String, blackName: String) {
        viewModel.white = whiteName
        viewModel.black = blackName
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
            viewModel.setupGame()
            viewModel.saveGame()
            setUciFenFromMoves()
            Game.board = Game.getBoardFromFen(Uci.fen())
            setRecyclerView()
            activity?.runOnUiThread { onPostSetupGame() }
        }.start()
    }

    private fun setUciFenFromMoves() {
        val moves = viewModel.gameLineRepository.all(viewModel.game!!.gameId)
        if (moves.isNotEmpty()) {
            var position = "startpos moves"
            moves.forEach { position += " ${it.whiteMove} ${it.blackMove}" }
            Uci.position(position)
        } else {
            Uci.position("startpos")
        }
    }

    private fun setRecyclerView(savedInstanceState: Bundle? = null) {
//        if (savedInstanceState != null && viewModel.game!!.moves.isEmpty()) {
//            savedInstanceState.getStringArray("moves")?.mapIndexed { i, m ->
//                viewModel.game!!.moves[i.toString()] = m
//            }
//        }
        viewManager = LinearLayoutManager(context)
        viewAdapter = MovesRecyclerAdapter(emptyList())
        recyclerView = view?.findViewById(R.id.moveList)
        if (recyclerView != null) {
            recyclerView!!.layoutManager = viewManager
            recyclerView!!.adapter = viewAdapter
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.i("Board", "Stopping stockfish")
        Uci.stop() // Need to make sure that stockfish stops thinking so that it doesn't crash
        // No guarantees, need to modify the native code to catch exception as a java exception instead of JNI exception
    }
}
