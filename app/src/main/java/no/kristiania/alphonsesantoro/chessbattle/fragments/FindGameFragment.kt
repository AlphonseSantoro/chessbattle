package no.kristiania.alphonsesantoro.chessbattle.fragments

import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.location.Location
import androidx.lifecycle.ViewModelProviders
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

import no.kristiania.alphonsesantoro.chessbattle.R
import androidx.core.app.ActivityCompat
import androidx.core.os.bundleOf
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.model.LatLng
import kotlinx.android.synthetic.main.find_game_fragment.*
import androidx.navigation.findNavController
import com.firebase.ui.auth.AuthUI
import com.google.firebase.auth.FirebaseAuth
import no.kristiania.alphonsesantoro.chessbattle.viewmodels.FindGameViewModel


class FindGameFragment : BaseFragment(), OnMapReadyCallback {

    private lateinit var viewModel: FindGameViewModel
    private lateinit var mMap: GoogleMap
    private lateinit var mLastKnownLocation: Location

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
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProviders.of(this).get(FindGameViewModel::class.java)
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
//                viewModel.addMyLocation(mLastKnownLocation, activity!!)
            }
        }
        viewModel.showNearbyPlayers(mMap)
//        viewModel.listenForNewPlayers(mMap)

//        mMap.setOnMarkerClickListener {
//            viewModel.joinRoom(it.snippet, activity!!)
//
//        }
    }

    private fun checkPermissions(): Boolean {
        val accessFine = ActivityCompat.checkSelfPermission(context!!, android.Manifest.permission.ACCESS_FINE_LOCATION)
        val accessCoarse = ActivityCompat.checkSelfPermission(context!!, android.Manifest.permission.ACCESS_COARSE_LOCATION)
        if (accessFine != PackageManager.PERMISSION_GRANTED && accessCoarse != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(
                arrayOf(
                    android.Manifest.permission.ACCESS_COARSE_LOCATION,
                    android.Manifest.permission.ACCESS_FINE_LOCATION
                ), 1
            )
            activity!!.findNavController(R.id.fragment).navigate(R.id.findGameFragment, bundleOf())
            return false
        }
        return true
    }
}
