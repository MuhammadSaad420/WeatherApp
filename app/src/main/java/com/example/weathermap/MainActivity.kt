package com.example.weathermap

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.location.LocationManager
import android.net.Uri
import android.os.Bundle
import android.os.Looper
import android.provider.Settings
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.example.weathermap.models.WeatherResponse
import com.example.weathermap.network.WeatherService
import com.google.android.gms.location.*
import com.karumi.dexter.Dexter
import com.karumi.dexter.MultiplePermissionsReport
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.multi.MultiplePermissionsListener
import retrofit.*

class MainActivity : AppCompatActivity() {
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private var customProgressDialog: Dialog? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        if(isLocationEnabled()) {
            Dexter.withContext(this).withPermissions(android.Manifest.permission.ACCESS_FINE_LOCATION,
                android.Manifest.permission.ACCESS_COARSE_LOCATION)
                .withListener(object: MultiplePermissionsListener {
                    override fun onPermissionsChecked(result: MultiplePermissionsReport?) {
                        if(result!!.areAllPermissionsGranted()) {
                            getUserCurrentLocation()
                        }
                    }

                    override fun onPermissionRationaleShouldBeShown(
                        p0: MutableList<PermissionRequest>?,
                        p1: PermissionToken?
                    ) {
                        showActivatePermissionDialogPrompt()
                    }

                }).onSameThread().check()
        } else {
            Toast.makeText(this,"Your location is not enabled", Toast.LENGTH_SHORT).show()
            startActivity(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS));
        }
    }

    private fun getLocationWeatherDetails(latitude: Double, longitude:Double) {
        if(Constants.isNetworkAvailable(this)) {
            val retrofit = Retrofit.Builder()
                .baseUrl(Constants.BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
            val weatherService: WeatherService = retrofit.create(WeatherService::class.java)
            val weatherServiceCall: Call<WeatherResponse> = weatherService.getWeather(latitude = latitude,
                longitude = longitude,
                units = Constants.METRIC_UNIT
            , appid = Constants.API_KEY)
            showCustomProgressDialog()
            weatherServiceCall.enqueue(object:Callback<WeatherResponse>{
                override fun onResponse(response: Response<WeatherResponse>?, retrofit: Retrofit?) {
                    if(response!!.isSuccess) {
                        hideProgressDialog()
                        Log.e("Responce", response.body().toString())
                    } else {
                        when(response.code()) {
                            400 -> {

                            }
                            404 -> {

                            }
                            else -> {

                            }
                        }
                    }

                }

                override fun onFailure(t: Throwable?) {
                    hideProgressDialog()
                    Log.e("Error", t!!.message.toString())
                }

            })
        } else {
            Toast.makeText(this,"Internet is not available",Toast.LENGTH_SHORT).show()
        }
    }
    @SuppressLint("MissingPermission")
    private fun getUserCurrentLocation() {
        val locationRequest = LocationRequest()
        locationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        fusedLocationClient.requestLocationUpdates(locationRequest, object: LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                val location = locationResult.lastLocation;
                getLocationWeatherDetails(location!!.latitude,location!!.longitude)
            }
   }, Looper.myLooper())
    }

    private fun showActivatePermissionDialogPrompt() {
        AlertDialog.Builder(this).setTitle("Location Permission!!")
            .setPositiveButton("go to settings", DialogInterface.OnClickListener { _, _ ->

                val intent = Intent()
                intent.action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
                val uri = Uri.fromParts("package", packageName, null)
                intent.data = uri
                startActivity(intent)
            })
            .setNegativeButton("Cancel", DialogInterface.OnClickListener { dialog, _ ->
                dialog.dismiss()
            })
            .create()
            .show()
    }

    private fun isLocationEnabled():Boolean {
        val lm: LocationManager =
            applicationContext.getSystemService(Context.LOCATION_SERVICE) as LocationManager;
        return lm.isProviderEnabled(LocationManager.GPS_PROVIDER) || lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
    }
    private fun showCustomProgressDialog() {
       customProgressDialog = Dialog(this@MainActivity)
       customProgressDialog!!.setContentView(R.layout.dialog_custom_progress)
        customProgressDialog!!.show()
    }
    private fun hideProgressDialog() {
        customProgressDialog!!.hide()
    }
}


