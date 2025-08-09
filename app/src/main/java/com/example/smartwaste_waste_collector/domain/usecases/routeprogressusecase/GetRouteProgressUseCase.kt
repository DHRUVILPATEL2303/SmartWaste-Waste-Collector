package com.example.smartwaste_waste_collector.domain.usecases.routeprogressusecase

import com.example.smartwaste_waste_collector.domain.repo.RouteProgressRepo
import javax.inject.Inject

class GetRouteProgressUseCase @Inject constructor(
    private val routeProgressRepo: RouteProgressRepo
) {
    suspend fun getRouteProgress() =routeProgressRepo.getTodayRouteProgress()
}