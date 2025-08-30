package com.example.smartwaste_waste_collector

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.smartwaste_waste_collector.presentation.navigation.AppNavigation
import com.example.smartwaste_waste_collector.presentation.viewmodels.onBoardingViewModel.OnBoardingViewModel
import com.example.smartwaste_waste_collector.presentation.viewmodels.routeprogressviewmodel.RouteProgressViewModel
import com.example.smartwaste_waste_collector.ui.theme.SmartWasteWasteCollectorTheme
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        installSplashScreen()

        enableEdgeToEdge()
        setContent {
            SmartWasteWasteCollectorTheme {
                val firebaseAuth = FirebaseAuth.getInstance()
                val onBoardingViewModel: OnBoardingViewModel = hiltViewModel()
                val isOnboardingCompleted by onBoardingViewModel.onboardingCompleted.collectAsState()
                var showSplash by remember { mutableStateOf(true) }


                LaunchedEffect(Unit) {
                    delay(5000)
                    showSplash = false
                }



                val routeProgressViewModel: RouteProgressViewModel = hiltViewModel()




                Log.d("MainActivity", "RouteProgressState in MainActivity: ${routeProgressViewModel.routeProgressState.collectAsState().value}")

                if (showSplash || isOnboardingCompleted == null) {
                    SplashScreen()
                } else {

                    AppNavigation(
                        isOnboardingCompleted = isOnboardingCompleted!!,
                        isLogin = firebaseAuth.currentUser,
                        routeProgressViewModel = routeProgressViewModel
                    )
                }
            }
        }
    }
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!",
        modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    SmartWasteWasteCollectorTheme {
        Greeting("Android")
    }
}

@Composable
fun SplashScreen(routeProgressViewModel: RouteProgressViewModel=hiltViewModel<RouteProgressViewModel>()) {
    var showContent by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        delay(500)
        showContent = true
    }



    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF4CAF50),
                        Color(0xFF2E7D32)
                    )
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(140.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.95f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_launcher_foreground),
                    contentDescription = "App Logo",
                    modifier = Modifier
                        .size(100.dp)
                        .padding(8.dp),
                    tint = Color.Unspecified
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            if (showContent) {
                Text(
                    text = "Smart Waste",
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    textAlign = TextAlign.Center
                )

                Text(
                    text = "Waste Collector",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color.White.copy(alpha = 0.9f),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(top = 4.dp)
                )

                Spacer(modifier = Modifier.height(48.dp))

                CircularProgressIndicator(
                    color = Color.White,
                    strokeWidth = 3.dp,
                    modifier = Modifier.size(32.dp)
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Loading...",
                    fontSize = 14.sp,
                    color = Color.White.copy(alpha = 0.8f),
                    textAlign = TextAlign.Center
                )
            }
        }

        if (showContent) {
            Column(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 48.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Making waste management smarter",
                    fontSize = 12.sp,
                    color = Color.White.copy(alpha = 0.7f),
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Version 1.0",
                    fontSize = 10.sp,
                    color = Color.White.copy(alpha = 0.6f),
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun SplashScreenPreview() {
    SmartWasteWasteCollectorTheme {
        SplashScreen()
    }
}