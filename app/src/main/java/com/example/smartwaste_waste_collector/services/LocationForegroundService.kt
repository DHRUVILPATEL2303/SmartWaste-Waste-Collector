package com.example.smartwaste_waste_collector.services


import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.os.Looper
import android.util.Log
import androidx.core.app.NotificationCompat
import com.example.smartwaste_waste_collector.R
import com.example.smartwaste_waste_collector.domain.repo.locationrepo.LocationRepositry
import com.google.android.gms.location.*
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class LocationForegroundService : Service() {

    @Inject lateinit var locationRepo: LocationRepositry

    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private var routeId: String = ""

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        routeId = intent?.getStringExtra("ROUTE_ID") ?: ""
        Log.d("LocationService", "Service started for Route ID: $routeId")

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        startForeground(1, createNotification())
        startLocationUpdates()
        return START_STICKY
    }

    private fun createNotification(): android.app.Notification {
        val channelId = "location_service_channel"
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Location Service",
                NotificationManager.IMPORTANCE_LOW
            )
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }

        return NotificationCompat.Builder(this, channelId)
            .setContentTitle("Smart Waste Collector")
            .setContentText("Tracking location for the current route...")
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .build()
    }

    @SuppressLint("MissingPermission")
    private fun startLocationUpdates() {
        val request = LocationRequest.Builder(
            Priority.PRIORITY_HIGH_ACCURACY,
            60L // 1 minute
        ).setMinUpdateIntervalMillis(30_000L).build()

        fusedLocationClient.requestLocationUpdates(
            request,
            object : LocationCallback() {
                override fun onLocationResult(result: LocationResult) {
                    val location = result.lastLocation ?: return


                    if (routeId.isNotBlank()) {
                        CoroutineScope(Dispatchers.IO).launch {
                            locationRepo.updateCollectorLocation(
                                routeId,
                                location.latitude,
                                location.longitude
                            )
                        }
                    } else {
                        Log.w("LocationService", "Route ID is blank, cannot update location.")
                    }
                }
            },
            Looper.getMainLooper()
        )
    }

    override fun onDestroy() {
        Log.d("LocationService", "Service destroyed for Route ID: $routeId")
        fusedLocationClient.removeLocationUpdates(locationCallback)
        super.onDestroy()
    }

    private val locationCallback = object : LocationCallback() {
        override fun onLocationResult(result: LocationResult) {
        }
    }
}