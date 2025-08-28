package com.example.smartwaste_waste_collector.presentation.screens.routemapsscreen

import android.Manifest
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import androidx.annotation.DrawableRes
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.example.smartwaste_waste_collector.R
import com.example.smartwaste_waste_collector.presentation.viewmodels.directionviewmodel.RouteMapViewModel
import com.example.smartwaste_waste_collector.presentation.viewmodels.routeprogressviewmodel.RouteProgressViewModel
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.BitmapDescriptor
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.gms.maps.model.MapStyleOptions
import com.google.maps.android.compose.*
import kotlinx.coroutines.launch

@OptIn(ExperimentalPermissionsApi::class, ExperimentalMaterial3Api::class)
@Composable
fun RouteMapScreenUI(
    navController: NavHostController,
    routeId: String,
    routeProgressViewModel: RouteProgressViewModel = hiltViewModel(),
    routeMapViewModel: RouteMapViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    var isDarkMode by rememberSaveable { mutableStateOf(false) }
    var hasZoomedToRoute by rememberSaveable { mutableStateOf(false) }

    val scope = rememberCoroutineScope()
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(LatLng(23.0225, 72.5714), 12f) // Default to Ahmedabad
    }
    val mapUiSettings by remember {
        mutableStateOf(
            MapUiSettings(
                zoomControlsEnabled = false, // We use custom controls
                myLocationButtonEnabled = false // We use a custom FAB
            )
        )
    }

    // State for map type selection
    var selectedMapTypeIndex by rememberSaveable { mutableStateOf(0) }
    val mapTypes = listOf(MapType.NORMAL, MapType.HYBRID, MapType.SATELLITE)
    val mapTypeLabels = listOf("Normal", "Hybrid", "Satellite")

    val routeProgressState = routeProgressViewModel.getRouteProgressState.collectAsState().value
    val mapState = routeMapViewModel.state.collectAsState().value
    val selectedRoute = routeProgressState.success

    val locationPermissionsState = rememberMultiplePermissionsState(
        listOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION,
        )
    )

    LaunchedEffect(key1 = routeId) {
        if (!locationPermissionsState.allPermissionsGranted) {
            locationPermissionsState.launchMultiplePermissionRequest()
        }
        routeProgressViewModel.getRouteProgressById(routeId)
    }

    LaunchedEffect(selectedRoute) {
        selectedRoute?.let {
            routeMapViewModel.loadRoute(it.areaProgress)
        }
    }

    val backgroundColor = if (isDarkMode) Color(0xFF121212) else MaterialTheme.colorScheme.background
    val onSurfaceColor = if (isDarkMode) Color.White else Color.Black
    val primaryColor = if (isDarkMode) Color(0xFFBB86FC) else MaterialTheme.colorScheme.primary
    val cardColor = if (isDarkMode) Color(0xFF1E1E1E) else Color.White
    val mapStyle = if (isDarkMode) MapStyleOptions.loadRawResourceStyle(context, R.raw.map_style_dark) else null

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(selectedRoute?.routeId ?: "Loading Route...", color = onSurfaceColor) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", tint = onSurfaceColor)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = backgroundColor)
            )
        },
        containerColor = backgroundColor,
        contentColor = onSurfaceColor
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            GoogleMap(
                modifier = Modifier.fillMaxSize(),
                cameraPositionState = cameraPositionState,
                uiSettings = mapUiSettings,
                properties = MapProperties(
                    isMyLocationEnabled = locationPermissionsState.allPermissionsGranted,
                    mapType = mapTypes[selectedMapTypeIndex],
                    mapStyleOptions = mapStyle
                )
            ) {
                selectedRoute?.areaProgress?.forEach { area ->
                    val pinColor = if (area.isCompleted) Color(0xFF4CAF50) else Color(0xFFF44336) // Green or Red
                    Marker(
                        state = MarkerState(position = LatLng(area.latitude, area.longitude)),
                        title = area.areaName,

                    )
                }

                mapState.polylines.forEach { leg ->
                    if (leg.isNotEmpty()) {
                        val latLngList = leg.map { geoPoint -> LatLng(geoPoint.latitude, geoPoint.longitude) }
                        Polyline(
                            points = latLngList,
                            color = primaryColor,
                            width = 12f
                        )
                    }
                }
            }

            LaunchedEffect(mapState.polylines) {
                if (mapState.polylines.isNotEmpty() && !hasZoomedToRoute) {
                    val allPoints = mutableListOf<LatLng>()
                    selectedRoute?.areaProgress?.forEach { allPoints.add(LatLng(it.latitude, it.longitude)) }
                    mapState.polylines.flatten().forEach { allPoints.add(LatLng(it.latitude, it.longitude)) }

                    if (allPoints.size > 1) {
                        val boundsBuilder = LatLngBounds.Builder()
                        allPoints.forEach { boundsBuilder.include(it) }
                        cameraPositionState.move(CameraUpdateFactory.newLatLngBounds(boundsBuilder.build(), 150))
                        hasZoomedToRoute = true
                    }
                }
            }

            Column(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(16.dp),
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                IconButton(
                    onClick = { isDarkMode = !isDarkMode },
                    modifier = Modifier.background(cardColor.copy(alpha = 0.8f), RoundedCornerShape(8.dp))
                ) {
                    Icon(
                        imageVector = if (isDarkMode) Icons.Default.LightMode else Icons.Default.DarkMode,
                        contentDescription = "Toggle Theme", tint = onSurfaceColor
                    )
                }

                SingleChoiceSegmentedButtonRow {
                    mapTypeLabels.forEachIndexed { index, label ->
                        SegmentedButton(
                            shape = SegmentedButtonDefaults.baseShape,
                            onClick = { selectedMapTypeIndex = index },
                            selected = index == selectedMapTypeIndex,
                            colors = SegmentedButtonDefaults.colors(
                                activeContainerColor = primaryColor,
                                activeContentColor = onSurfaceColor,
                                inactiveContainerColor = cardColor.copy(alpha = 0.8f),
                                inactiveContentColor = onSurfaceColor
                            )
                        ) {
                            Text(label, style = MaterialTheme.typography.labelSmall)
                        }
                    }
                }
            }

            Column(
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Zoom In
                SmallFloatingActionButton(
                    onClick = { scope.launch { cameraPositionState.animate(CameraUpdateFactory.zoomIn()) } },
                    containerColor = cardColor, contentColor = onSurfaceColor
                ) {
                    Icon(Icons.Default.Add, "Zoom In")
                }
                // Zoom Out
                SmallFloatingActionButton(
                    onClick = { scope.launch { cameraPositionState.animate(CameraUpdateFactory.zoomOut()) } },
                    containerColor = cardColor, contentColor = onSurfaceColor
                ) {
                    Icon(Icons.Default.Remove, "Zoom Out")
                }
            }

            if (locationPermissionsState.allPermissionsGranted) {
                FloatingActionButton(
                    onClick = {
                },
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(16.dp),
                    containerColor = primaryColor
                ) {
                    Icon(Icons.Default.MyLocation, "My Location", tint = onSurfaceColor)
                }
            }

            when {
                routeProgressState.isLoading -> {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                }
                selectedRoute != null -> {
                    val completedCount = selectedRoute.areaProgress.count { it.isCompleted }
                    val totalCount = selectedRoute.areaProgress.size
                    val progress = if (totalCount > 0) completedCount.toFloat() / totalCount else 0f

                    Card(
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .fillMaxWidth(),
                        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
                        elevation = CardDefaults.cardElevation(8.dp),
                        colors = CardDefaults.cardColors(containerColor = cardColor)
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Text(
                                text = "Route: ${selectedRoute.routeId}",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = onSurfaceColor
                            )
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("Progress: $completedCount / $totalCount Areas", color = onSurfaceColor)
                                Text("${(progress * 100).toInt()}%", color = primaryColor, fontWeight = FontWeight.Bold)
                            }
                            LinearProgressIndicator(
                                progress = { progress },
                                modifier = Modifier.fillMaxWidth(),
                                color = primaryColor,
                                trackColor = onSurfaceColor.copy(alpha = 0.3f)
                            )
                            Button(
                                onClick = {
                                    selectedRoute.areaProgress.find { !it.isCompleted }?.let { area ->
                                        routeProgressViewModel.updateAreaCompleted(selectedRoute.routeId, area.areaId, true)
                                    }
                                },
                                modifier = Modifier.fillMaxWidth(),
                                enabled = completedCount < totalCount,
                                colors = ButtonDefaults.buttonColors(containerColor = primaryColor)
                            ) {
                                Text("Mark Next Area as Complete", color = onSurfaceColor)
                            }
                        }
                    }
                }
            }
        }
    }
}


fun bitmapDescriptorFromVector(
    context: Context,
    @DrawableRes vectorResId: Int,
    tintColor: Color
): BitmapDescriptor? {
    val drawable = ContextCompat.getDrawable(context, vectorResId) ?: return null
    drawable.setBounds(0, 0, drawable.intrinsicWidth, drawable.intrinsicHeight)
    drawable.setTint(tintColor.hashCode())
    val bm = Bitmap.createBitmap(
        drawable.intrinsicWidth,
        drawable.intrinsicHeight,
        Bitmap.Config.ARGB_8888
    )
    val canvas = Canvas(bm)
    drawable.draw(canvas)
    return BitmapDescriptorFactory.fromBitmap(bm)
}