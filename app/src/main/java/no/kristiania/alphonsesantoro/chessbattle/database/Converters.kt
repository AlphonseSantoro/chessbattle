package no.kristiania.alphonsesantoro.chessbattle.database

import androidx.room.TypeConverter
import com.google.android.gms.maps.model.LatLng
import no.kristiania.alphonsesantoro.chessbattle.game.GameStatus
import no.kristiania.alphonsesantoro.chessbattle.util.asDouble

class Converters {
    companion object {
        @TypeConverter
        @JvmStatic
        fun fromGameResult(gameResult: GameStatus): Int = gameResult.result

        @TypeConverter
        @JvmStatic
        fun toGameResult(int: Int): GameStatus = GameStatus.fromInt(int)


        @TypeConverter
        @JvmStatic
        fun fromLatLng(latLng: LatLng): String = "${latLng.latitude}:${latLng.longitude}"

        @TypeConverter
        @JvmStatic
        fun toLatLng(latLng: String): LatLng = LatLng(latLng.split(":")[0].asDouble, latLng.split(":")[1].asDouble)
    }
}