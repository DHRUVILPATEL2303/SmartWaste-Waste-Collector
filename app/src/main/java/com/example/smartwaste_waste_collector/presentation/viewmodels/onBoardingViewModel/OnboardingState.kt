package com.example.smartwaste_waste_collector.presentation.viewmodels.onBoardingViewModel

sealed class OnboardingState {
    object Loading : OnboardingState()
    data class Loaded(val isCompleted: Boolean) : OnboardingState()
}