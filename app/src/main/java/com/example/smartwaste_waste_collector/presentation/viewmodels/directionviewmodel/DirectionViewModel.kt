package com.example.smartwaste_waste_collector.presentation.viewmodels.directionviewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.smartwaste_waste_collector.data.models.AreaProgress
import com.example.smartwaste_waste_collector.domain.usecases.directionusecase.GetDirectionUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.osmdroid.util.GeoPoint
import javax.inject.Inject

data class RouteUiState(
    val isLoading: Boolean = false,
    val markers: List<AreaProgress> = emptyList(),
    val polylines: List<List<GeoPoint>> = emptyList(), // multiple legs
    val error: String? = null
)


@HiltViewModel
class RouteMapViewModel @Inject constructor(
    private val getRouteDirectionUseCase: GetDirectionUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(RouteUiState())
    val state = _state.asStateFlow()

    fun loadRoute(areaList: List<AreaProgress>) {
        if (areaList.isEmpty()) {
            _state.value = RouteUiState(isLoading = false, markers = emptyList(), polylines = emptyList())
            return
        }

        _state.value = _state.value.copy(isLoading = true, error = null)

        viewModelScope.launch {
            val legs = mutableListOf<List<GeoPoint>>()

            for (i in 0 until areaList.size - 1) {
                val a = areaList[i]
                val b = areaList[i + 1]
                val route = getRouteDirectionUseCase.getDirectionUseCase(
                    startLat = a.latitude, startLng = a.longitude,
                    endLat = b.latitude, endLng = b.longitude
                )
                if (route.isNotEmpty()) {
                    legs.add(route)
                }
            }

            _state.value = RouteUiState(
                isLoading = false,
                markers = areaList,
                polylines = legs,
                error = null
            )
        }
    }
}