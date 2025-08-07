package com.example.smartwaste_waste_collector.domain.usecases.dailyassignmentusecases

import com.example.smartwaste_waste_collector.domain.repo.dailyAssignRepo.DailyAssignRepository
import javax.inject.Inject

class GetDailyAssignmentUseCase @Inject constructor(
    private val dailyAssignRepository: DailyAssignRepository
) {

    suspend fun getDailyAssignment() = dailyAssignRepository.getTodayAssignment()
}