package com.example.smartwaste_waste_collector.presentation.viewmodels.dailyassignviewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.smartwaste_waste_collector.common.ResultState
import com.example.smartwaste_waste_collector.data.models.DailyAssignment
import com.example.smartwaste_waste_collector.domain.usecases.dailyassignmentusecases.GetDailyAssignmentUseCase
import com.example.smartwaste_waste_collector.domain.usecases.dailyassignmentusecases.SubmitDailyAssignmentUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject


@HiltViewModel
class DailyAssignViewModel @Inject constructor(
    private val getDailyAssignmentUseCase: GetDailyAssignmentUseCase,
    private val submitDailyAssignmentUseCase: SubmitDailyAssignmentUseCase
) : ViewModel() {


    private val _dailyAssignState = MutableStateFlow(CommonDailyAssignState<DailyAssignment>())
    val dailyAssignState = _dailyAssignState.asStateFlow()

    private val _submitAssignState = MutableStateFlow(CommonDailyAssignState<String>())
    val submitAssignState = _submitAssignState.asStateFlow()

    fun getDailyAssignment() {
        viewModelScope.launch(Dispatchers.IO) {
            _dailyAssignState.value = CommonDailyAssignState(isLoading = true)
            val result = getDailyAssignmentUseCase.getDailyAssignment()

            when (result) {
                is ResultState.Error -> {
                    _dailyAssignState.value =
                        CommonDailyAssignState(error = result.message, isLoading = false)
                }

                ResultState.Loading -> {
                    _dailyAssignState.value = CommonDailyAssignState(isLoading = true)

                }

                is ResultState.Success -> {
                    _dailyAssignState.value = CommonDailyAssignState(
                        success = result.data ,
                        isLoading = false
                    )


                }
            }

        }
    }

    fun submitDailyAssignment(assignment: DailyAssignment) {
        viewModelScope.launch(Dispatchers.IO) {
            _submitAssignState.value = CommonDailyAssignState(isLoading = true)
            val result = submitDailyAssignmentUseCase.submitDailyAssignment(assignment)
            when (result) {
                is ResultState.Error -> {
                    _submitAssignState.value =
                        CommonDailyAssignState(error = result.message, isLoading = false)
                }

                ResultState.Loading -> {
                    _submitAssignState.value = CommonDailyAssignState(isLoading = true)
                }

                is ResultState.Success<*> -> {
                    _submitAssignState.value =
                        CommonDailyAssignState(success = "success", isLoading = false)
                }
            }

        }
    }


}

data class CommonDailyAssignState<T>(
    val isLoading: Boolean = false,
    val success: T? = null,
    val error: String = ""
)