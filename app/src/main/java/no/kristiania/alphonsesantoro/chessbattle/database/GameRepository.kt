package no.kristiania.alphonsesantoro.chessbattle.database

import android.app.Application
import androidx.annotation.WorkerThread
import androidx.lifecycle.LiveData
import no.kristiania.alphonsesantoro.chessbattle.game.GameMode
import no.kristiania.alphonsesantoro.chessbattle.game.GameStatus
import java.lang.RuntimeException

class GameRepository(application: Application, userId: Long){
    private val database = AppDatabase.getAppDataBase(application)!!
    private val gameDao = database.gameDao()

    private var usersGames: LiveData<List<GameModel>>

    init {
        usersGames = findGamesByUserAndGameStatus(userId, GameStatus.INPROGRESS)
    }

    fun getUsersGames(): LiveData<List<GameModel>>{
        return usersGames
    }

    fun insert(gameModel: GameModel): Long {
        return gameDao.insert(gameModel)
    }

    fun update(gameModel: GameModel): Int {
        return gameDao.update(gameModel)
    }

    fun delete(gameModel: GameModel) {
        return gameDao.delete(gameModel)
    }

    fun find(id: Long): GameModel? {
        return gameDao.findGame(id)
    }

    fun findGamesByUser(userId: Long): List<GameModel>{
        return gameDao.allByUser(userId)
    }

    fun findGamesByUserAndGameStatus(userId: Long, gameStatus: GameStatus): LiveData<List<GameModel>> {
        return gameDao.findGamesByUserAndStatus(userId, gameStatus.result)
    }
}
