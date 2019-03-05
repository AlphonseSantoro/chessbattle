package no.kristiania.alphonsesantoro.chessbattle.unit

import jstockfish.Uci
import no.kristiania.alphonsesantoro.chessbattle.game.Color
import no.kristiania.alphonsesantoro.chessbattle.game.Coordinate
import no.kristiania.alphonsesantoro.chessbattle.game.Game
import no.kristiania.alphonsesantoro.chessbattle.game.pieces.Pawn
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Matchers.any
import org.powermock.api.mockito.PowerMockito
import org.powermock.core.classloader.annotations.PrepareForTest
import org.powermock.modules.junit4.PowerMockRunner
import org.mockito.Mockito
import org.powermock.core.classloader.annotations.SuppressStaticInitializationFor


@RunWith(PowerMockRunner::class)
@SuppressStaticInitializationFor("jstockfish.Uci")
@PrepareForTest(Uci::class)
class GameTest {

    @Test
    fun `can move a piece when when its white's turn`(){
        PowerMockito.mockStatic(Uci::class.java)
        Mockito.`when`(Uci.position(any())).thenReturn(true)
        Mockito.`when`(Uci.fen()).thenReturn("rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1")
        val gameMock = Mockito.spy(Game(null, null, false, true, false, Color.WHITE))
        gameMock.move(Coordinate.d2, Coordinate.d4)
        assertTrue(Game.board[Coordinate.d4]!!.piece is Pawn)
        assertTrue(Game.board[Coordinate.d2]?.piece == null)
    }

    @Test
    fun `can move a piece when when its black's turn`(){
        PowerMockito.mockStatic(Uci::class.java)
        Mockito.`when`(Uci.position(any())).thenReturn(true)
        Mockito.`when`(Uci.fen()).thenReturn("rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1")
        val gameMock = Mockito.spy(Game(null, null, false, true, false, Color.WHITE))
        Mockito.`when`(Uci.fen()).thenReturn("rnbqkbnr/pppppppp/8/8/3P4/8/PPP1PPPP/RNBQKBNR b KQkq - 0 1")
        gameMock.move(Coordinate.d2, Coordinate.d4)
        gameMock.move(Coordinate.d7, Coordinate.d5)
        assertTrue(Game.board[Coordinate.d5]!!.piece is Pawn)
        assertTrue(Game.board[Coordinate.d7]?.piece == null)
    }

    @Test
    fun `can not move a piece when when its black's turn`(){
        PowerMockito.mockStatic(Uci::class.java)
        Mockito.`when`(Uci.position(any())).thenReturn(true)
        Mockito.`when`(Uci.fen()).thenReturn("rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1")
        val gameMock = Mockito.spy(Game(null, null, false, true, false, Color.WHITE))
        Mockito.`when`(Uci.fen()).thenReturn("rnbqkbnr/pppppppp/8/8/3P4/8/PPP1PPPP/RNBQKBNR b KQkq - 0 1")
        gameMock.move(Coordinate.d2, Coordinate.d4)
        gameMock.move(Coordinate.e2, Coordinate.e4)
        assertTrue(Game.board[Coordinate.e4]?.piece == null)
    }
}