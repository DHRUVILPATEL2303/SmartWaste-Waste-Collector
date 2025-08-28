package com.example.smartwaste_waste_collector.domain.usecases.locationusecase

import com.example.smartwaste_waste_collector.domain.repo.locationrepo.LocationRepositry
import javax.inject.Inject

class UpdateLocationUseCase @Inject constructor(
    private val locationRepositry: LocationRepositry
) {
    suspend fun updateLocation(routeID : String,lat : Double, Lng : Double)=locationRepositry.updateCollectorLocation(routeID,lat, lng =Lng)
}