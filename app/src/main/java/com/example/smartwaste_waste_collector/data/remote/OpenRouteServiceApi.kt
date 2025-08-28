package com.example.smartwaste_waste_collector.data.remote

import com.example.smartwaste_waste_collector.data.models.ORSRouteResponse
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Query

interface OpenRouteServiceApi {


    @GET("/v2/directions/driving-car")
    suspend fun getDrivingRoute(
        @Header("Authorization") apiKey: String,
        @Query("start") start: String,
        @Query("end") end: String
    ): ORSRouteResponse
}



object NetworkModule {

    private const val BASE_URL = "https://api.openrouteservice.org/"

    fun provideOkHttp(): OkHttpClient {

        return OkHttpClient.Builder()

            .build()
    }

    fun provideRetrofit(client: OkHttpClient): Retrofit {
        return Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    fun provideORSApi(): OpenRouteServiceApi {
        return provideRetrofit(provideOkHttp()).create(OpenRouteServiceApi::class.java)
    }
}