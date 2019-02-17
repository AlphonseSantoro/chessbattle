package no.kristiania.alphonsesantoro.chessbattle

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import jstockfish.Position
import jstockfish.Uci

class ChessBattleActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chess_battle)
//        Uci.uci()
//        Uci.newGame()
//        println(Uci.fen())
//        println(Position.isLegal(false, "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1", "e2e4"))
//        print(Uci.d())
//        println()
    // Example of a call to a native method
//    sample_text.text = Uci.uci()
    }

    companion object {
//
//        init {
//            System.loadLibrary("jstockfish")
//        }
    }
}
