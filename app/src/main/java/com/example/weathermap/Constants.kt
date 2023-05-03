package com.example.weathermap

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities

object Constants {
    const val BASE_URL: String = "https://api.openweathermap.org/data/";
    const val API_KEY: String = "1a040fde2010ac1211490b81016f29f7";
    const val METRIC_UNIT: String = "metric";
    fun isNetworkAvailable(context: Context): Boolean {
        val connectivityManager =
            context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        if (connectivityManager != null) {
            val capabilities =
                connectivityManager.getNetworkCapabilities(connectivityManager.activeNetwork)
            if (capabilities != null) {
                if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)) {
                    return true
                } else if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)) {
                    return true
                } else if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET)) {
                    return true
                }
            }
        }
        return false
    }
}