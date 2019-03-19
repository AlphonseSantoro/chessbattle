package no.kristiania.alphonsesantoro.chessbattle.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.recycler_list_item.view.*
import no.kristiania.alphonsesantoro.chessbattle.R
import no.kristiania.alphonsesantoro.chessbattle.database.GameLineModel

class MovesRecyclerAdapter(var movesList: List<GameLineModel>) : RecyclerView.Adapter<MovesRecyclerAdapter.MovesViewHolder>() {
    class MovesViewHolder(val view: View) : RecyclerView.ViewHolder(view) {
        val whiteMoveText = view.whiteMoveText
        val blackMoveText = view.blackMoveText
    }

    override fun onBindViewHolder(holder: MovesViewHolder, position: Int) {
        holder.whiteMoveText.text = "${position+1}. ${movesList[position].whiteMove}"
        if(movesList[position].blackMove != null){
            holder.blackMoveText.text = "${movesList[position].blackMove}"
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MovesViewHolder {
        val textView = LayoutInflater.from(parent.context)
            .inflate(R.layout.recycler_list_item, parent, false) as LinearLayout
        return MovesViewHolder(textView)
    }

    override fun getItemCount(): Int {
        return movesList.size
    }

    fun setData(movesList: List<GameLineModel>) {
        this.movesList = movesList
        notifyDataSetChanged()
    }
}