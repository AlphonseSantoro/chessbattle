package no.kristiania.alphonsesantoro.chessbattle.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import no.kristiania.alphonsesantoro.chessbattle.database.AppDatabase
import no.kristiania.alphonsesantoro.chessbattle.database.UserModel
import no.kristiania.alphonsesantoro.chessbattle.database.UserRepository

class SharedViewModel(application: Application) : AndroidViewModel(application) {
    var user: UserModel? = null
    val auth = FirebaseAuth.getInstance()
    var repository = UserRepository(application)

    internal fun setUser(email: String?): Thread {
        val thread = Thread {
            val mail = email ?: defaultEmail
            user = repository.findUserByEmail(mail)
            if (user == null && auth.currentUser != null || user!!.email == defaultEmail && auth.currentUser != null) {
                user = UserModel(
                    userName = auth.currentUser!!.displayName!!,
                    email = auth.currentUser!!.email!!,
                    firebase_key = auth.currentUser!!.uid
                )
                repository.insert(user!!)
                user = repository.findUserByEmail(mail)
            }
        }
        thread.start()
        return thread
    }

    fun updateUser(currentUser: FirebaseUser?) {
        if (currentUser != null) {
            Thread {
                val dataBase = AppDatabase.getAppDataBase(getApplication())
                val user = dataBase?.userDao()?.findUserByEmail(currentUser.email!!)!!
                user.email = currentUser.email!!
                user.firebase_key = currentUser.uid
                user.userName = currentUser.displayName!!
                dataBase.userDao().update(user)
            }.start()
        }
    }

    companion object {
        const val defaultEmail = "default@chessbattle.com"
    }
}