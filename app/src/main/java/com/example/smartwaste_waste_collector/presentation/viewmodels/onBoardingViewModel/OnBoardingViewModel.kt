package com.example.smartwaste_waste_collector.presentation.viewmodels.onBoardingViewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.smartwaste_waste_collector.datastore.DataStoreManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject


@HiltViewModel
class OnBoardingViewModel @Inject constructor(
    private val dataStoreManager : DataStoreManager
): ViewModel() {


    val onboardingCompleted = dataStoreManager.onboardingCompleted

    fun setOnboardingShown() {
        viewModelScope.launch {
            dataStoreManager.setOnboardingCompleted(true)
        }
    }
}