package com.example.smartwaste_waste_collector.presentation.viewmodels.truckviewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.smartwaste_waste_collector.common.ResultState
import com.example.smartwaste_waste_collector.data.models.TruckModel
import com.example.smartwaste_waste_collector.domain.usecases.truckusecases.GetAllTruckUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TruckViewModel @Inject constructor(
    private val getAllTruckUseCase: GetAllTruckUseCase
): ViewModel() {

    private val _allTrucksState= MutableStateFlow(CommonTruckState<List<TruckModel>>())
    val allTruckState=_allTrucksState.asStateFlow()


    init {
        getAllTrucks()
    }
    fun getAllTrucks(){
        viewModelScope.launch(Dispatchers.IO) {

            getAllTruckUseCase.getAllTrucks().collect {
                when(it){
                    is ResultState.Error -> {
                        _allTrucksState.value=CommonTruckState(error = it.message, isLoading = false)

                    }
                    ResultState.Loading -> {
                        _allTrucksState.value=CommonTruckState(isLoading = true)

                    }
                    is ResultState.Success -> {
                        _allTrucksState.value=CommonTruckState(success = it.data, isLoading = false)


                    }
                }
            }
        }


    }

}


data class CommonTruckState<T>(
    val isLoading: Boolean = false,
    val success: T? =null,
    val error:String=""
)