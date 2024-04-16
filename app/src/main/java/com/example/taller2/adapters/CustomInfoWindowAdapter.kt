package com.example.taller2.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.widget.TextView
import com.example.taller2.R
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.Marker

class CustomInfoWindowAdapter(private val context: Context) : GoogleMap.InfoWindowAdapter {
    override fun getInfoContents(p0: Marker): View? {
        return null
    }

    override fun getInfoWindow(p0: Marker): View? {

        val view = LayoutInflater.from(context).inflate(R.layout.marker_with_address, null)


        val addressTextView = view.findViewById<TextView>(R.id.addressText)
        addressTextView.text = p0?.snippet

        val title = extractSubarray(p0?.snippet)
        val titleText = view.findViewById<TextView>(R.id.textTitleMarkerView)
        titleText.text = title
        return view
    }
}

fun extractSubarray(input: String?): String? {
    if (input == null) {
        return null
    }

    val startIndex = input.indexOf('#') + 1 // Add 1 to exclude the '#' character
    val endIndex = input.indexOf(',')

    // Check if both '#' and ',' are found in the string
    if (startIndex >= 0 && endIndex >= 0 && startIndex < endIndex) {
        return input.substring(startIndex, endIndex).trim()
    }

    // Return null if either '#' or ',' is not found or if the startIndex is after the endIndex
    return null
}