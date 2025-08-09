package com.example.smartwaste_waste_collector.presentation.viewmodels.rrouteviewmodel

import android.text.BoringLayout
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelStore
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.smartwaste_waste_collector.data.models.RouteModel
import com.example.smartwaste_waste_collector.domain.usecases.routerepo.GetAllRoutesUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class RouteViewModel @Inject constructor(
    private val getAllRoutesUseCase: GetAllRoutesUseCase
) : ViewModel() {

    private val _allroutestate= MutableStateFlow(CommonRouteState<List<RouteModel>>())
    val allroutestate = _allroutestate.asStateFlow()

    init {
        getAllRoutes()
    }
    fun getAllRoutes(){
        viewModelScope.launch(Dispatchers.IO) {
            getAllRoutesUseCase.getAllRoutes().collect{

                when(it){
                    is com.example.smartwaste_waste_collector.common.ResultState.Loading ->{
                        _allroutestate.value = CommonRouteState(isLoading = true)
                    }

                    is com.example.smartwaste_waste_collector.common.ResultState.Success ->{
                        _allroutestate.value = CommonRouteState(success = it.data)
                    }
                    is com.example.smartwaste_waste_collector.common.ResultState.Error ->{
                        _allroutestate.value = CommonRouteState(error = it.message)
                    }

                }
            }

        }
    }



}

data class CommonRouteState<T>(
    val isLoading : Boolean = false,
    val success : T? = null,
    val error : String = ""
)