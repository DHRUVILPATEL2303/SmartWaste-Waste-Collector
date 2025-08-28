package com.example.smartwaste_waste_collector.presentation.viewmodels.routeprogressviewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.smartwaste_waste_collector.common.ResultState
import com.example.smartwaste_waste_collector.data.models.AreaProgress
import com.example.smartwaste_waste_collector.data.models.RouteModel
import com.example.smartwaste_waste_collector.data.models.RouteProgressModel
import com.example.smartwaste_waste_collector.data.models.TruckModel
import com.example.smartwaste_waste_collector.domain.usecases.routeprogressusecase.CreateAndSubmitRouteProgressUseCase
import com.example.smartwaste_waste_collector.domain.usecases.routeprogressusecase.GetRouteProgressByIdUseCase
import com.example.smartwaste_waste_collector.domain.usecases.routeprogressusecase.GetRouteProgressUseCase
import com.example.smartwaste_waste_collector.domain.usecases.routeprogressusecase.MarkAreaAsCompletedUseCase
import com.example.smartwaste_waste_collector.domain.usecases.routeprogressusecase.UpdateAreaCompletedUseCase
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.handleCoroutineException
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import javax.inject.Inject

@HiltViewModel
class RouteProgressViewModel @Inject constructor(
    private val getRouteProgressUseCase: GetRouteProgressUseCase,
    private val updateAreaCompletedUseCase: UpdateAreaCompletedUseCase,
    private val markAreaAsCompletedUseCase: MarkAreaAsCompletedUseCase,
    private val createAndSubmitRouteProgressUseCase: CreateAndSubmitRouteProgressUseCase,
    private val getRouteProgressByIdUseCase: GetRouteProgressByIdUseCase,
    private val firebaseAuth: FirebaseAuth
) : ViewModel(){


    private val _routeProgressState = MutableStateFlow(CommonRouteProgressState<RouteProgressModel?>())
    val routeProgressState = _routeProgressState.asStateFlow()

    private val _updateAreaCompletedState = MutableStateFlow(CommonRouteProgressState<Unit>())
    val updateAreaCompletedState = _updateAreaCompletedState.asStateFlow()

    private val _markRouteAsCompletedState = MutableStateFlow(CommonRouteProgressState<Unit>())
    val markRouteAsCompletedState = _markRouteAsCompletedState.asStateFlow()


    private val _createAndSubmitRouteProgressState = MutableStateFlow(CommonRouteProgressState<Unit>())
    val createAndSubmitRouteProgressState = _createAndSubmitRouteProgressState.asStateFlow()

    private val _getRouteProgressByIdState= MutableStateFlow(CommonRouteProgressState<RouteProgressModel>())
    val getRouteProgressState = _getRouteProgressByIdState.asStateFlow()



    fun getRouteProgress() {
        if(firebaseAuth.currentUser?.uid?.isEmpty() == true) return

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

    fun updateAreaCompleted(documentId: String, areaId: String, isCompleted: Boolean) {
        viewModelScope.launch {
            _updateAreaCompletedState.value = CommonRouteProgressState(isLoading = true)
            val result = updateAreaCompletedUseCase.updateAreaCompleted(documentId, areaId, isCompleted)
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

    fun markRouteAsCompleted(documentId: String) {
        viewModelScope.launch {
            _markRouteAsCompletedState.value = CommonRouteProgressState(isLoading = true)
            val result = markAreaAsCompletedUseCase.markRouteAsCompleted(documentId)
            when (result) {
                is ResultState.Success -> {
                    _markRouteAsCompletedState.value = CommonRouteProgressState(success = result.data, isLoading = false)
                }
                is ResultState.Error -> {
                    _markRouteAsCompletedState.value = CommonRouteProgressState(error = result.message, isLoading = false)
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

            val currentUserId = firebaseAuth.currentUser?.uid
            if (currentUserId.isNullOrEmpty()) {
                _createAndSubmitRouteProgressState.value = CommonRouteProgressState(error = "User not authenticated.", isLoading = false)
                return@launch
            }

            val today = LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE)
            val areaProgressList = selectedRoute.areaList.map { areaInfo ->
                AreaProgress(
                    areaId = areaInfo.areaId,
                    areaName = areaInfo.areaName,
                    isCompleted = false,
                    completedAt = null
                )
            }

            val driverId = if (selectedRole.equals("Driver", ignoreCase = true)) currentUserId else ""
            val collectorId = if (selectedRole.equals("Collector", ignoreCase = true)) currentUserId else ""

            val progressModel = RouteProgressModel(
                routeId = selectedRoute.id,
                date = today,
                assignedCollectorId = collectorId,
                assignedDriverId = driverId,
                assignedTruckId = selectedTruck.id,
                areaProgress = areaProgressList,
                routeCompleted = false
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

    fun getRouteProgressById(routeID : String){
        viewModelScope.launch(Dispatchers.IO) {
            _getRouteProgressByIdState.value= CommonRouteProgressState(isLoading = true)
            val result=getRouteProgressByIdUseCase.getRouteProgressById(routeID)

            when(result){
                is ResultState.Error -> {
                    _getRouteProgressByIdState.value= CommonRouteProgressState(error = result.message, isLoading = false)

                }
                is ResultState.Success-> {
                    _getRouteProgressByIdState.value= CommonRouteProgressState(isLoading = false, success = result.data)
                }
                else -> {

                }
            }
        }
    }

}


data class CommonRouteProgressState<T>(
    val isLoading : Boolean = false,
    val success : T? = null,
    val error : String = ""
)