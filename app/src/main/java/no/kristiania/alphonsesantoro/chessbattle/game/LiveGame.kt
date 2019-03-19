package no.kristiania.alphonsesantoro.chessbattle.game

import android.util.Log
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class LiveGame(perspective: Color, val gameKey: String?, gameStatus: GameStatus, onPieceMovedListener: OnPieceMovedListener) :
    Game(perspective, gameStatus, onPieceMovedListener = onPieceMovedListener) {
    private val firebase = FirebaseDatabase.getInstance()

    data class GameData(var lastMoveByColor: Color, var move: String, var moves: HashMap<String, String> = hashMapOf())

    init {
        setupLiveGame()
    }

    fun move(
        fromCoordinate: Coordinate,
        toCoordinate: Coordinate,
        promotePiece: Char?,
        liveMove: Boolean = false
    ): Boolean {
        if (move(fromCoordinate, toCoordinate, promotePiece)) {
            if (!liveMove) {
//                Thread {
//                    firebase.getReference("games/$gameKey")
//                        .setValue(GameData(perspective, "$fromCoordinate$toCoordinate"), moves)
//                }.start()
            }
            return true
        }
        return false
    }

    private fun setupLiveGame() {
        val gameRef = firebase.getReference("games/$gameKey")
        Thread {
            val gameListener = object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    val game = dataSnapshot.getValue(GameData::class.java)
                    if (game != null) {
//                        if (game.lastMoveByColor != perspective && game.move != moves[lastMoveNr.toString()]) {
//                            val promotePiece = if (game.move.length > 4) game.move[4] else null
//                            move(
//                                Coordinate.fromString(game.move.substring(0, 2))!!,
//                                Coordinate.fromString(game.move.substring(2, 4))!!,
//                                promotePiece,
//                                liveMove = true
//                            )
//                        }
                    }
                }

                override fun onCancelled(databaseError: DatabaseError) {
                    Log.w("GameListener", "loadPost:onCancelled", databaseError.toException())
                }
            }
            gameRef.addValueEventListener(gameListener)
        }.start()
    }
}