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
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.example.weathermap.databinding.ActivityMainBinding
import com.example.weathermap.models.WeatherResponse
import com.example.weathermap.network.WeatherService
import com.google.android.gms.location.*
import com.karumi.dexter.Dexter
import com.karumi.dexter.MultiplePermissionsReport
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.multi.MultiplePermissionsListener
import retrofit.*
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : AppCompatActivity() {
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private var customProgressDialog: Dialog? = null
    private var binding: ActivityMainBinding? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding!!.root)
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
                        setupUI(weatherList = response.body())
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
    private fun setupUI(weatherList: WeatherResponse) {

        // For loop to get the required data. And all are populated in the UI.
        for (z in weatherList.weather.indices) {
            Log.i("NAMEEEEEEEE", weatherList.weather[z].main)

            binding?.tvMain?.text = weatherList.weather[z].main
            binding?.tvMainDescription?.text = weatherList.weather[z].description
            binding?.tvTemp?.text =
                weatherList.main.temp.toString() + getUnit(application.resources.configuration.locales.toString())
            binding?.tvHumidity?.text = weatherList.main.humidity.toString() + " per cent"
            binding?.tvMin?.text = weatherList.main.temp_min.toString() + " min"
            binding?.tvMax?.text = weatherList.main.temp_max.toString() + " max"
            binding?.tvSpeed?.text = weatherList.wind.speed.toString()
            binding?.tvName?.text = weatherList.name
            binding?.tvCountry?.text = weatherList.sys.country
            binding?.tvSunriseTime?.text = unixTime(weatherList.sys.sunrise.toLong())
            binding?.tvSunsetTime?.text = unixTime(weatherList.sys.sunset.toLong())

            when (weatherList.weather[z].icon) {
                "01d" -> binding?.ivMain?.setImageResource(R.drawable.sunny)
                "02d" -> binding?.ivMain?.setImageResource(R.drawable.cloud)
                "03d" -> binding?.ivMain?.setImageResource(R.drawable.cloud)
                "04d" -> binding?.ivMain?.setImageResource(R.drawable.cloud)
                "04n" -> binding?.ivMain?.setImageResource(R.drawable.cloud)
                "10d" -> binding?.ivMain?.setImageResource(R.drawable.rain)
                "11d" -> binding?.ivMain?.setImageResource(R.drawable.storm)
                "13d" -> binding?.ivMain?.setImageResource(R.drawable.snowflake)
                "01n" -> binding?.ivMain?.setImageResource(R.drawable.cloud)
                "02n" -> binding?.ivMain?.setImageResource(R.drawable.cloud)
                "03n" -> binding?.ivMain?.setImageResource(R.drawable.cloud)
                "10n" -> binding?.ivMain?.setImageResource(R.drawable.cloud)
                "11n" -> binding?.ivMain?.setImageResource(R.drawable.rain)
                "13n" -> binding?.ivMain?.setImageResource(R.drawable.snowflake)
            }

        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.refresh_menu,menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when(item.itemId) {
            R.id.refresh_menu -> {
                getUserCurrentLocation()
                true
            }
            else -> {
                super.onOptionsItemSelected(item)
            }
        }
    }

    private fun getUnit(value: String): String? {
        var value = "°C"
        if ("US" == value || "LR" == value || "MM" == value) {
            value = "°F"
        }
        return value
    }

    private fun unixTime(timex: Long): String? {
        val date = Date(timex * 1000L)
        @SuppressLint("SimpleDateFormat") val sdf =
            SimpleDateFormat("HH:mm", Locale.UK)
        sdf.timeZone = TimeZone.getDefault()
        return sdf.format(date)
    }
}


