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
import no.kristiania.alphonsesantoro.chessbattle.database.UserModel
import java.util.*


class FindGameViewModel : ViewModel() {

    private val firebase = FirebaseDatabase.getInstance()
    private val currentUser = FirebaseAuth.getInstance().currentUser
    private val usersRef = firebase.getReference("users")
    private var nearbyUsers: MutableList<FirebaseUser> = mutableListOf()
    private var userListener: ValueEventListener? = null

    data class FirebaseUser(
        val uid: String? = null,
        val username: String? = null,
        val latLng: LatLng? = null,
        val isOnline: Boolean = false,
        val id: String = ""
    )

    fun addMyLocation(location: Location, user: UserModel) {
        if (currentUser != null) {
            val user = FirebaseUser(
                currentUser.uid,
                currentUser.displayName!!,
                LatLng(location.latitude, location.longitude),
                true,
                user.google_play_key!!
            )
            usersRef.updateChildren(mapOf(currentUser.uid to user))
        }
    }

    fun showNearbyPlayers(mMap: GoogleMap) {
        val query = firebase.getReference("users")

        if(userListener == null){
            userListener = object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    dataSnapshot.children.forEach {
                        val values = it.value as HashMap<String, Any>
                        val latLng = values["latLng"] as HashMap<String, String>
                        val user = FirebaseUser(
                            values["uid"].toString(),
                            values["username"].toString(),
                            LatLng(latLng["latitude"].toString().toDouble(), latLng["longitude"].toString().toDouble()),
                            values["uid"].toString().toBoolean(),
                            values["id"].toString()
                        )
                        if (user.uid != currentUser!!.uid) {
                            nearbyUsers.add(user)
                            mMap.addMarker(MarkerOptions().position(user.latLng!!).snippet(user.id))
                        }
                    }
                }

                override fun onCancelled(databaseError: DatabaseError) {
                    Log.w("FindGameViewModel", "loadPost:onCancelled", databaseError.toException())
                }
            }

            query.addValueEventListener(userListener!!)
        }
    }

//    fun listenForNewPlayers(mMap: GoogleMap): ListenerRegistration {
//        return query.addSnapshotListener { snapshot, exception ->
//            mMap.clear()
//            snapshot?.documents?.forEach {
//                if (it.id != currentUser?.uid)
//                    mMap.addMarker(
//                        MarkerOptions().position(
//                            LatLng(
//                                it["lat"].asDouble,
//                                it["long"].asDouble
//                            )
//                        ).snippet(it.id)
//                    )
//            }
//        }
//    }
//
}
