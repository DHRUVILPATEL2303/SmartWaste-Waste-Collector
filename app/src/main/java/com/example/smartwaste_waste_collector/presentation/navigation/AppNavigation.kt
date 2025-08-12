package com.example.smartwaste_waste_collector.presentation.navigation

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Scaffold
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Feedback
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.PointOfSale
import androidx.compose.material.icons.filled.Report
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.navigation
import androidx.navigation.compose.rememberNavController
import com.example.bottombar.AnimatedBottomBar
import com.example.bottombar.components.BottomBarItem
import com.example.bottombar.model.IndicatorDirection
import com.example.bottombar.model.IndicatorStyle
import com.example.smartwaste_waste_collector.presentation.screens.authscreen.LoginScreenUI
import com.example.smartwaste_waste_collector.presentation.screens.authscreen.SignUpScreenUI
import com.example.smartwaste_waste_collector.presentation.screens.feedbackscreen.FeedBackScreenUI
import com.example.smartwaste_waste_collector.presentation.screens.home.HomeScreenUI
import com.example.smartwaste_waste_collector.presentation.screens.onboarding.OnBoardingScreenUI
import com.example.smartwaste_waste_collector.presentation.screens.pointsscreen.GivePointsScreenUI
import com.example.smartwaste_waste_collector.presentation.screens.report.ReportScreenUI
import com.example.smartwaste_waste_collector.presentation.viewmodels.authviewmodel.AuthViewModel
import com.example.smartwaste_waste_collector.presentation.viewmodels.onBoardingViewModel.OnBoardingViewModel
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser


@Composable
fun AppNavigation(
    viewModel: AuthViewModel = hiltViewModel(),
    onBoardingViewModel: OnBoardingViewModel = hiltViewModel(),
    isLogin : FirebaseUser?,
    isOnboardingCompleted : Boolean
) {
    val navController = rememberNavController()


    val startDestination = if (!isOnboardingCompleted) {
        SubNavigation.OnBoardingRoutes
    } else if (isLogin!=null){
        SubNavigation.HomeRoutes
    } else {
        SubNavigation.AuthRoutes
    }

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    val currentBaseRoute = currentRoute?.substringBefore("?")?.substringBefore("/")

    var selectedItem by remember { mutableIntStateOf(0) }

    val bottomBarRoutes = listOf(
        Routes.HomeScreen::class.qualifiedName,
        Routes.ReportScreen::class.qualifiedName,
        Routes.PointsScreen::class.qualifiedName,
        Routes.FeedBackScreen::class.qualifiedName
    )

    if (currentBaseRoute in bottomBarRoutes) {
        selectedItem = bottomBarRoutes.indexOf(currentBaseRoute)
    }

    Scaffold(
        bottomBar = {
            if (currentBaseRoute in bottomBarRoutes) {
                AnimatedBottomBar(
                    selectedItem = selectedItem,
                    itemSize = bottomBarItems.size,
                    containerColor = Color(6, 25, 60, 255).copy(alpha = 0.9f),
                    indicatorStyle = IndicatorStyle.FILLED,
                    containerShape = RoundedCornerShape(50.dp),
                    bottomBarHeight = 65.dp,
                    modifier = Modifier.padding(16.dp).navigationBarsPadding(),
                    indicatorColor = Color.White.copy(alpha = 0.4f),
                    indicatorDirection = IndicatorDirection.BOTTOM
                ) {
                    bottomBarItems.forEachIndexed { index, item ->
                        BottomBarItem(
                            selected = selectedItem == index,
                            onClick = {
                                if (selectedItem != index) {
                                    selectedItem = index
                                    val route = when (index) {
                                        0 -> Routes.HomeScreen
                                        1 -> Routes.ReportScreen
                                        2 -> Routes.PointsScreen
                                        3 -> Routes.FeedBackScreen
                                        else -> Routes.HomeScreen
                                    }
                                    navController.navigate(route) {
                                        popUpTo(Routes.HomeScreen) { inclusive = false }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                }
                            },
                            imageVector = item.icon,
                            iconColor = if (selectedItem == index) Color.Red else Color.White,
                            label = item.label,
                            contentColor = Color.Red,
                            textColor = Color.White
                        )
                    }
                }
            }
        }
    ) { paddingValues ->
        BackHandler(enabled = currentBaseRoute in bottomBarRoutes && currentBaseRoute != Routes.HomeScreen::class.qualifiedName) {
            navController.navigate(Routes.HomeScreen) {
                popUpTo(Routes.HomeScreen) { inclusive = false }
                launchSingleTop = true
            }
            selectedItem = 0
        }

        NavHost(
            navController = navController,
            startDestination = startDestination,
            modifier = Modifier.padding(paddingValues).fillMaxSize()
        ) {
            navigation<SubNavigation.OnBoardingRoutes>(startDestination = Routes.OnBoardingScreen) {
                composable<Routes.OnBoardingScreen> { OnBoardingScreenUI(navController=navController) }
            }
            navigation<SubNavigation.AuthRoutes>(startDestination = Routes.LoginScreen) {
                composable<Routes.LoginScreen> { LoginScreenUI(navController=navController) }
                composable<Routes.SignUpScreen> { SignUpScreenUI(navController=navController) }
            }
            navigation<SubNavigation.HomeRoutes>(startDestination = Routes.HomeScreen) {
                composable<Routes.HomeScreen> { HomeScreenUI(navController=navController) }
                composable<Routes.ReportScreen> { ReportScreenUI() }
                composable<Routes.PointsScreen> { GivePointsScreenUI(navController=navController) }
                composable<Routes.FeedBackScreen> { FeedBackScreenUI(navController=navController) }
            }
        }
    }
}



data class BottomBarItem(
    val label: String,
    val icon: ImageVector
)

val bottomBarItems = listOf(
    BottomBarItem("Home", Icons.Default.Home),
    BottomBarItem("Report", Icons.Default.Report),
    BottomBarItem("Points", Icons.Default.PointOfSale),
    BottomBarItem("Feedback", Icons.Default.Feedback)
)


@Composable fun PointScreenUI(navController: NavHostController) { /* TODO */ }
