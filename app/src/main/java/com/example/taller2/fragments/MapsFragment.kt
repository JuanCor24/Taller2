package com.example.taller2.fragments

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.location.Location
import androidx.fragment.app.Fragment
import android.widget.Toast

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.example.taller2.R
import com.example.taller2.adapters.CustomInfoWindowAdapter
import com.example.taller2.utils.GeocoderSearch
import com.google.android.gms.maps.CameraUpdate

import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptor
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MapStyleOptions
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.maps.model.PolylineOptions

class MapsFragment : Fragment(), SensorEventListener{


    private lateinit var gMap: GoogleMap
    val sydney = LatLng(-34.0, 151.0)
    private lateinit var dogMarker: Marker
    var zoomLevel = 15.0f
    private val rutaCoordinates = mutableListOf<LatLng>()
    var moveCamera = true
    private lateinit var sensorManager: SensorManager
    private lateinit var lightSensor: Sensor
    private val callback = OnMapReadyCallback { googleMap ->
        /**
         * Manipulates the map once available.
         * This callback is triggered when the map is ready to be used.
         * This is where we can add markers or lines, add listeners or move the camera.
         * In this case, we just add a marker near Sydney, Australia.
         * If Google Play services is not installed on the device, the user will be prompted to
         * install it inside the SupportMapFragment. This method will only be triggered once the
         * user has installed Google Play services and returned to the app.
         */
        gMap = googleMap
        gMap.uiSettings.isZoomControlsEnabled = false
        gMap.uiSettings.isCompassEnabled = true



        gMap.setMapStyle(
            context?.let { MapStyleOptions.loadRawResourceStyle(it, R.raw.map_day) })

        dogMarker = gMap.addMarker(
            MarkerOptions().position(sydney).title("Hey Dog!")
                .icon(context?.let { bitmapDescriptorFromVector(it, R.drawable.doushouqi_dog) })
        )!!
        gMap.moveCamera(CameraUpdateFactory.newLatLng(sydney))


        gMap.setOnMapLongClickListener {  latLng ->

            val geocoderSearch = GeocoderSearch(requireContext())

            val latitude = latLng.latitude
            val longitude = latLng.longitude


            val markerTitle = "Título del marcador en ($latitude, $longitude)"

            // Obtiene la dirección del marcador utilizando la función findAddressByPosition
            val address = geocoderSearch.findAddressByPosition(latLng)?.getAddressLine(0) ?: "Dirección desconocida"


            val context = requireContext() // Si estás en un fragmento


            addStore(context, latLng, markerTitle, address)
        }


    }

    fun updateCamera(cameraUpdate: CameraUpdate) {
        gMap.animateCamera(cameraUpdate)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.activity_fragmentmaps, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val mapFragment = childFragmentManager.findFragmentById(R.id.mapFragment) as SupportMapFragment?
        mapFragment?.getMapAsync(callback)

        sensorManager = context?.getSystemService(Context.SENSOR_SERVICE) as SensorManager
        lightSensor = sensorManager.getDefaultSensor(Sensor.TYPE_LIGHT)!!
    }

    //From https://stackoverflow.com/questions/42365658/custom-marker-in-google-maps-in-android-with-vector-asset-icon
    private fun bitmapDescriptorFromVector(context: Context, vectorResId: Int): BitmapDescriptor? {
        return ContextCompat.getDrawable(context, vectorResId)?.run {
            setBounds(0, 0, intrinsicWidth, intrinsicHeight)
            val bitmap =
                Bitmap.createBitmap(intrinsicWidth, intrinsicHeight, Bitmap.Config.ARGB_8888)
            draw(Canvas(bitmap))
            BitmapDescriptorFactory.fromBitmap(bitmap)
        }
    }

    override fun onSensorChanged(event: SensorEvent?) {
        if (this::gMap.isInitialized) {
            if (event!!.values[0] > 100) {
                gMap.setMapStyle(
                    context?.let { MapStyleOptions.loadRawResourceStyle(it, R.raw.map_day) })
            } else {
                gMap.setMapStyle(
                    context?.let { MapStyleOptions.loadRawResourceStyle(it, R.raw.map_night) })
            }
        }
    }
    private fun addPoint(latLng: LatLng) {
        gMap.addMarker(
            MarkerOptions().position(latLng).title("Estuve aqui").icon(
                context?.let { bitmapDescriptorFromVector(it, R.drawable.golf) })
        )
    }

    fun addStore(context: Context, location: LatLng, title: String, address: String) {

        val infoWindowAdapter = CustomInfoWindowAdapter(context)
        gMap.setInfoWindowAdapter(infoWindowAdapter)

        val markerOptions = MarkerOptions()
            .position(location)
            .title(title)
            .snippet(address) // Set the address as the snippet
        gMap.addMarker(markerOptions)
    }

    fun moveDog(location: Location) {
        val latLng = LatLng(location.latitude, location.longitude)
        dogMarker.position = latLng
        dogMarker.zIndex = 10.0f
        if(moveCamera) {
            gMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, zoomLevel))
        }

        rutaCoordinates.add(latLng)
        val polylineOptions = PolylineOptions().apply {
            color(ContextCompat.getColor(requireContext(), R.color.md_theme_light_scrim))
            width(10f)
            addAll(rutaCoordinates)
        }
        gMap.addPolyline(polylineOptions)



        val message = "La posición se ha actualizado: Latitud ${location.latitude}, Longitud ${location.longitude}"
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
    }

    override fun onAccuracyChanged(p0: Sensor?, p1: Int) {
        //Do nothing
    }


    fun getRutaCoordinates(): List<LatLng> {
        return rutaCoordinates.toList()
    }


    override fun onResume() {
        super.onResume()
        sensorManager.registerListener(this, lightSensor, SensorManager.SENSOR_DELAY_NORMAL)

    }

    override fun onPause() {
        super.onPause()
        sensorManager.unregisterListener(this)

    }


}