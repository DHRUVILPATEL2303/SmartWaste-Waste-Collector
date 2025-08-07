package com.example.smartwaste_waste_collector.domain.usecases.dailyassignmentusecases

import com.example.smartwaste_waste_collector.data.models.DailyAssignment
import com.example.smartwaste_waste_collector.domain.repo.dailyAssignRepo.DailyAssignRepository
import javax.inject.Inject

class SubmitDailyAssignmentUseCase @Inject constructor(
    private val dailyAssignRepository: DailyAssignRepository
) {

    suspend fun submitDailyAssignment(assignment: DailyAssignment) = dailyAssignRepository.submitTodayAssignment(assignment)
}