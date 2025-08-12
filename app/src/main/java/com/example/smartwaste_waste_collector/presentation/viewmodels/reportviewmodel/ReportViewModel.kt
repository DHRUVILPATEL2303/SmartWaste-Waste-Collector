package com.example.smartwaste_waste_collector.presentation.viewmodels.reportviewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.smartwaste_waste_collector.common.ResultState
import com.example.smartwaste_waste_collector.data.models.ReportModel
import com.example.smartwaste_waste_collector.domain.usecases.reportusecases.GetAllReportsUseCase
import com.example.smartwaste_waste_collector.domain.usecases.reportusecases.UpdateReportUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject


@HiltViewModel
class ReportViewModel @Inject constructor(
    private val getAllReportsUseCase: GetAllReportsUseCase,
    private val updateReportUseCase: UpdateReportUseCase
) : ViewModel() {

    private val _getReportsState = MutableStateFlow(CommonReportState<List<ReportModel>>())
    val getReportsState: StateFlow<CommonReportState<List<ReportModel>>> =
        _getReportsState.asStateFlow()

    private val _updateReportState = MutableStateFlow(CommonReportState<String>())
    val updateReportState = _updateReportState.asStateFlow()


    fun getAllReports() {
        viewModelScope.launch(Dispatchers.IO) {
            getAllReportsUseCase.getAllReports().collect {
                when (it) {
                    is ResultState.Success -> {
                        _getReportsState.value = CommonReportState(success = it.data)
                    }

                    is ResultState.Error -> {
                        _getReportsState.value = CommonReportState(error = it.message)
                    }

                    is ResultState.Loading -> {
                        _getReportsState.value = CommonReportState(isLoading = true)
                    }
                }
            }
        }
    }


    fun updateReport(reportId: String, status: String, attachments: List<String>) {
        viewModelScope.launch(Dispatchers.IO) {
            _updateReportState.value = CommonReportState(isLoading = true)
            updateReportUseCase.updateReportUseCase(reportId, status, attachments).also {
                when (it) {
                    is ResultState.Success -> {
                        _updateReportState.value = CommonReportState(success = it.data, isLoading = false)
                        getAllReports() // Refresh the list after a successful update
                    }

                    is ResultState.Error -> {
                        _updateReportState.value = CommonReportState(error = it.message, isLoading = false)
                    }

                    is ResultState.Loading -> {
                        _updateReportState.value = CommonReportState(isLoading = true)
                    }
                }
            }
        }
    }


    fun clearUpdateState() {
        _updateReportState.value = CommonReportState()
    }


}

data class CommonReportState<T>(
    val isLoading: Boolean = false,
    val success: T? = null,
    val error: String = ""
)