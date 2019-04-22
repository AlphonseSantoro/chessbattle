package no.kristiania.alphonsesantoro.chessbattle.viewmodels

import android.location.Location
import android.util.Log
import androidx.lifecycle.ViewModel
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.gson.Gson
import no.kristiania.alphonsesantoro.chessbattle.database.UserModel
import java.util.*


class FindGameViewModel : ViewModel() {

    private val firebase = FirebaseDatabase.getInstance()
    private val currentUser = FirebaseAuth.getInstance().currentUser
    private val usersRef = firebase.getReference("users")
    private var nearbyUsers: MutableMap<String, Marker> = mutableMapOf()
    private var userListener: ValueEventListener? = null

    data class FirebaseUser(
        val uid: String? = null,
        val username: String? = null,
        val latLng: LatLng? = null,
        val isOnline: Boolean = false,
        val id: String = ""
    )

    fun showNearbyPlayers(mMap: GoogleMap) {
        if (userListener == null) {
            userListener = object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    dataSnapshot.children.forEach { updateMarker(mMap, it) }
                }

                override fun onCancelled(databaseError: DatabaseError) {
                    Log.w("FindGameViewModel", "loadPost:onCancelled", databaseError.toException())
                }
            }

            usersRef.addValueEventListener(userListener!!)
        }
    }

    fun updateMarker(mMap: GoogleMap, it: DataSnapshot) {
        val values = it.value as HashMap<String, Any>
        val latLng = values["latLng"] as HashMap<String, String>
        val user = FirebaseUser(
            uid = values["uid"].toString(),
            username = values["username"].toString(),
            latLng = LatLng(latLng["latitude"].toString().toDouble(), latLng["longitude"].toString().toDouble()),
            isOnline = values["online"].toString().toBoolean(),
            id = values["id"].toString()
        )
        Log.d("FindGame", user.toString())
        if (user.uid != currentUser!!.uid && user.isOnline) {
            nearbyUsers[user.uid!!] =
                mMap.addMarker(MarkerOptions().position(user.latLng!!).snippet(Gson().toJson(user)))
        } else {
            nearbyUsers[user.uid!!]?.remove()
        }
    }

    fun updateStatus(user: UserModel, location: Location, online: Boolean) {
        if (currentUser != null) {
            val firebaseUser = FirebaseUser(
                currentUser.uid,
                currentUser.displayName!!,
                LatLng(location.latitude, location.longitude),
                online,
                user.google_play_key!!
            )
            usersRef.updateChildren(mapOf(currentUser.uid to firebaseUser))
        }
    }
}
