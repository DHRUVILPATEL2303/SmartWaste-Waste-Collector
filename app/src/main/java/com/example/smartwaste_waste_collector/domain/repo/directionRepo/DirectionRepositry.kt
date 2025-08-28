package com.example.smartwaste_waste_collector.domain.repo.directionRepo

import org.osmdroid.util.GeoPoint

interface DirectionsRepositry {

    suspend fun fetchRoute(
        startLat: Double, startLng: Double,
        endLat: Double, endLng: Double
    ) : List<GeoPoint>
}