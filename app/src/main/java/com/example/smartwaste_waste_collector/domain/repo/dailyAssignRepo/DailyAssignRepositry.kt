package com.example.smartwaste_waste_collector.domain.repo.dailyAssignRepo

import com.example.smartwaste_waste_collector.common.ResultState
import com.example.smartwaste_waste_collector.data.models.DailyAssignment


interface DailyAssignRepository {

    suspend fun getTodayAssignment(): ResultState<DailyAssignment?>

    suspend fun submitTodayAssignment(assignment: DailyAssignment): ResultState<Unit>
}