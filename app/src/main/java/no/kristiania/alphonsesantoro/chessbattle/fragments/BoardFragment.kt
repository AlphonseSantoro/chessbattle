package no.kristiania.alphonsesantoro.chessbattle.fragments

import android.content.Context
import android.net.Uri
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

import no.kristiania.alphonsesantoro.chessbattle.R
import no.kristiania.alphonsesantoro.chessbattle.game.Game

private const val LIVE = "live"
private const val TWO_PLAYER = "two_player"
private const val STOCKFISH = "stockfish"

class BoardFragment : Fragment() {
    private var listener: OnFragmentInteractionListener? = null
    private var live: Boolean = false
    private var twoPlayer: Boolean = false
    private var stockfish: Boolean = false

    lateinit var game: Game

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            live = it.getBoolean(LIVE)
            stockfish = it.getBoolean(STOCKFISH)
            twoPlayer = it.getBoolean(TWO_PLAYER)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_board, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        game = Game(activity!!, view, live, twoPlayer, stockfish)
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

    interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        fun onFragmentInteraction(uri: Uri)
    }
}
