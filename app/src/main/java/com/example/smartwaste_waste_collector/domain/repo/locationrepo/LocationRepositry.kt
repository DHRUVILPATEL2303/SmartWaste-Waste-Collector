package com.example.smartwaste_waste_collector.domain.repo.locationrepo

interface LocationRepositry {

    suspend fun updateCollectorLocation(routeId : String ,lat: Double, lng: Double)
}