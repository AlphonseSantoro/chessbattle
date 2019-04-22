package no.kristiania.alphonsesantoro.chessbattle.fragments

import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.location.Location
import androidx.lifecycle.ViewModelProviders
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog

import no.kristiania.alphonsesantoro.chessbattle.R
import androidx.core.app.ActivityCompat
import androidx.core.os.bundleOf
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.model.LatLng
import kotlinx.android.synthetic.main.find_game_fragment.*
import no.kristiania.alphonsesantoro.chessbattle.game.Color.*
import no.kristiania.alphonsesantoro.chessbattle.game.GameMode
import no.kristiania.alphonsesantoro.chessbattle.viewmodels.FindGameViewModel
import androidx.navigation.fragment.findNavController
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.games.Games
import com.google.android.gms.games.RealTimeMultiplayerClient
import com.google.android.gms.games.multiplayer.Invitation
import com.google.android.gms.games.multiplayer.InvitationCallback
import com.google.android.gms.games.multiplayer.realtime.RoomConfig
import com.google.gson.Gson

class FindGameFragment : BaseFragment(), OnMapReadyCallback {
    private val TAG = "FindGame"

    private lateinit var viewModel: FindGameViewModel
    private lateinit var mMap: GoogleMap
    private lateinit var mLastKnownLocation: Location
    private lateinit var mRealTimeMultiplayerClient: RealTimeMultiplayerClient

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.find_game_fragment, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        map.onCreate(savedInstanceState)
        map.onResume()
        map.getMapAsync(this)
        viewModel = ViewModelProviders.of(this).get(FindGameViewModel::class.java)

        mRealTimeMultiplayerClient = Games.getRealTimeMultiplayerClient(context!!, GoogleSignIn.getLastSignedInAccount(activity)!!)
        sharedViewModel.mRealTimeMultiplayerClient = mRealTimeMultiplayerClient

        Games.getInvitationsClient(activity!!, GoogleSignIn.getLastSignedInAccount(activity!!)!!)
            .registerInvitationCallback(mInvitationCallbackHandler)
    }

    private fun sendInvite(user: FindGameViewModel.FirebaseUser): Boolean {
        sharedViewModel.acceptedGame = false
        sharedViewModel.declinedGame = false
        sharedViewModel.mRoomConfig = RoomConfig.builder(sharedViewModel.mRoomUpdateCallback)
            .setOnMessageReceivedListener(sharedViewModel.mMessageReceivedHandler)
            .setRoomStatusUpdateCallback(sharedViewModel.mRoomStatusCallbackHandler)
            .addPlayersToInvite(user.id).build()

        mRealTimeMultiplayerClient.create(sharedViewModel.mRoomConfig).addOnSuccessListener {
            navigateTurnBased("Challenging ${user.username}", null)
        }
        return true
    }

    private fun navigateTurnBased(title: String, participantId: String?) {
        val alert = AlertDialog.Builder(context!!)
        alert.setTitle(title)
        alert.setView(R.layout.loading_panel)
        val dialog = alert.show()
        Thread {
            while (!sharedViewModel.acceptedGame){
                // Wait for player to join
                if(sharedViewModel.declinedGame){
                    // Declined no not navigate
                    dialog.dismiss()
                    return@Thread
                }
            }
            // set default view to white perspective, then when the other player joins update UI
            dialog.dismiss()
            findNavController().navigate(
                R.id.boardFragment,
                bundleOf(
                    "gameMode" to GameMode.LIVE,
                    "white" to sharedViewModel.white,
                    "black" to sharedViewModel.black,
                    "participantId" to participantId,
                    "perspective" to if(sharedViewModel.white == sharedViewModel.user!!.userName) WHITE else BLACK,
                    "gameId" to -1L
                )
            )
        }.start()
    }

    private val mInvitationCallbackHandler = object : InvitationCallback() {
        override fun onInvitationRemoved(p0: String) {
            Log.d(TAG, p0)
        }

        override fun onInvitationReceived(invitation: Invitation) {
            Log.d(TAG, invitation.invitationId)
            val alert = AlertDialog.Builder(context!!)
            alert.setTitle("Challenge")
            alert.setMessage("${invitation.inviter.displayName} has challenged you to a game")
            alert.setPositiveButton("Accept") { dialog, _ ->
                sharedViewModel.mRoomConfig = RoomConfig.builder(sharedViewModel.mRoomUpdateCallback)
                    .setOnMessageReceivedListener(sharedViewModel.mMessageReceivedHandler)
                    .setRoomStatusUpdateCallback(sharedViewModel.mRoomStatusCallbackHandler)
                    .setInvitationIdToAccept(invitation.invitationId)
                    .build()
                mRealTimeMultiplayerClient.join(sharedViewModel.mRoomConfig)
                    .addOnSuccessListener {
                        navigateTurnBased("Joining game...", invitation.inviter.participantId)
                    }
                dialog.dismiss()
            }
            alert.setNegativeButton("Decline") { dialog, _ ->
                mRealTimeMultiplayerClient.declineInvitation(invitation.invitationId)
                sharedViewModel.declinedGame = true
                dialog.dismiss()
            }
            alert.create().show()
        }
    }

    @SuppressLint("MissingPermission")
    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        if (checkPermissions()) {
            mMap.isMyLocationEnabled = true
            mMap.uiSettings.isMyLocationButtonEnabled = true
            val locationProviderClient = LocationServices.getFusedLocationProviderClient(context!!)
            val lastLocation = locationProviderClient.lastLocation
            lastLocation.addOnSuccessListener {
                mLastKnownLocation = it
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(LatLng(it.latitude, it.longitude), 14f))
                viewModel.updateStatus(sharedViewModel.user!!, mLastKnownLocation, true)
            }
        }
        viewModel.showNearbyPlayers(mMap)
        mMap.setOnMarkerClickListener {
            sendInvite(Gson().fromJson(it.snippet, FindGameViewModel.FirebaseUser::class.java))
        }
    }

    private fun checkPermissions(): Boolean {
        val accessFine = ActivityCompat.checkSelfPermission(context!!, android.Manifest.permission.ACCESS_FINE_LOCATION)
        val accessCoarse =
            ActivityCompat.checkSelfPermission(context!!, android.Manifest.permission.ACCESS_COARSE_LOCATION)
        if (accessFine != PackageManager.PERMISSION_GRANTED && accessCoarse != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(
                arrayOf(
                    android.Manifest.permission.ACCESS_COARSE_LOCATION,
                    android.Manifest.permission.ACCESS_FINE_LOCATION
                ), 1
            )
            findNavController().navigate(R.id.findGameFragment, bundleOf())
            return false
        }
        return true
    }

    override fun onDestroyView() {
        super.onDestroyView()
        viewModel.updateStatus(sharedViewModel.user!!, mLastKnownLocation, false)
    }

    override fun onDetach() {
        super.onDetach()
        viewModel.updateStatus(sharedViewModel.user!!, mLastKnownLocation, false)
    }

    override fun onDestroy() {
        super.onDestroy()
        viewModel.updateStatus(sharedViewModel.user!!, mLastKnownLocation, false)
    }
}
