package com.example.weathermap

import android.content.Context
import android.content.Intent
import android.location.LocationManager
import android.os.Bundle
import android.provider.Settings
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        if(isLocationEnabled()) {
            Toast.makeText(this,"Your location is enabled", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(this,"Your location is not enabled", Toast.LENGTH_SHORT).show()
            startActivity(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS));
        }
    }
    private fun isLocationEnabled():Boolean {
        val lm: LocationManager =
            applicationContext.getSystemService(Context.LOCATION_SERVICE) as LocationManager;
        return lm.isProviderEnabled(LocationManager.GPS_PROVIDER) || lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
    }
}