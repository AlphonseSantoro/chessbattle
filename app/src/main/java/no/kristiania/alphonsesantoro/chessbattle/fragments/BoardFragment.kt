package no.kristiania.alphonsesantoro.chessbattle.fragments

import android.content.Context
import android.net.Uri
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import no.kristiania.alphonsesantoro.chessbattle.R

import no.kristiania.alphonsesantoro.chessbattle.game.Color
import no.kristiania.alphonsesantoro.chessbattle.game.Game
import android.widget.FrameLayout


private const val LIVE = "live"
private const val TWO_PLAYER = "two_player"
private const val STOCKFISH = "stockfish"
private const val WHITE = "white"
private const val BLACK = "black"
private const val GAME_ID = "game_id"
private const val PERSPECTIVE = "perspective"
private const val OTHER_USERNAME = "other_username"

class BoardFragment : Fragment() {
    private var listener: OnFragmentInteractionListener? = null
    private var live: Boolean = false
    private var stockfish: Boolean = false
    private var twoPlayer: Boolean = false
    private var white: String? = null
    private var black: String? = null
    private var perspective: Color = Color.WHITE
    private var gameId: String? = null
    private var currentUser: FirebaseUser? = null
    private var otherUserName: String? = null

    lateinit var game: Game

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            live = it.getBoolean(LIVE)
            stockfish = it.getBoolean(STOCKFISH)
            twoPlayer = it.getBoolean(TWO_PLAYER)
            white = it.getString(WHITE)
            black = it.getString(BLACK)
            gameId = it.getString(GAME_ID)
            perspective = it.get(PERSPECTIVE) as Color
            otherUserName = it.getString(OTHER_USERNAME)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        if (stockfish) perspective = Color.random
        val layout = if (perspective == Color.WHITE) R.layout.white_perspective else R.layout.black_perspective
        val view = inflater.inflate(R.layout.fragment_board, container, false)
        val boardPerspective = inflater.inflate(layout, null, false)
        val boardFrame = view.findViewById<FrameLayout>(R.id.boardFrame)
        boardFrame.addView(boardPerspective)
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        if (live) {
            currentUser = FirebaseAuth.getInstance().currentUser
        }
        game = if (live) {
            val color = if (currentUser!!.uid == white) Color.WHITE else Color.BLACK
            Game(
                activity!!,
                view,
                live,
                twoPlayer,
                stockfish,
                perspective,
                liveColor = color,
                gameId = gameId,
                otherUsername = otherUserName
            )
        } else {
            Game(activity!!, view, live, twoPlayer, stockfish, perspective, otherUsername = otherUserName)
        }
    }

    // TODO: Use Super class to navigate
    fun onButtonPressed(uri: Uri) {
        listener?.onFragmentInteraction(uri)
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
    }

    override fun onDetach() {
        super.onDetach()
        listener = null
    }

    override fun onDestroy() {
        super.onDestroy()
    }

    interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        fun onFragmentInteraction(uri: Uri)
    }
}
