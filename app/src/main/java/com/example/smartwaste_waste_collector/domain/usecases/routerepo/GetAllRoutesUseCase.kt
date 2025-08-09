package com.example.smartwaste_waste_collector.domain.usecases.routerepo

import com.example.smartwaste_waste_collector.domain.repo.trucksrepo.RouteRepositry
import javax.inject.Inject

class GetAllRoutesUseCase @Inject constructor(
    private val routeRepositry: RouteRepositry
) {
    suspend fun getAllRoutes() = routeRepositry.getAllRoutes()
}