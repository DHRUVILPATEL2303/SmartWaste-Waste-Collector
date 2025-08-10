package com.example.smartwaste_waste_collector.domain.repo.feedbackrepo

import com.example.smartwaste_waste_collector.common.ResultState
import com.example.smartwaste_waste_collector.data.models.WorkerFeedBackModel
import kotlinx.coroutines.flow.Flow
import javax.sql.RowSet

interface WorkerFeedBackRepositry {
    suspend fun getWorkerFeedBack() : Flow<ResultState<List<WorkerFeedBackModel>>>
}