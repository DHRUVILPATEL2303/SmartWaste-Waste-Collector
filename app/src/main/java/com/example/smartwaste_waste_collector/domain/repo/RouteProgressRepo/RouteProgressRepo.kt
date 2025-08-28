package com.example.smartwaste_waste_collector.domain.repo

import com.example.smartwaste_waste_collector.common.ResultState
import com.example.smartwaste_waste_collector.data.models.RouteProgressModel
import kotlinx.coroutines.flow.Flow

interface RouteProgressRepo {


    fun getTodayRouteProgress(): Flow<ResultState<RouteProgressModel?>>

    suspend fun updateAreaCompletionStatus(
        routeId: String,
        areaId: String,
        isCompleted: Boolean
    ): ResultState<Unit>


    suspend fun markRouteCompleted(
        routeId: String
    ): ResultState<Unit>

    suspend fun createAndSubmitRouteProgress(routeProgressModel: RouteProgressModel): ResultState<Unit>

    suspend fun getRouteProgressByRouteID(routeId : String) : ResultState<RouteProgressModel>


}