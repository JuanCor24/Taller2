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




class MapsActivity : AppCompatActivity(){
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

        setupLocation()
        when {
            ContextCompat.checkSelfPermission(
                this, Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED -> {
                startLocationUpdates()
            }

            shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_FINE_LOCATION) -> {
                alerts.indefiniteSnackbar(
                    binding.root,
                    "El permiso de Localizacion es necesario para usar esta actividad 😭"
                )

            }

            else -> {
                requestPermissions(
                    arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                    PERM_LOCATION_CODE
                )
            }
        }

         val mapFragment = supportFragmentManager.findFragmentById(R.id.mapFragment) as MapsFragment

        geocoderSearch = GeocoderSearch(this)


        binding.searchView.editText?.setOnEditorActionListener { v, actionId, event ->
            return@setOnEditorActionListener when (actionId) {
                EditorInfo.IME_ACTION_SEARCH -> {
                    val text = binding.searchView.editText?.text.toString()
                    val address: MutableList<Address> = geocoderSearch.finPlacesByNameInRadius(
                        text,
                        LatLng(currentLocation.latitude, currentLocation.longitude)
                    )!!
                    address.forEach() {
                        var title = text
                        var desc =
                            if (it.getAddressLine(0).isNullOrEmpty()) it.getAddressLine(0) else ""

                    }
                    true
                }

                else -> false
            }
        }
    }


//klskdladk
private fun setupLocation() {
    fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
    locationRequest = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 1000).apply {
        setMinUpdateDistanceMeters(5F)
        setGranularity(Granularity.GRANULARITY_PERMISSION_LEVEL)
        setWaitForAccurateLocation(true)
    }.build()
    locationCallback = object : LocationCallback() {
        override fun onLocationResult(locationResult: LocationResult) {
            locationResult.locations.forEach { location ->
                Log.i(TAG, "onLocationResult: $location") // Aquí se muestra en el log la ubicación actual
                // Aquí puedes realizar cualquier acción con la ubicación actual
                currentLocation = location  // Aquí se actualiza la posición actual
            }
        }
    }
}


    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            PERM_LOCATION_CODE -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    startLocationUpdates()
                } else {
                    alerts.shortSimpleSnackbar(
                        binding.root,
                        "Me acaban de negar los permisos de Localizacion 😭"
                    )

                }
            }
        }
    }




    private fun startLocationUpdates() {
        if (ContextCompat.checkSelfPermission(
                this, Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            fusedLocationClient.requestLocationUpdates(
                locationRequest, locationCallback, Looper.getMainLooper()
            )

        }
    }



}