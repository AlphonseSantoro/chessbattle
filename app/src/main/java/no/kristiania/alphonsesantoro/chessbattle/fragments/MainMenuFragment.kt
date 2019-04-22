package no.kristiania.alphonsesantoro.chessbattle.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.navigation.fragment.findNavController
import kotlinx.android.synthetic.main.fragment_main_menu.view.*
import no.kristiania.alphonsesantoro.chessbattle.R
import no.kristiania.alphonsesantoro.chessbattle.game.Color
import no.kristiania.alphonsesantoro.chessbattle.game.GameMode


class MainMenuFragment : BaseFragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_main_menu, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if(sharedViewModel.user?.email == "default@chessbattle.com"){
            view.playOnlineBtn.visibility = View.GONE
        } else {
            view.playOnlineBtn.visibility = View.VISIBLE
        }
        view.stockfishBtn.setOnClickListener {
            findNavController().navigate(
                R.id.boardFragment,
                bundleOf("gameMode" to GameMode.STOCKFISH, "perspective" to Color.random, "gameId" to -1L)
            )
        }
        view.twoPlayerBtn.setOnClickListener {
            findNavController().navigate(
                R.id.boardFragment,
                bundleOf("gameMode" to GameMode.TWO_PLAYER, "perspective" to Color.WHITE, "gameId" to -1L)
            )
        }
        view.playOnlineBtn.setOnClickListener {
            findNavController().navigate(
                R.id.findGameFragment,
                bundleOf("gameMode" to GameMode.LIVE, "gameId" to -1L)
            )
        }
    }
}
