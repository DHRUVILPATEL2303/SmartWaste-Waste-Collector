package com.example.smartwaste_waste_collector.domain.usecases.truckusecases

import com.example.smartwaste_waste_collector.domain.repo.trucksrepo.TrucksRepositry
import javax.inject.Inject

class GetAllTruckUseCase @Inject constructor(
    private val trucksRepositry: TrucksRepositry
) {

    suspend fun getAllTrucks() = trucksRepositry.getallTrucks()
}