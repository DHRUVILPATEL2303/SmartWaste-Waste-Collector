package com.example.smartwaste_waste_collector.presentation.viewmodels.workerfeedbackviewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.smartwaste_waste_collector.data.models.WorkerFeedBackModel
import com.example.smartwaste_waste_collector.domain.usecases.workerfeedbackusecases.GetAllFeedBackUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject


@HiltViewModel
class WorkerFeedBackViewModel  @Inject constructor(
    private val getAllFeedBackUseCase: GetAllFeedBackUseCase
): ViewModel(){

    private val _feedBackState= MutableStateFlow(CommonFeedBackState<List<WorkerFeedBackModel>>())
    val feedBackState=_feedBackState.asStateFlow()

    fun getFeedBack(){
        viewModelScope.launch(Dispatchers.IO) {
            getAllFeedBackUseCase.getAllFeedbacks().collect{


                when(it){
                    is com.example.smartwaste_waste_collector.common.ResultState.Loading->{
                        _feedBackState.value=CommonFeedBackState(isLoading = true)
                    }

                    is com.example.smartwaste_waste_collector.common.ResultState.Success->{
                        _feedBackState.value=CommonFeedBackState(success = it.data)
                    }
                    is com.example.smartwaste_waste_collector.common.ResultState.Error->{
                        _feedBackState.value=CommonFeedBackState(error = it.message)
                    }

                }
            }

        }
    }


}


data class CommonFeedBackState<T>(
    val isLoading: Boolean = false,
    val success: T? = null,
    val error: String = ""

)