package no.kristiania.alphonsesantoro.chessbattle.database

import android.app.Application

class UserRepository(application: Application){
    private val database = AppDatabase.getAppDataBase(application)!!
    private val userDao = database.userDao()

    fun insert(userModel: UserModel): Long {
        return userDao.insert(userModel)
    }

    fun update(userModel: UserModel): Int {
        return userDao.update(userModel)
    }

    fun delete(userModel: UserModel) {
        return userDao.delete(userModel)
    }

    fun findUserByEmail(email: String): UserModel? {
        return userDao.findUserByEmail(email)
    }

    fun findUserByUserName(username: String): UserModel? {
        return userDao.findUserByUserName(username)
    }
}