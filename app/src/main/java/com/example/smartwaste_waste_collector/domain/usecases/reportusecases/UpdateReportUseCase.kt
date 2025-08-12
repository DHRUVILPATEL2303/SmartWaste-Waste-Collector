package com.example.smartwaste_waste_collector.domain.usecases.reportusecases

import com.example.smartwaste_waste_collector.domain.repo.reportrepo.ReportRepositry
import javax.inject.Inject

class UpdateReportUseCase @Inject constructor(
    private val reportRepository: ReportRepositry
) {

    suspend fun updateReportUseCase(reportId:String,status:String,attachments:List<String>) = reportRepository.updateReport(reportId,status,attachments)
}