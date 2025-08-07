package com.example.smartwaste_waste_collector.presentation.navigation

import kotlinx.serialization.Serializable

sealed class Routes {

    @Serializable
    object SignUpScreen

    @Serializable
    object LoginScreen

    @Serializable
    object HomeScreen

    @Serializable
    object OnBoardingScreen


    @Serializable
    object ReportScreen

    @Serializable
    object PointsScreen

    @Serializable
    object FeedBackScreen

    @Serializable
    object ProfileScreen

}

sealed class SubNavigation {

    @Serializable
    object HomeRoutes


    @Serializable
    object AuthRoutes

    @Serializable
    object OnBoardingRoutes
}