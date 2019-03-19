package no.kristiania.alphonsesantoro.chessbattle.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders
import com.google.firebase.auth.FirebaseAuth
import no.kristiania.alphonsesantoro.chessbattle.viewmodels.SharedViewModel

open class BaseFragment : Fragment(){

    internal lateinit var sharedViewModel: SharedViewModel
    internal val auth = FirebaseAuth.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        activity?.run {
            sharedViewModel = ViewModelProviders.of(this).get(SharedViewModel::class.java)
        }
    }
}