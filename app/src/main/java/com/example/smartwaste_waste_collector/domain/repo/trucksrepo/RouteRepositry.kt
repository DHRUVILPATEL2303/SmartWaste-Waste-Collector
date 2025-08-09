package com.example.smartwaste_waste_collector.domain.repo.trucksrepo

import com.example.smartwaste_waste_collector.common.ResultState
import com.example.smartwaste_waste_collector.data.models.RouteModel
import kotlinx.coroutines.flow.Flow

interface RouteRepositry {

    suspend fun getAllRoutes() : Flow<ResultState<List<RouteModel>>>
}