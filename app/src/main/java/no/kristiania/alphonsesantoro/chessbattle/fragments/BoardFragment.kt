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
import com.google.firebase.firestore.FirebaseFirestore
import no.kristiania.alphonsesantoro.chessbattle.R

import no.kristiania.alphonsesantoro.chessbattle.game.Color
import no.kristiania.alphonsesantoro.chessbattle.game.Game


private const val LIVE = "live"
private const val TWO_PLAYER = "two_player"
private const val STOCKFISH = "stockfish"
private const val WHITE = "white"
private const val BLACK = "black"
private const val GAME_ID = "game_id"

class BoardFragment : Fragment() {
    private var listener: OnFragmentInteractionListener? = null
    private var live: Boolean = false
    private var stockfish: Boolean = false
    private var twoPlayer: Boolean = false
    private var white: String? = null
    private var black: String? = null
    private var gameId: String? = null
    private var currentUser: FirebaseUser? = null

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
        if (live) {
            currentUser = FirebaseAuth.getInstance().currentUser
        }
        game = if (live) {
            val color = if (currentUser!!.uid == white) Color.WHITE else Color.BLACK
            Game(activity!!, view, live, twoPlayer, stockfish, liveColor = color, gameId = gameId)
        } else {
            Game(activity!!, view, live, twoPlayer, stockfish)
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
//        doUnbindService()
    }

    interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        fun onFragmentInteraction(uri: Uri)
    }

//    private fun doBindService(ip: String? = null, port: Int? = null, isHost: Boolean = false) {
//        val intent = Intent(context, SocketService::class.java)
//        intent.putExtra("ip", ip)
//        intent.putExtra("port", port)
//        intent.putExtra("isHost", isHost)
//        activity!!.bindService(intent, mConnection!!, Context.BIND_AUTO_CREATE)
//        println(mConnection)
//    }
//
//
//    private fun doUnbindService() {
//        println("DETACHING")
//        if (mConnection != null) activity!!.unbindService(mConnection!!)
//    }
//
//    inner class ServiceConnectionImpl : ServiceConnection {
//        override fun onServiceDisconnected(name: ComponentName?) {
//            mBoundService = null
//        }
//
//        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
//            println("Connected Service")
//            mBoundService = (service as SocketService.LocalBinder).service
//            Game.socketService = (service as SocketService.LocalBinder).service
//            mBoundService!!.inputStreamReader = mBoundService!!.socket!!.getInputStream().reader(Charsets.UTF_8)
//            mBoundService!!.outputStreamWriter = mBoundService!!.socket!!.getOutputStream().writer(Charsets.UTF_8)
//            game = Game(activity!!, view, live, twoPlayer, stockfish, liveColor = color)
//        }
//
//    }
}
