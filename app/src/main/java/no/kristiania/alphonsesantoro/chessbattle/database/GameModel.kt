package no.kristiania.alphonsesantoro.chessbattle.database

import androidx.lifecycle.LiveData
import androidx.room.*
import androidx.room.ForeignKey.CASCADE
import no.kristiania.alphonsesantoro.chessbattle.game.GameStatus
import org.jetbrains.annotations.NotNull

@Entity(
    indices = [Index(value = ["id"]), Index(value = ["userId"])],
    foreignKeys = [ForeignKey(
        entity = UserModel::class,
        parentColumns = ["id"],
        childColumns = ["userId"],
        onDelete = CASCADE
    )]
)
class GameModel(
    @PrimaryKey(autoGenerate = true)
    var id: Long? = null,
    @NotNull
    var white: String,
    @NotNull
    var black: String,
    var status: GameStatus,
    var userId: Long,
    var type: String,
    var perspective: String
)

@Dao
interface GameModelDao : BaseDao<GameModel> {
    @Query("SELECT * FROM GameModel WHERE status = :status AND userId = :userId")
    fun findGameModelsByResult(status: GameStatus, userId: Int): List<GameModel>

    @Query("SELECT * FROM GameModel WHERE id = :id")
    fun findGame(id: Long): GameModel?

    @Query("SELECT * FROM GameModel WHERE userId = :userId ORDER BY id DESC")
    fun allByUser(userId: Long): List<GameModel>

    @Query("SELECT * FROM GameModel WHERE userId = :userId AND type = 'stockfish' OR type = 'two_player' ORDER BY id DESC")
    fun findGamesByUser(userId: Long): LiveData<List<GameModel>>

    @Query("SELECT * FROM GameModel WHERE userId = :userId AND status = :gameStatus AND type IN ('STOCKFISH', 'TWO_PLAYER') ORDER BY id DESC")
    fun findGamesByUserAndStatus(userId: Long, gameStatus: Int): LiveData<List<GameModel>>
}