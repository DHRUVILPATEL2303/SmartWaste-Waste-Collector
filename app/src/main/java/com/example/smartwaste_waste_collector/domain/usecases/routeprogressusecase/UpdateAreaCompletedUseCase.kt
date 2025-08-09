package com.example.smartwaste_waste_collector.domain.usecases.routeprogressusecase

import com.example.smartwaste_waste_collector.domain.repo.RouteProgressRepo
import javax.inject.Inject

class UpdateAreaCompletedUseCase @Inject constructor(
    private val routeProgressRepo:
    RouteProgressRepo
) {

    suspend fun updateAreaCompleted(routeId: String, areaId: String, isCompleted: Boolean) = routeProgressRepo.updateAreaCompletionStatus(routeId, areaId, isCompleted)
}