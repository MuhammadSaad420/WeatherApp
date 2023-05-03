package com.example.weathermap.network

import com.example.weathermap.models.Weather
import com.example.weathermap.models.WeatherResponse
import retrofit.*
import retrofit.http.GET
import retrofit.http.Query

interface WeatherService {
    @GET("2.5/weather")
    fun getWeather(@Query("lat") latitude: Double,
                   @Query("lon") longitude: Double,
                   @Query("units") units: String?,
                   @Query("appid") appid: String?): Call<WeatherResponse>
}