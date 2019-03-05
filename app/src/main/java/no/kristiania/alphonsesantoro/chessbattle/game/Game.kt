package no.kristiania.alphonsesantoro.chessbattle.game

import android.app.Activity
import android.widget.ImageView
import android.widget.PopupWindow
import jstockfish.Uci
import jstockfish.Uci.fen
import jstockfish.Uci.position
import no.kristiania.alphonsesantoro.chessbattle.R
import no.kristiania.alphonsesantoro.chessbattle.game.pieces.*
import no.kristiania.alphonsesantoro.chessbattle.util.OutputListenerImpl
import no.kristiania.alphonsesantoro.chessbattle.util.asInt
import java.lang.IllegalArgumentException
import android.content.Context
import android.graphics.Point
import android.graphics.drawable.Drawable
import android.graphics.drawable.LayerDrawable
import android.view.*
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.DocumentReference
import kotlinx.android.synthetic.main.white_promote_popup.view.*


open class Game(
    val activity: Activity?,
    val view: View?,
    val live: Boolean = false,
    val twoPlayer: Boolean = false,
    val stockfish: Boolean = false,
    val perspective: Color,
    val computerColor: Color = if (perspective == Color.WHITE) Color.BLACK else Color.WHITE,
    val liveColor: Color? = null,
    val gameId: String? = null,
    val otherUsername: String? = null
) {

    companion object {
        var board = mutableMapOf<Coordinate, Square>()
        var isChecked = false
    }

    private var colorToMove: Color
    private var selectedPiece: Piece? = null
    private var lastMovedPiece: Piece? = null
    private var uciListener: OutputListenerImpl
    private var gameRef: DocumentReference? = null
    private var moves: MutableMap<String, String> = mutableMapOf()
    private var lastMoveNr: Int = 0

    init {
        newGame()
        Uci.position("startpos")
        colorToMove = Color.fromFen()
        selectedPiece = null
        lastMovedPiece = null
        uciListener = OutputListenerImpl
        Uci.setOutputListener(uciListener)
        drawPosition(false)

        if(stockfish && perspective != colorToMove) computerMove() // Stockfish is white, make a move
        setupLiveGame()
        setupNames()
    }

    private fun setupNames() {
        if(live) {
            view?.findViewById<TextView>(R.id.nameWhite)?.text = FirebaseAuth.getInstance().currentUser?.displayName
            view?.findViewById<TextView>(R.id.nameBlack)?.text = otherUsername
        }
        if(stockfish || twoPlayer) {
            view?.findViewById<LinearLayout>(R.id.whiteNameBlock)?.removeAllViews()
            view?.findViewById<LinearLayout>(R.id.blackNameBlock)?.removeAllViews()
        }
    }

    fun move(
        fromCoordinate: Coordinate,
        toCoordinate: Coordinate,
        promotePiece: Char? = null,
        liveMove: Boolean = false
    ): Boolean {
        var validMove = false
        val fenTokens = Uci.fen().split(" ")
        val enPassantPos = fenTokens[3]
        val canCastle = fenTokens[2]
        var move = "${fromCoordinate.name}${toCoordinate.name}"
        if (promotePiece != null) move += promotePiece
        if (isPromotion(fromCoordinate, toCoordinate) && promotePiece == null) {
            showPromotePopup(fromCoordinate, toCoordinate)
            return true
        }
        if (colorToMove == board[fromCoordinate]?.piece?.color && position("fen ${fen()} moves $move")) {
            castle(canCastle, fromCoordinate, toCoordinate)
            enPassant(enPassantPos, fromCoordinate, toCoordinate)
            board[fromCoordinate]!!.piece!!.showPossibleMoves(false, false)
            board[toCoordinate]!!.piece = board[fromCoordinate]!!.piece?.promote(promotePiece)
            board[toCoordinate]!!.piece!!.coordinate = toCoordinate
            lastMovedPiece = board[toCoordinate]!!.piece
            board[fromCoordinate]!!.piece = null
            colorToMove = Color.fromFen()
            if (stockfish && computerColor == colorToMove) computerMove()
            if (live && !liveMove) {
                Thread {
                    moves[(++lastMoveNr).toString()] = move
                    gameRef!!.update(mapOf("lastMoveByColor" to liveColor, "moves" to moves))
                }.start()
            }
            validMove = true
        }
        selectedPiece = null
        drawPosition()
        isGameOver()
        return validMove
    }

    private fun isGameOver() {
        var legalMovesCounter = 0
        isChecked = false
        board.forEach { coordinate, square ->
            if(square.piece != null){
                square.piece!!.showPossibleMoves(false, false)
                legalMovesCounter += square.piece!!.legalMovesCount
                square.piece!!.showPossibleMoves(false, true)
            }
        }
        if(legalMovesCounter == 0) {
            showGameOver()
        }
    }

    private fun showGameOver() {
        val alert = AlertDialog.Builder(ContextThemeWrapper(activity, R.style.AppTheme))
        alert.setTitle("Game Over")
        val win = if(colorToMove == perspective && isChecked){
            "You lost"
        } else if(colorToMove != perspective && isChecked) {
            "You win"
        } else "Stale mate"
        alert.setMessage(win)
        alert.show()
    }

    private fun isPromotion(fromCoordinate: Coordinate, toCoordinate: Coordinate): Boolean {
        return board[fromCoordinate]!!.piece is Pawn && board[fromCoordinate]!!.piece!!.promoteRank == toCoordinate.name[1].asInt
    }

    private fun showPromotePopup(fromCoordinate: Coordinate, toCoordinate: Coordinate) {
        val inflater = activity!!.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val coord = view!!.findViewById<ImageView>(
            view.resources.getIdentifier(
                toCoordinate.name,
                "id",
                view.context.packageName
            )
        )
        val point = Point()
        activity.windowManager.defaultDisplay.getSize(point)
        val layout = if (perspective == Color.WHITE) R.layout.white_promote_popup else R.layout.black_promote_popup
        val contentView = inflater.inflate(layout, coord.parent as ViewGroup, false)
        val pw = PopupWindow(contentView, point.x / 8, point.x / 2, true)
        pw.showAsDropDown(coord, Gravity.CENTER, 0, 0)
        contentView.promote_queen.setOnClickListener {
            move(fromCoordinate, toCoordinate, 'Q')
            pw.dismiss()
        }
        contentView.promote_rook.setOnClickListener {
            move(fromCoordinate, toCoordinate, 'R')
            pw.dismiss()
        }
        contentView.promote_knight.setOnClickListener {
            move(fromCoordinate, toCoordinate, 'N')
            pw.dismiss()
        }
        contentView.promote_bishop.setOnClickListener {
            move(fromCoordinate, toCoordinate, 'B')
            pw.dismiss()
        }
    }

    private fun enPassant(enPassantPos: String, fromCoordinate: Coordinate, toCoordinate: Coordinate) {
        if (enPassantPos != "-" && board[fromCoordinate]!!.piece is Pawn) {
            val moveDir = if (colorToMove == Color.WHITE) 1 else -1
            val capturePieceCoord = "${toCoordinate.name[0]}${toCoordinate.name[1] - 1 * moveDir}"
            board[Coordinate.valueOf(capturePieceCoord)]!!.piece = null
        }
    }

    private fun castle(canCastle: String, fromCoordinate: Coordinate, toCoordinate: Coordinate) {
        val movePos = "${fromCoordinate.name}${toCoordinate.name}"
        if (colorToMove == Color.WHITE && !canCastle.contains(Regex("[QK]")) ||
            colorToMove == Color.BLACK && !canCastle.contains(Regex("[kq]"))
        )
            return
        val castleMoves = arrayOf("e1c1", "e1g1", "e8c8", "e8g8")
        if (board[fromCoordinate]!!.piece is King && castleMoves.contains(movePos)) {
            when (toCoordinate) {
                Coordinate.c1 -> {
                    board[Coordinate.d1]!!.piece = board[Coordinate.a1]!!.piece
                    board[Coordinate.d1]!!.piece!!.coordinate = Coordinate.d1
                    board[Coordinate.a1]!!.piece = null
                }
                Coordinate.g1 -> {
                    board[Coordinate.f1]!!.piece = board[Coordinate.h1]!!.piece
                    board[Coordinate.f1]!!.piece!!.coordinate = Coordinate.f1
                    board[Coordinate.h1]!!.piece = null
                }
                Coordinate.c8 -> {
                    board[Coordinate.d8]!!.piece = board[Coordinate.a8]!!.piece
                    board[Coordinate.d8]!!.piece!!.coordinate = Coordinate.d8
                    board[Coordinate.a8]!!.piece = null
                }
                Coordinate.g8 -> {
                    board[Coordinate.f8]!!.piece = board[Coordinate.h8]!!.piece
                    board[Coordinate.f8]!!.piece!!.coordinate = Coordinate.h8
                    board[Coordinate.h8]!!.piece = null
                }
            }
        }
    }

    fun onSquareClick(square: ImageView) {
        val coord = Coordinate.valueOf(square.contentDescription.toString())
        if (live && liveColor != colorToMove) return // TODO: enable premoving
        if (selectedPiece != null) {
            move(selectedPiece!!.coordinate, coord)
        } else {
            if (colorToMove != board[coord]?.piece?.color) return
            selectedPiece = board[coord]!!.piece
            if (selectedPiece != null) {
                selectedPiece!!.showPossibleMoves(true, false)
                if(selectedPiece!!.legalMovesCount == 0) {
                    selectedPiece!!.showPossibleMoves(false, false)
                    selectedPiece = null
                    drawPosition(false)
                } else {
                    drawPosition(true)
                }
            }
        }
    }

    private fun computerMove() {
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
                activity!!.runOnUiThread { move(from, to) }
            } catch (e: IllegalArgumentException) {
                computerMove()
            }
        }.start()
    }

    fun drawPosition(showForeground: Boolean = false) {
        if (view != null) {
            board.forEach { coordinate, square ->
                val id = view.resources.getIdentifier(coordinate.name, "id", view.context.packageName)
                val squareView = view.findViewById<ImageView>(id)
                if (square.piece != null) {
                    squareView.setImageResource(square.piece!!.resource)
                } else squareView.setImageResource(square.emptySquareRes)
                val drawables = mutableListOf<Drawable>()
                if (showForeground) {
                    if (selectedPiece == square.piece) {
                        drawables.add(view.resources.getDrawable(R.drawable.ic_selected_square, view.context.theme))
                    }
                    drawables.add(view.resources.getDrawable(square.foregroundResource, view.context.theme))
                    squareView.foreground = LayerDrawable(drawables.toTypedArray())
                } else {
                    squareView.foreground = view.resources.getDrawable(R.drawable.ic_blank_tile, view.context.theme)
                    removeForeground()
                }
                if (square.piece != null && square.piece == lastMovedPiece) {
                    drawables.add(view.resources.getDrawable(R.drawable.ic_selected_square, view.context.theme))
                    squareView.foreground = LayerDrawable(drawables.toTypedArray())
                }
                squareView.setOnClickListener { onSquareClick(squareView) }
            }
        }
    }

    fun removeForeground() {
        board.forEach { coordinate, square -> square.foregroundResource = R.drawable.ic_blank_tile }
    }

    private fun newGame() {
        board = mutableMapOf()
        for (row in 8 downTo 1) {
            for (col in 'a'..'h') {
                board[Coordinate.valueOf("$col$row")] = Square(Coordinate.valueOf("$col$row"))
            }
        }
        placePieces()
    }

    private fun placePieces() {
        // White pieces
        board[Coordinate.a1]!!.piece = Rook(ResourcePiece.W_ROOK.resource, Color.WHITE, 'R', Coordinate.a1)
        board[Coordinate.b1]!!.piece = Knight(ResourcePiece.W_KNIGHT.resource, Color.WHITE, 'N', Coordinate.b1)
        board[Coordinate.c1]!!.piece = Bishop(ResourcePiece.W_BISHOP.resource, Color.WHITE, 'B', Coordinate.c1)
        board[Coordinate.d1]!!.piece = Queen(ResourcePiece.W_QUEEN.resource, Color.WHITE, 'Q', Coordinate.d1)
        board[Coordinate.e1]!!.piece = King(ResourcePiece.W_KING.resource, Color.WHITE, 'K', Coordinate.e1)
        board[Coordinate.f1]!!.piece = Bishop(ResourcePiece.W_BISHOP.resource, Color.WHITE, 'B', Coordinate.f1)
        board[Coordinate.g1]!!.piece = Knight(ResourcePiece.W_KNIGHT.resource, Color.WHITE, 'N', Coordinate.g1)
        board[Coordinate.h1]!!.piece = Rook(ResourcePiece.W_ROOK.resource, Color.WHITE, 'R', Coordinate.h1)

        for (col in 'a'..'h') {
            board[Coordinate.valueOf("${col}2")]!!.piece =
                Pawn(ResourcePiece.W_PAWN.resource, Color.WHITE, 'P', Coordinate.valueOf("${col}2"))
            board[Coordinate.valueOf("${col}7")]!!.piece =
                Pawn(ResourcePiece.B_PAWN.resource, Color.BLACK, 'p', Coordinate.valueOf("${col}7"))
        }

        // Black pieces
        board[Coordinate.a8]!!.piece = Rook(ResourcePiece.B_ROOK.resource, Color.BLACK, 'r', Coordinate.a8)
        board[Coordinate.b8]!!.piece = Knight(ResourcePiece.B_KNIGHT.resource, Color.BLACK, 'n', Coordinate.b8)
        board[Coordinate.c8]!!.piece = Bishop(ResourcePiece.B_BISHOP.resource, Color.BLACK, 'b', Coordinate.c8)
        board[Coordinate.d8]!!.piece = Queen(ResourcePiece.B_QUEEN.resource, Color.BLACK, 'q', Coordinate.d8)
        board[Coordinate.e8]!!.piece = King(ResourcePiece.B_KING.resource, Color.BLACK, 'k', Coordinate.e8)
        board[Coordinate.f8]!!.piece = Bishop(ResourcePiece.B_BISHOP.resource, Color.BLACK, 'b', Coordinate.f8)
        board[Coordinate.g8]!!.piece = Knight(ResourcePiece.B_KNIGHT.resource, Color.BLACK, 'n', Coordinate.g8)
        board[Coordinate.h8]!!.piece = Rook(ResourcePiece.B_ROOK.resource, Color.BLACK, 'r', Coordinate.h8)
    }

    private fun setupLiveGame() {
        if (live) {
            gameRef = FirebaseFirestore.getInstance().document("games/$gameId")
            Thread {
                gameRef!!.addSnapshotListener { documentSnapshot, exception ->
                    if (documentSnapshot!!.get("moves") != null && colorToMove != liveColor && Color.fromString(
                            documentSnapshot.getString("lastMoveByColor")
                        ) != liveColor
                    ) {
                        moves = documentSnapshot.get("moves") as MutableMap<String, String>
                        if (moves.isNotEmpty()) {
                            val lastMove = moves.entries.sortedByDescending { it.key.toInt() }.first()
                            lastMoveNr = lastMove.key.toInt()
                            val promotePiece = if (lastMove.value.length > 4) lastMove.value[4] else null
                            val liveMove = Move(
                                Coordinate.fromString(lastMove.value.substring(0, 2))!!,
                                Coordinate.fromString(lastMove.value.substring(2, 4))!!,
                                promotePiece
                            )
                            move(liveMove.fromCoordinate, liveMove.toCoordinate, liveMove.promotePiece, liveMove = true)
                        }
                    }
                }
            }.start()
        }
    }
}