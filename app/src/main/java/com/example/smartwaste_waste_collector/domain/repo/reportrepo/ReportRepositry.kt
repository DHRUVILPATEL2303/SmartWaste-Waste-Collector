package com.example.smartwaste_waste_collector.domain.repo.reportrepo

import com.example.smartwaste_waste_collector.common.ResultState
import com.example.smartwaste_waste_collector.data.models.ReportModel
import kotlinx.coroutines.flow.Flow

interface ReportRepositry {

    suspend fun getAllReports(): Flow<ResultState<List<ReportModel>>>

    suspend fun updateReport(reportId: String, status: String, attachments: List<String>): ResultState<String>


}