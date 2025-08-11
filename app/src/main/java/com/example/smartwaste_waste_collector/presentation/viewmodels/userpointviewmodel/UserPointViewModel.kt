// presentation/viewmodels/userpointviewmodel/UserPointViewModel.kt

package com.example.smartwaste_waste_collector.presentation.viewmodels.userpointviewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.smartwaste_waste_collector.common.ResultState
import com.example.smartwaste_waste_collector.data.models.UserPointModel
import com.example.smartwaste_waste_collector.domain.usecases.userpointusecase.GivePointToUserUseCase
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class UserPointViewModel @Inject constructor(
    private val givePointToUserUseCase: GivePointToUserUseCase,
    private val auth: FirebaseAuth // Inject FirebaseAuth
) : ViewModel() {

    private val _state = MutableStateFlow(CommonUserPointState<String>())
    val state = _state.asStateFlow()

    private var scannedUserId: String? = null

    fun onQrScanned(userId: String) {
        scannedUserId = userId

        _state.value = CommonUserPointState()
    }

    fun givePoints(points: String) {
        val userId = scannedUserId
        if (userId.isNullOrEmpty()) {
            _state.value = CommonUserPointState(error = "No QR code scanned")
            return
        }

        val workerId = auth.currentUser?.uid
        if (workerId.isNullOrEmpty()) {
            _state.value = CommonUserPointState(error = "Worker not logged in. Please restart the app.")
            return
        }

        val userPointModel = UserPointModel(

            userId = userId,
            workerID = workerId,
            points = points,
            images = emptyList()
        )

        viewModelScope.launch(Dispatchers.IO) {
            givePointToUserUseCase.givePointsToUser(userPointModel).collect { result ->
                when (result) {
                    is ResultState.Loading -> {
                        _state.value = CommonUserPointState(isLoading = true)
                    }
                    is ResultState.Success -> {
                        _state.value = CommonUserPointState(success = result.data, isLoading = false)
                    }
                    is ResultState.Error -> {
                        _state.value = CommonUserPointState(error = result.message, isLoading = false)
                    }
                }
            }
        }
    }

    fun resetState() {
        _state.value = CommonUserPointState()
        scannedUserId = null
    }
}

data class CommonUserPointState<T>(
    val isLoading: Boolean = false,
    val success: T? = null,
    val error: String = ""
)