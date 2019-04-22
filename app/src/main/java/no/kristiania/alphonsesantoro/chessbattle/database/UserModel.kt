package no.kristiania.alphonsesantoro.chessbattle.database

import androidx.room.*
import com.google.android.gms.maps.model.LatLng
import org.jetbrains.annotations.NotNull

@Entity(indices = [Index(value = ["id"]), Index(value = ["userName"], unique = true), Index(value = ["email"], unique = true)])
data class UserModel(
    @PrimaryKey(autoGenerate = true)
    val id: Long? = null,
    @NotNull
    var userName: String,
    @NotNull
    var email: String,
    var firebase_key: String?,
    var google_play_key: String?
): BaseModel()

@Dao
interface UserDao : BaseDao<UserModel> {
    @Query("SELECT * FROM UserModel WHERE id = :userId LIMIT 1")
    fun findUser(userId: Long): UserModel?

    @Query("SELECT * FROM UserModel WHERE userName = :userName LIMIT 1")
    fun findUserByUserName(userName: String): UserModel?

    @Query("SELECT * FROM UserModel WHERE email = :email LIMIT 1")
    fun findUserByEmail(email: String): UserModel?

    @Query("SELECT * FROM UserModel ORDER BY id DESC")
    fun all(): List<UserModel>
}