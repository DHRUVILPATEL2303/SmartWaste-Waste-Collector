package com.example.smartwaste_waste_collector.presentation.viewmodels.authviewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.smartwaste_waste_collector.common.ResultState
import com.example.smartwaste_waste_collector.data.models.WorkerModel
import com.example.smartwaste_waste_collector.domain.usecases.authusecases.CreateWorkerAccountUseCase
import com.example.smartwaste_waste_collector.domain.usecases.authusecases.LoginUserWithEmailAndPasswordUseCase
import com.google.firebase.auth.FirebaseUser
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject


@HiltViewModel
class AuthViewModel @Inject constructor(
    private val createWorkerAccountUseCase: CreateWorkerAccountUseCase,
    private val loginUserWithEmailAndPasswordUseCase: LoginUserWithEmailAndPasswordUseCase
) : ViewModel() {

    private val _authState = MutableStateFlow(CommonAuthState<FirebaseUser>())
    val authState = _authState.asStateFlow()

    fun createAcccount(model: WorkerModel, password: String) {
        viewModelScope.launch(Dispatchers.IO) {

            _authState.value = CommonAuthState(isLoading = true)
            val result = createWorkerAccountUseCase.createAccount(model, password)

            when (result) {
                is ResultState.Error -> {
                    _authState.value = CommonAuthState(error = result.message, isLoading = false)
                }

                is ResultState.Success -> {
                    _authState.value = CommonAuthState(success = result.data, isLoading = false)
                }

                else -> {
                    _authState.value =
                        CommonAuthState(isLoading = false, error = "Something went wrong")

                }
            }
        }


    }

    fun loginWithEmailAndPassword(email: String, password: String) {
        viewModelScope.launch(Dispatchers.IO) {
            _authState.value = CommonAuthState(isLoading = true)
            val result =
                loginUserWithEmailAndPasswordUseCase.loginWithEmailAndPassword(email, password)
            when (result) {
                is ResultState.Error -> {
                    _authState.value = CommonAuthState(error = result.message, isLoading = false)

                }

                is ResultState.Success -> {
                    _authState.value = CommonAuthState(success = result.data, isLoading = false)
                }

                else -> {
                    _authState.value =
                        CommonAuthState(isLoading = false, error = "Something went wrong")
                }
            }

        }

    }


}

data class CommonAuthState<T>(
    val isLoading: Boolean = false,
    val success: T? = null,
    val error: String = ""
)