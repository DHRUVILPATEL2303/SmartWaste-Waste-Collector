package com.example.smartwaste_waste_collector.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.navigation
import androidx.navigation.compose.rememberNavController
import com.example.smartwaste_waste_collector.presentation.screens.authscreen.LoginScreenUI
import com.example.smartwaste_waste_collector.presentation.screens.authscreen.SignUpScreenUI
import com.example.smartwaste_waste_collector.presentation.screens.onboarding.OnBoardingScreenUI
import com.example.smartwaste_waste_collector.presentation.viewmodels.authviewmodel.AuthViewModel


@Composable
fun AppNavigation(viewModel: AuthViewModel = hiltViewModel<AuthViewModel>()){

    val navController = rememberNavController()


    NavHost(
        navController=navController,
        startDestination = SubNavigation.OnBoardingRoutes
    ){
        navigation<SubNavigation.OnBoardingRoutes>(startDestination = Routes.OnBoardingScreen){
            composable<Routes.OnBoardingScreen> {
                OnBoardingScreenUI(navController = navController)
            }
        }
        navigation<SubNavigation.AuthRoutes>(startDestination = Routes.LoginScreen){
            composable<Routes.LoginScreen> {
                LoginScreenUI(navController = navController)
            }
            composable<Routes.SignUpScreen>{
                SignUpScreenUI(navController = navController)
            }

        }
    }

}