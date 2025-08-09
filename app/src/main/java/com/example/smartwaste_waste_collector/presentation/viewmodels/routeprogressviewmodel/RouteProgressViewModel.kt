package com.example.smartwaste_waste_collector.presentation.viewmodels.routeprogressviewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.smartwaste_waste_collector.common.ResultState
import com.example.smartwaste_waste_collector.data.models.AreaProgress
import com.example.smartwaste_waste_collector.data.models.RouteModel
import com.example.smartwaste_waste_collector.data.models.RouteProgressModel
import com.example.smartwaste_waste_collector.data.models.TruckModel
import com.example.smartwaste_waste_collector.domain.usecases.routeprogressusecase.CreateAndSubmitRouteProgressUseCase
import com.example.smartwaste_waste_collector.domain.usecases.routeprogressusecase.GetRouteProgressUseCase
import com.example.smartwaste_waste_collector.domain.usecases.routeprogressusecase.MarkAreaAsCompletedUseCase
import com.example.smartwaste_waste_collector.domain.usecases.routeprogressusecase.UpdateAreaCompletedUseCase
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class RouteProgressViewModel @Inject constructor(
    private val getRouteProgressUseCase: GetRouteProgressUseCase,
    private val updateAreaCompletedUseCase: UpdateAreaCompletedUseCase,
    private val markAreaAsCompletedUseCase: MarkAreaAsCompletedUseCase,
    private val createAndSubmitRouteProgressUseCase: CreateAndSubmitRouteProgressUseCase
) : ViewModel(){


    private val _routeProgressState = MutableStateFlow(CommonRouteProgressState<RouteProgressModel>())
    val routeProgressState = _routeProgressState.asStateFlow()

    private val _updateAreaCompletedState = MutableStateFlow(CommonRouteProgressState<Unit>())
    val updateAreaCompletedState = _updateAreaCompletedState.asStateFlow()

    private val _markAreaAsCompletedState = MutableStateFlow(CommonRouteProgressState<Unit>())
    val markAreaAsCompletedState = _markAreaAsCompletedState.asStateFlow()


    private val _createAndSubmitRouteProgressState = MutableStateFlow(CommonRouteProgressState<Unit>())
    val createAndSubmitRouteProgressState = _createAndSubmitRouteProgressState.asStateFlow()


    init {
        getRouteProgress()
    }



    fun getRouteProgress() {
        viewModelScope.launch {

            getRouteProgressUseCase.getRouteProgress().collect {
                when (it) {

                    is ResultState.Success -> {
                        _routeProgressState.value = CommonRouteProgressState(success = it.data, isLoading = false)
                    }

                    is ResultState.Error -> {
                        _routeProgressState.value = CommonRouteProgressState(error = it.message, isLoading = false)
                    }

                    is ResultState.Loading -> {
                        _routeProgressState.value = CommonRouteProgressState(isLoading = true)
                    }


                }

            }

        }
    }

    fun updateAreaCompleted(routeId: String, areaId: String, isCompleted: Boolean) {
        viewModelScope.launch {

            _updateAreaCompletedState.value = CommonRouteProgressState(isLoading = true)
            val result=updateAreaCompletedUseCase.updateAreaCompleted(routeId, areaId, isCompleted)

            when (result) {
                is ResultState.Success -> {
                    _updateAreaCompletedState.value = CommonRouteProgressState(success = result.data, isLoading = false)
                }

                is ResultState.Error -> {
                    _updateAreaCompletedState.value = CommonRouteProgressState(error = result.message, isLoading = false)


                }
                else -> {}


            }

        }

    }

    fun markAreaAsCompleted(routeId: String) {
        viewModelScope.launch {
            _markAreaAsCompletedState.value = CommonRouteProgressState(isLoading = true)
            val result = markAreaAsCompletedUseCase.markRouteAsCompleted(routeId)
            when (result) {

                is ResultState.Success -> {
                    _markAreaAsCompletedState.value = CommonRouteProgressState(success = result.data, isLoading = false)
                }

                is ResultState.Error -> {
                    _markAreaAsCompletedState.value = CommonRouteProgressState(error = result.message, isLoading = false)
                }
                else -> {}

            }

        }
    }

    fun createAndSubmitRouteProgress(
        selectedTruck: TruckModel,
        selectedRoute: RouteModel,
        selectedRole: String
    ) {
        viewModelScope.launch {
            _createAndSubmitRouteProgressState.value = CommonRouteProgressState(isLoading = true)

            val today = java.time.LocalDate.now().toString()
            val areaProgressList = selectedRoute.areaList.map { areaInfo ->
                AreaProgress(
                    areaId = areaInfo.areaId,
                    areaName = areaInfo.areaName,
                    isCompleted = false,
                    completedAt = null
                )
            }

            val progressModel = RouteProgressModel(
                routeId = selectedRoute.id,
                date = today,
                assignedCollectorId = if (selectedRole == "collector") FirebaseAuth.getInstance().currentUser!!.uid else "",
                assignedDriverId = if (selectedRole == "driver") FirebaseAuth.getInstance().currentUser!!.uid else "",
                assignedTruckId = selectedTruck.id,
                areaProgress = areaProgressList,
                isRouteCompleted = false
            )

            val result = createAndSubmitRouteProgressUseCase.createAndSubmitRouteProgress(progressModel)

            when(result) {
                is ResultState.Success -> {
                    _createAndSubmitRouteProgressState.value = CommonRouteProgressState(success = result.data, isLoading = false)
                }
                is ResultState.Error -> {
                    _createAndSubmitRouteProgressState.value = CommonRouteProgressState(error = result.message, isLoading = false)
                }
                else -> {
                    _createAndSubmitRouteProgressState.value = CommonRouteProgressState(isLoading = false)
                }
            }
        }
    }
}


data class CommonRouteProgressState<T>(
    val isLoading : Boolean = false,
    val success : T ?= null,
    val error : String =""
)