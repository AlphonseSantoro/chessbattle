package no.kristiania.alphonsesantoro.chessbattle.fragments

import android.os.Bundle
import android.text.Editable
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.widget.addTextChangedListener
import androidx.navigation.fragment.findNavController
import com.google.firebase.auth.UserProfileChangeRequest
import kotlinx.android.synthetic.main.activity_chess_battle.*
import kotlinx.android.synthetic.main.fragment_sign_in.*
import kotlinx.android.synthetic.main.fragment_sign_in.view.*

import no.kristiania.alphonsesantoro.chessbattle.R
import no.kristiania.alphonsesantoro.chessbattle.database.AppDatabase
import no.kristiania.alphonsesantoro.chessbattle.database.UserRepository

const val emailRegex =
    "(?:[a-z0-9!#\$%&'*+/=?^_`{|}~-]+(?:\\.[a-z0-9!#\$%&'*+/=?^_`{|}~-]+)*|\"(?:[\\x01-\\x08\\x0b\\x0c\\x0e-\\x1f\\x21\\x23-\\x5b\\x5d-\\x7f]|\\\\[\\x01-\\x09\\x0b\\x0c\\x0e-\\x7f])*\")@(?:(?:[a-z0-9](?:[a-z0-9-]*[a-z0-9])?\\.)+[a-z0-9](?:[a-z0-9-]*[a-z0-9])?|\\[(?:(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.){3}(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?|[a-z0-9-]*[a-z0-9]:(?:[\\x01-\\x08\\x0b\\x0c\\x0e-\\x1f\\x21-\\x5a\\x53-\\x7f]|\\\\[\\x01-\\x09\\x0b\\x0c\\x0e-\\x7f])+)\\])"

class SignInFragment : BaseFragment() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {

        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val repository = UserRepository(activity!!.application)
        view.emailField.addTextChangedListener {
            if (it != null) {
                if (!it.matches(Regex(emailRegex))) {
                    email_validation.text = "Please enter a valid email"
                } else {
                    showFields(it)
                    email_validation.text = ""
                }
            }
        }

        view.passwordField.addTextChangedListener {
            if (it != null && it.length < 6) {
                password_validation.text = "Must be longer than 6 characters"
            } else {
                password_validation.text = ""
            }
        }

        view.userNameField.addTextChangedListener {
            if (it != null) {
                Thread {
                    val user = repository.findUserByUserName(it.toString())
                    if (user != null) {
                        username_validation.text = "Username is taken"
                    } else {
                        username_validation.text = ""
                    }
                }.start()
            }
        }

        view.signInBtn.setOnClickListener {
            loadingPanel.visibility = View.VISIBLE
            auth.signInWithEmailAndPassword(emailField.text.toString(), passwordField.text.toString())
                .addOnCompleteListener {
                    if (it.isSuccessful) {
                        sharedViewModel.setUser(auth.currentUser?.email)
                        findNavController().navigate(R.id.mainMenuFragment)
                    } else {
                        findNavController().navigate(R.id.signInFragment)
                        Toast.makeText(context, "Invalid credentials", Toast.LENGTH_LONG).show()
                    }
                }
        }

        view.registerBtn.setOnClickListener {
            loadingPanel.visibility = View.VISIBLE
            auth.createUserWithEmailAndPassword(emailField.text.toString(), passwordField.text.toString())
                .addOnCompleteListener {
                    if (it.isSuccessful) {
                        it.result!!.user.updateProfile(
                            UserProfileChangeRequest.Builder().setDisplayName(userNameField.text.toString()).build()
                        ).addOnCompleteListener {
                            if(it.isSuccessful){
                                sharedViewModel.setUser(auth.currentUser?.email)
                            }
                        }
                        findNavController().navigate(R.id.mainMenuFragment)
                    } else {
                        findNavController().navigate(R.id.signInFragment)
                        Log.w("Register", it.exception!!.message)
                        Toast.makeText(context, "Could not create user: ${it.exception?.message}", Toast.LENGTH_LONG).show()
                    }
                }
        }
    }

    private fun showFields(it: Editable) {
        Thread {
            val dataBase = AppDatabase.getAppDataBase(context!!.applicationContext)
            val user = dataBase?.userDao()?.findUserByEmail(it.toString())
            if (user == null) {
                activity?.runOnUiThread {
                    userNameBlock.visibility = View.VISIBLE
                    signInBtn.visibility = View.GONE
                    registerBtn.visibility = View.VISIBLE
                }
            } else {
                activity?.runOnUiThread {
                    userNameBlock.visibility = View.GONE
                    signInBtn.visibility = View.VISIBLE
                    registerBtn.visibility = View.GONE
                }
            }
        }.start()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_sign_in, container, false)
    }
}
