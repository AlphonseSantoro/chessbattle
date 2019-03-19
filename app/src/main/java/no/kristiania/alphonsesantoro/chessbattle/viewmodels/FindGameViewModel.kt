package no.kristiania.alphonsesantoro.chessbattle.viewmodels

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel;
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import no.kristiania.alphonsesantoro.chessbattle.database.AppDatabase


class FindGameViewModel : ViewModel() {

    private val firebase = FirebaseDatabase.getInstance()
    private val currentUser = FirebaseAuth.getInstance().currentUser
    private val query = firebase.getReference("users")
    private var nearbyUsers: MutableList<FirebaseUser> = mutableListOf()
//        .whereGreaterThan("locatedAt", Timestamp(Date(System.currentTimeMillis() - 3600 * 1000)))
//    private val roomDocument = firebase.document("rooms/${currentUser?.uid}")


    data class FirebaseUser(val uid: String, val username: String, val latLng: LatLng, val isOnline: Boolean)

    fun showNearbyPlayers(mMap: GoogleMap) {
        val query= firebase.getReference("users")

        val listener = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                dataSnapshot.children.forEach {
                    val user = dataSnapshot.getValue(FirebaseUser::class.java)!!
                    nearbyUsers.add(user)
                    if(user.uid != currentUser!!.uid) {
                        mMap.addMarker(MarkerOptions().position(user.latLng).snippet(user.username))
                    }
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                Log.w("FindGameViewModel", "loadPost:onCancelled", databaseError.toException())
            }
        }
        query.addValueEventListener(listener)
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
//    fun addMyLocation(location: Location, activity: FragmentActivity) {
//        if (currentUser != null) {
//            firestore.collection("users").document(currentUser.uid)
//                .set(mapOf("lat" to location.latitude, "long" to location.longitude, "locatedAt" to Timestamp.now()))
//            roomDocument.set(mapOf("playerOne" to currentUser.uid, "playerTwo" to null))
//            var haveRead = false
//            roomDocument.addSnapshotListener(activity) { documentSnapshot, _ ->
//                if (documentSnapshot?.getString("playerTwo") != null && !haveRead) {
//                    haveRead = true
//                    val alert = AlertDialog.Builder(ContextThemeWrapper(activity, R.style.AppTheme))
//                    alert.setMessage("You have been challenged by ${documentSnapshot["playerTwo"]}")
//                    alert.setTitle("Battle")
//                    alert.setPositiveButton("Accept") { dialog, which ->
//                        dialog.dismiss()
//                        val playerList = listOf(documentSnapshot["playerOne"], documentSnapshot["playerTwo"])
//                        val white = playerList.random().toString()
//                        val black = playerList.first { it.toString() != white }.toString()
//                        firestore.collection("games")
//                            .add(mapOf("white" to black, "black" to black, "moves" to emptyMap<String, String>()))
//                            .addOnSuccessListener {
//                                roomDocument.update(mapOf("white" to white, "black" to black, "gameId" to it.id))
//                                activity.findNavController(R.id.fragment).navigate(
//                                    R.id.boardFragment,
//                                    bundleOf(
//                                        "live" to true,
//                                        "playerOne" to documentSnapshot.id,
//                                        "playerTwo" to documentSnapshot["playerTwo"],
//                                        "white" to white,
//                                        "black" to black,
//                                        "gameKey" to it.id,
//                                        "perspective" to if(white == currentUser.uid) Color.WHITE else Color.BLACK
//                                    )
//                                )
//                            }
//                    }
//                    alert.setNegativeButton("Reject") { dialog, which ->
//                        dialog.dismiss()
//                        roomDocument.update(mapOf("playerOne" to currentUser.uid, "playerTwo" to null))
//                    }
//                    alert.setCancelable(true)
//                    alert.create().show()
//                }
//            }
//        }
//    }
//
//    fun joinRoom(roomId: String, activity: FragmentActivity): Boolean {
//        val room = firestore.document("rooms/$roomId")
//        room.addSnapshotListener { documentSnapshot, _ ->
//            if (documentSnapshot?.getString("white") != null && documentSnapshot.getString("black") != null) {
//                activity.findNavController(R.id.fragment).navigate(
//                    R.id.boardFragment,
//                    bundleOf(
//                        "live" to true,
//                        "playerTwo" to documentSnapshot["playerTwo"],
//                        "playerOne" to documentSnapshot["playerOne"],
//                        "white" to documentSnapshot["white"],
//                        "other_username" to currentUser?.displayName,
//                        "black" to documentSnapshot["black"],
//                        "game_id" to documentSnapshot["gameId"],
//                        "perspective" to if(documentSnapshot["white"] == currentUser!!.uid) Color.WHITE else Color.BLACK
//                    )
//                )
//            }
//            if (documentSnapshot?.getString("playerTwo") == null) {
//                activity.findNavController(R.id.fragment).navigate(R.id.findGameFragment, bundleOf()) // Rejected
//            }
//        }
//        room.get().addOnSuccessListener {
//            if (it.getString("playerTwo").isNullOrBlank()) {
//                room.update(mapOf("playerTwo" to currentUser?.uid))
//            }
//        }
//        return true
//    }
}
