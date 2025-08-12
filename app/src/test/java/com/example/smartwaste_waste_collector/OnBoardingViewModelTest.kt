package com.example.smartwaste_waste_collector.presentation.viewmodels.onBoardingViewModel

import org.junit.Test
import org.junit.Assert.*

/**
 * Unit tests for OnboardingState sealed class and related logic
 */
class OnBoardingStateTest {
    
    @Test
    fun `OnboardingState Loading is singleton object`() {
        // Given & When
        val loading1 = OnboardingState.Loading
        val loading2 = OnboardingState.Loading
        
        // Then
        assertSame("Loading should be singleton", loading1, loading2)
    }
    
    @Test
    fun `OnboardingState Loaded holds isCompleted value correctly`() {
        // Given & When
        val loadedTrue = OnboardingState.Loaded(true)
        val loadedFalse = OnboardingState.Loaded(false)
        
        // Then
        assertTrue("Loaded(true) should have isCompleted=true", loadedTrue.isCompleted)
        assertFalse("Loaded(false) should have isCompleted=false", loadedFalse.isCompleted)
    }
    
    @Test
    fun `OnboardingState Loaded instances with same value are equal`() {
        // Given & When
        val loaded1 = OnboardingState.Loaded(true)
        val loaded2 = OnboardingState.Loaded(true)
        val loaded3 = OnboardingState.Loaded(false)
        
        // Then
        assertEquals("Loaded instances with same value should be equal", loaded1, loaded2)
        assertNotEquals("Loaded instances with different values should not be equal", loaded1, loaded3)
    }
    
    @Test
    fun `Loading and Loaded are different types`() {
        // Given & When
        val loading = OnboardingState.Loading
        val loaded = OnboardingState.Loaded(false)
        
        // Then
        assertTrue("Should be Loading type", loading is OnboardingState.Loading)
        assertTrue("Should be Loaded type", loaded is OnboardingState.Loaded)
        assertNotEquals("Loading and Loaded should not be equal", loading, loaded)
    }
}