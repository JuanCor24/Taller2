package com.example.taller2.activities

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.taller2.R
import com.example.taller2.databinding.ActivityMainBinding
import com.example.taller2.utils.Alerts

class MainActivity : AppCompatActivity() {
    private lateinit var binding:ActivityMainBinding;
    private val alerts: Alerts = Alerts(this)


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.cameraActivityButton.setOnClickListener() {
            startActivity(Intent(this, CamaraActivity::class.java))
        }

        binding.paisesActivityButton.setOnClickListener(){
            startActivity(Intent(this,MapsActivity::class.java))
        }
    }
}