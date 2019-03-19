package no.kristiania.alphonsesantoro.chessbattle.database

import androidx.lifecycle.LiveData
import androidx.room.*
import org.jetbrains.annotations.NotNull

@Entity(
    indices = [Index(value = ["id"]), Index(value = ["gameId"])],
    foreignKeys = [ForeignKey(
        entity = GameModel::class,
        parentColumns = ["id"],
        childColumns = ["gameId"],
        onDelete = ForeignKey.CASCADE
    )]
)
data class GameLineModel(
    @PrimaryKey(autoGenerate = true)
    val id: Long? = null,
    @NotNull
    val whiteMove: String,
    var blackMove: String? = null,
    var whiteFen: String? = null,
    var blackFen: String? = null,
    @NotNull
    var gameId: Long
): BaseModel()

@Dao
interface GameLineDao : BaseDao<GameLineModel> {
    @Query("SELECT * FROM GameLineModel WHERE id = :id")
    fun findGameLine(id: Long): GameLineModel

    @Query("SELECT * FROM GameLineModel WHERE gameId = :gameId")
    fun all(gameId: Long): List<GameLineModel>

    @Query("SELECT * FROM GameLineModel WHERE gameId = :gameId")
    fun findGameLinesByGame(gameId: Long): LiveData<List<GameLineModel>>
}