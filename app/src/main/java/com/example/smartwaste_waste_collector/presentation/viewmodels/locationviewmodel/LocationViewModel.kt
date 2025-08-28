package com.example.smartwaste_waste_collector.presentation.viewmodel

import android.Manifest
import android.annotation.SuppressLint
import android.app.Application
import android.location.Location
import androidx.annotation.RequiresPermission
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.smartwaste_waste_collector.domain.usecases.locationusecase.UpdateLocationUseCase
import com.google.android.gms.location.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

@HiltViewModel
class LocationViewModel @Inject constructor(
    application: Application,
    private val updateCollectorLocationUseCase: UpdateLocationUseCase
) : AndroidViewModel(application) {

    private val fusedLocationClient =
        LocationServices.getFusedLocationProviderClient(application)


    init {
        startUpdatingLocation("r7CTdoJCqbqyj4j10El8")
    }

    private var locationJob: Job? = null

    @SuppressLint("MissingPermission")
    fun startUpdatingLocation(routeId: String) {
        locationJob?.cancel()
        locationJob = viewModelScope.launch {
            while (true) {
                val location: Location? = fusedLocationClient.lastLocation.await()
                location?.let {
                    updateCollectorLocationUseCase.updateLocation(
                        routeId,
                        it.latitude,
                        it.longitude
                    )
                }
                delay(60)
            }
        }
    }

    fun stopUpdatingLocation() {
        locationJob?.cancel()
    }

    @RequiresPermission(allOf = [Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION])
    suspend fun FusedLocationProviderClient.awaitOrNull(): Location? {
        return try {
            this.lastLocation.await()  // suspends until task completes
        } catch (e: Exception) {
            null
        }
    }
}