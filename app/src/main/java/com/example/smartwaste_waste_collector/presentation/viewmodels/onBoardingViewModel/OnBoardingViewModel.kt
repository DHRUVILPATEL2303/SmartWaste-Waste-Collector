package com.example.smartwaste_waste_collector.presentation.viewmodels.onBoardingViewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.smartwaste_waste_collector.datastore.DataStoreManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject


@HiltViewModel
class OnBoardingViewModel @Inject constructor(
    private val dataStoreManager : DataStoreManager
): ViewModel() {

    // StateFlow that properly handles loading state
    val onboardingState: StateFlow<OnboardingState> = dataStoreManager.onboardingCompleted
        .map { isCompleted -> OnboardingState.Loaded(isCompleted) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.Eagerly, // Changed to Eagerly to load immediately
            initialValue = OnboardingState.Loading // Use Loading as initial state
        )

    // Keep backward compatibility for existing code
    val onboardingCompleted = dataStoreManager.onboardingCompleted

    fun setOnboardingShown() {
        viewModelScope.launch {
            dataStoreManager.setOnboardingCompleted(true)
        }
    }
}