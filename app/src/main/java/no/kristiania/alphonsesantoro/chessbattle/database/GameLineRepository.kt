package no.kristiania.alphonsesantoro.chessbattle.database

import android.app.Application
import androidx.lifecycle.LiveData

class GameLineRepository(application: Application, gameId: Long){
    private val database = AppDatabase.getAppDataBase(application)!!
    private val gameLineDao = database.gameLineDao()

    private var gameLines: LiveData<List<GameLineModel>>

    init {
        gameLines = gameLineDao.findGameLinesByGame(gameId)
    }

    fun getGameLines(): LiveData<List<GameLineModel>> {
        return gameLines
    }

    fun all(gameId: Long): List<GameLineModel> {
        return gameLineDao.all(gameId)
    }

    fun insert(gameModel: GameLineModel): Long {
        return gameLineDao.insert(gameModel)
    }

    fun update(gameModel: GameLineModel): Int {
        return gameLineDao.update(gameModel)
    }

    fun delete(gameModel: GameLineModel) {
        return gameLineDao.delete(gameModel)
    }

    fun find(id: Long): GameLineModel? {
        return gameLineDao.findGameLine(id)
    }
}
