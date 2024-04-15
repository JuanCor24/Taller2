package com.example.taller2.activities


import android.Manifest
import android.app.Activity
import android.content.pm.PackageManager
import android.location.Address
import android.location.Location
import android.os.Bundle
import android.os.Looper
import android.util.Log
import android.view.inputmethod.EditorInfo
import android.widget.EditText
import android.widget.SearchView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.Granularity
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.maps.model.LatLng
import com.google.android.material.textfield.TextInputLayout
import com.example.taller2.R

import com.example.taller2.databinding.ActivityMapaBinding
import com.example.taller2.fragments.MapsFragment
import com.example.taller2.utils.Alerts
import com.example.taller2.utils.GeocoderSearch
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions




class MapsActivity : AppCompatActivity(), OnMapReadyCallback,GoogleMap.OnMarkerClickListener{
    private lateinit var binding: ActivityMapaBinding
    var TAG = MainActivity::class.java.name
    private lateinit var mMap: GoogleMap
    private var alerts: Alerts = Alerts(this)
    private lateinit var geocoderSearch: GeocoderSearch
    private val PERM_LOCATION_CODE = 101
    private lateinit var currentLocation: Location
    private lateinit var fragment: MapsFragment
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var locationRequest: LocationRequest
    private lateinit var locationCallback: LocationCallback
    private lateinit var searchView:SearchView;
    companion object{
        private const val  LOCATION_REQUEST_CODE = 1
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMapaBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val mapFragment = supportFragmentManager.findFragmentById(R.id.mapFragment) as SupportMapFragment
        mapFragment.getMapAsync(this)

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)


    }




    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        mMap.uiSettings.isZoomControlsEnabled = true
        mMap.setOnMarkerClickListener (this)
        getCurrentLocationUser()
    }

    private fun getCurrentLocationUser() {
        if (ActivityCompat.checkSelfPermission(
                this,
                android.Manifest.permission.ACCESS_FINE_LOCATION
            ) !=
            PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION),
                LOCATION_REQUEST_CODE)
            return
        }
        mMap.uiSettings.isZoomControlsEnabled = true


        fusedLocationClient.lastLocation.addOnSuccessListener(this) {
                location ->
            if(location != null){
                currentLocation = location
                val LatLong = LatLng(location.latitude,location.longitude)
                placeMarkeOnMap(LatLong)
                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(LatLong,12f))
            }
        }

    }

    private fun placeMarkeOnMap(latLong: LatLng) {
        val markerOptions = MarkerOptions().position(latLong)
        markerOptions.title("$latLong")
        mMap.addMarker(MarkerOptions())
    }





    override fun onMarkerClick(p0: Marker) = false
}