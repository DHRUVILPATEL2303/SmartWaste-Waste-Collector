package com.example.smartwaste_waste_collector.domain.repo.trucksrepo

import com.example.smartwaste_waste_collector.common.ResultState
import com.example.smartwaste_waste_collector.data.models.TruckModel
import kotlinx.coroutines.flow.Flow

interface TrucksRepositry {

    suspend fun getallTrucks() : Flow<ResultState<List<TruckModel>>>
}