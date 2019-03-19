package no.kristiania.alphonsesantoro.chessbattle.adapters

import android.app.Activity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.appcompat.app.AlertDialog
import androidx.core.os.bundleOf
import androidx.navigation.Navigation.findNavController
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.recycler_game_item.view.*
import no.kristiania.alphonsesantoro.chessbattle.R
import no.kristiania.alphonsesantoro.chessbattle.database.GameModel
import no.kristiania.alphonsesantoro.chessbattle.database.GameRepository
import no.kristiania.alphonsesantoro.chessbattle.game.Color
import no.kristiania.alphonsesantoro.chessbattle.game.GameMode

class GamesInProgressRecyclerAdapter(
    var games: List<GameModel>,
    val activity: Activity,
    val alertdialog: AlertDialog
) :
    RecyclerView.Adapter<GamesInProgressRecyclerAdapter.GamesViewHolder>() {

    class GamesViewHolder(val view: View) : RecyclerView.ViewHolder(view) {
        val gameid = view.gameId
        val whiteName = view.whiteName
        val blackName = view.blackName
    }

    override fun onBindViewHolder(holder: GamesViewHolder, position: Int) {
        holder.gameid.text = "${games[position].id}"
        holder.whiteName.text = "${games[position].white}"
        holder.blackName.text = "${games[position].black}"
        holder.view.setOnClickListener {
            Thread {
                val gameModel = GameRepository(activity.application, -1).find(games[position].id!!)
                if (gameModel != null) {
                    alertdialog.dismiss()
                    findNavController(activity, R.id.fragment).navigate(
                        R.id.boardFragment,
                        bundleOf(
                            "gameMode" to GameMode.valueOf(gameModel.type),
                            "perspective" to Color.valueOf(gameModel.perspective),
                            "gameId" to gameModel.id
                        )
                    )
                }
            }.start()
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GamesViewHolder {
        val textView = LayoutInflater.from(parent.context)
            .inflate(R.layout.recycler_game_item, parent, false) as LinearLayout
        return GamesViewHolder(textView)
    }

    override fun getItemCount(): Int {
        return games.size
    }

    fun setData(gameList: List<GameModel>) {
        this.games = gameList
        notifyDataSetChanged()
    }
}