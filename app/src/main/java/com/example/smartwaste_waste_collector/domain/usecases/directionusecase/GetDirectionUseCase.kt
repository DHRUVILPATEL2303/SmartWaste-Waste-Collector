package com.example.smartwaste_waste_collector.domain.usecases.directionusecase

import com.example.smartwaste_waste_collector.domain.repo.directionRepo.DirectionsRepositry
import javax.inject.Inject

class GetDirectionUseCase @Inject constructor(
    private val directionsRepositry: DirectionsRepositry
) {
    suspend fun getDirectionUseCase(
        startLat: Double, startLng: Double,
        endLat: Double, endLng: Double
    )=directionsRepositry.fetchRoute(startLat,startLng,endLat,endLng)
}