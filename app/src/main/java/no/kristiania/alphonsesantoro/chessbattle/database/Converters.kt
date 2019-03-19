package no.kristiania.alphonsesantoro.chessbattle.database

import androidx.room.TypeConverter
import no.kristiania.alphonsesantoro.chessbattle.game.GameStatus

class Converters {
    companion object {
        @TypeConverter
        @JvmStatic
        fun fromGameResult(gameResult: GameStatus): Int = gameResult.result

        @TypeConverter
        @JvmStatic
        fun toGameResult(int: Int): GameStatus = GameStatus.fromInt(int)
    }
}