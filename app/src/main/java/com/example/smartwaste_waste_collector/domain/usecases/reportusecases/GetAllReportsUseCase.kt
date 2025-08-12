package com.example.smartwaste_waste_collector.domain.usecases.reportusecases

import com.example.smartwaste_waste_collector.domain.repo.reportrepo.ReportRepositry
import javax.inject.Inject

class GetAllReportsUseCase @Inject constructor(
    private val reportRepository: ReportRepositry
) {

    suspend  fun getAllReports() = reportRepository.getAllReports()

}