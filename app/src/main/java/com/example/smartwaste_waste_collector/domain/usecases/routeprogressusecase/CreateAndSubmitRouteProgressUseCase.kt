package com.example.smartwaste_waste_collector.domain.usecases.routeprogressusecase

import com.example.smartwaste_waste_collector.data.models.RouteProgressModel
import com.example.smartwaste_waste_collector.domain.repo.RouteProgressRepo
import javax.inject.Inject

class CreateAndSubmitRouteProgressUseCase @Inject constructor(
    private val routeProgressRepo: RouteProgressRepo
) {
    suspend fun createAndSubmitRouteProgress(routeProgressModel: RouteProgressModel) =routeProgressRepo.createAndSubmitRouteProgress(routeProgressModel)
}