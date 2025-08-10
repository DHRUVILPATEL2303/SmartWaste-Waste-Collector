package com.example.smartwaste_waste_collector.presentation.screens.home

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.EaseInOutQuad
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.BorderStroke
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.example.smartwaste_waste_collector.data.models.AreaProgress
import com.example.smartwaste_waste_collector.data.models.RouteModel
import com.example.smartwaste_waste_collector.data.models.RouteProgressModel
import com.example.smartwaste_waste_collector.data.models.TruckModel
import com.example.smartwaste_waste_collector.presentation.viewmodels.routeprogressviewmodel.RouteProgressViewModel
import com.example.smartwaste_waste_collector.presentation.viewmodels.rrouteviewmodel.CommonRouteState
import com.example.smartwaste_waste_collector.presentation.viewmodels.rrouteviewmodel.RouteViewModel
import com.example.smartwaste_waste_collector.presentation.viewmodels.truckviewmodel.CommonTruckState
import com.example.smartwaste_waste_collector.presentation.viewmodels.truckviewmodel.TruckViewModel
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreenUI(
    modifier: Modifier = Modifier,
    routeProgressViewModel: RouteProgressViewModel = hiltViewModel(),
    truckViewModel: TruckViewModel = hiltViewModel(),
    routeViewModel: RouteViewModel = hiltViewModel(),
    navController: NavHostController
) {
    val routeProgressState by routeProgressViewModel.routeProgressState.collectAsState()
    val createState by routeProgressViewModel.createAndSubmitRouteProgressState.collectAsState()
    val updateState by routeProgressViewModel.updateAreaCompletedState.collectAsState()
    val truckState by truckViewModel.allTruckState.collectAsState()
    val routeState by routeViewModel.allroutestate.collectAsState()

    val currentUser = FirebaseAuth.getInstance().currentUser
    val currentUserId = currentUser?.uid.orEmpty()
    val currentUserName = currentUser?.displayName ?: "Unknown"

    var showDialog by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()


    LaunchedEffect(routeProgressState) {
        if (!routeProgressState.isLoading) {
            val progress = routeProgressState.success

            val noProgressDocument = progress == null

            val userNotAssigned = progress != null &&
                    progress.assignedDriverId != currentUserId &&
                    progress.assignedCollectorId != currentUserId

            val shouldShowDialog = noProgressDocument || userNotAssigned

            showDialog = shouldShowDialog

            if (shouldShowDialog) {
                truckViewModel.getAllTrucks()
                routeViewModel.getAllRoutes()
            }
        }

    }

    LaunchedEffect(createState, updateState) {
        if ((!createState.isLoading && createState.success != null) ||
            (!updateState.isLoading && updateState.success != null)) {
            routeProgressViewModel.getRouteProgress()
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            HomeTopAppBar(hasRouteProgress = routeProgressState.success != null && !showDialog)
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            when {
                routeProgressState.isLoading -> {
                    LoadingContent("Loading Your Route...")
                }

                routeProgressState.success != null && !showDialog -> {
                    val progress = routeProgressState.success!!
                    val role = getUserRole(progress, currentUserId)

                    LaunchedEffect(Unit) {
                        truckViewModel.getAllTrucks()
                        routeViewModel.getAllRoutes()
                    }

                    when {
                        routeState.isLoading || truckState.isLoading -> {
                            LoadingContent("Loading Route Details...")
                        }

                        routeState.success != null && truckState.success != null -> {
                            val route = routeState.success!!.find { it.id == progress.routeId }
                            val truck = truckState.success!!.find { it.id == progress.assignedTruckId }

                            if (route != null) {
                                RouteProgressContent(
                                    progress = progress,
                                    route = route,
                                    truck = truck,
                                    role = role,
                                    currentUserId = currentUserId,
                                    currentUserName = currentUserName,
                                    routeProgressViewModel = routeProgressViewModel,
                                    coroutineScope = coroutineScope
                                )
                            } else {
                                ErrorContent("Route information not found.")
                            }
                        }

                        else -> {
                            val error = routeState.error.ifEmpty { truckState.error }
                            if (error.isNotEmpty()) {
                                ErrorContent("Error: $error")
                            }
                        }
                    }
                }

                routeProgressState.error.isNotEmpty() -> {
                    ErrorContent("Error: ${routeProgressState.error}")
                }

                else -> {
                    if (!showDialog) {
                        LoadingContent("Checking for active routes...")
                    } else {
                        Column(
                            modifier = Modifier.fillMaxSize(),
                            verticalArrangement = Arrangement.Center,
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(Icons.Default.Route, "No route", modifier = Modifier.size(48.dp), tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
                            Spacer(modifier = Modifier.height(16.dp))
                            Text(
                                "No active route found.",
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                            )
                            Text(
                                "Please set up a new route to begin.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                            )
                        }
                    }
                }
            }
        }
    }

    if (showDialog) {
        RouteProgressDialog(
            truckState = truckState,
            routeState = routeState,
            onSubmit = { truck, route, role ->
                routeProgressViewModel.createAndSubmitRouteProgress(truck, route, role)
                showDialog = false
            },
            onDismiss = {

            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun HomeTopAppBar(hasRouteProgress: Boolean) {
    TopAppBar(
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Icon(
                    Icons.Default.LocalShipping,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onPrimary
                )
                Column {
                    Text(
                        "SmartWaste Collector",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold
                        ),
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                    if (hasRouteProgress) {
                        Text(
                            "Today's Route in Progress",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f)
                        )
                    }
                }
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.primary
        )
    )
}

@Composable
private fun LoadingContent(message: String) {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        CircularProgressIndicator(
            color = MaterialTheme.colorScheme.primary,
            strokeWidth = 4.dp,
            modifier = Modifier.size(48.dp)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            message,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.8f)
        )
    }
}

@Composable
private fun ErrorContent(message: String) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            Icons.Default.CloudOff,
            contentDescription = "Error",
            tint = MaterialTheme.colorScheme.error,
            modifier = Modifier.size(64.dp)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            "Something Went Wrong",
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.onBackground,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            message,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.error,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun EnhancedRouteMapIndicator(
    areas: List<AreaProgress>,
    currentIndex: Int,
    modifier: Modifier = Modifier
) {
    val density = LocalDensity.current

    val circleRadiusPx = with(density) { 14.dp.toPx() }
    val lineStrokeWidthPx = with(density) { 6.dp.toPx() }
    val markerRadiusPx = with(density) { 8.dp.toPx() }

    val primaryColor = MaterialTheme.colorScheme.primary
    val completedColor = MaterialTheme.colorScheme.primary
    val pendingColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
    val activeColor = MaterialTheme.colorScheme.tertiary

    val animatedCurrentIndex by animateFloatAsState(
        targetValue = currentIndex.toFloat(),
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "currentPosition"
    )

    val pulseRadius = rememberInfiniteTransition(label = "pulseTransition").run {
        animateFloat(
            initialValue = circleRadiusPx,
            targetValue = circleRadiusPx * 1.8f,
            animationSpec = infiniteRepeatable(
                animation = tween(1500, easing = EaseInOutQuad),
                repeatMode = RepeatMode.Reverse
            ),
            label = "pulseRadius"
        ).value
    }

    Canvas(modifier = modifier) {
        val centerX = size.width / 2
        val itemHeight = size.height / areas.size
        val pathEffect = PathEffect.dashPathEffect(floatArrayOf(20f, 12f))

        areas.forEachIndexed { index, _ ->
            val centerY = itemHeight * (index + 0.5f)

            if (index < areas.size - 1) {
                val nextCenterY = itemHeight * (index + 1.5f)

                val lineColor = when {
                    index < currentIndex -> completedColor
                    index == currentIndex -> activeColor
                    else -> pendingColor
                }

                drawLine(
                    color = lineColor,
                    start = Offset(centerX, centerY + circleRadiusPx),
                    end = Offset(centerX, nextCenterY - circleRadiusPx),
                    strokeWidth = lineStrokeWidthPx,
                    pathEffect = if (index >= currentIndex) pathEffect else null
                )
            }
        }

        areas.forEachIndexed { index, area ->
            val centerY = itemHeight * (index + 0.5f)
            val isCompleted = area.isCompleted
            val isCurrent = index == currentIndex

            val circleColor = when {
                isCompleted -> completedColor
                isCurrent -> activeColor
                else -> pendingColor
            }

            val radiusMultiplier = if (isCurrent) {
                1.2f + 0.2f * kotlin.math.sin(System.currentTimeMillis() / 300.0).toFloat()
            } else {
                1f
            }

            drawCircle(
                color = circleColor,
                radius = circleRadiusPx * radiusMultiplier,
                center = Offset(centerX, centerY),
                style = Stroke(width = lineStrokeWidthPx)
            )

            if (isCompleted) {
                drawCircle(
                    color = circleColor,
                    radius = (circleRadiusPx - lineStrokeWidthPx / 2) * radiusMultiplier,
                    center = Offset(centerX, centerY)
                )
            }

            if (isCurrent && !isCompleted) {
                drawCircle(
                    color = activeColor.copy(alpha = 0.3f),
                    radius = pulseRadius,
                    center = Offset(centerX, centerY)
                )
            }
        }

        if (currentIndex < areas.size) {
            val markerY = itemHeight * (animatedCurrentIndex + 0.5f)

            drawCircle(
                color = activeColor.copy(alpha = 0.4f),
                radius = markerRadiusPx * 2f,
                center = Offset(centerX, markerY)
            )

            drawCircle(
                color = Color.White,
                radius = markerRadiusPx,
                center = Offset(centerX, markerY)
            )

            drawCircle(
                color = activeColor,
                radius = markerRadiusPx * 0.6f,
                center = Offset(centerX, markerY)
            )
        }
    }
}
@Composable
private fun EnhancedAreaProgressItem(
    area: AreaProgress,
    index: Int,
    isFirst: Boolean,
    isLast: Boolean,
    isCurrent: Boolean,
    swipeThreshold: Float,
    onSwipeComplete: (Boolean) -> Unit
) {
    var offsetX by remember { mutableFloatStateOf(0f) }
    val coroutineScope = rememberCoroutineScope()

    val scale by animateFloatAsState(
        targetValue = if (isCurrent) 1.02f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "itemScale"
    )

    val elevation by animateDpAsState(
        targetValue = if (isCurrent) 8.dp else 4.dp,
        animationSpec = tween(300),
        label = "itemElevation"
    )

    Box(
        modifier = Modifier
            .padding(vertical = 8.dp)
            .graphicsLayer(scaleX = scale, scaleY = scale)
    ) {
        SwipeBackground(offsetX = offsetX, swipeThreshold = swipeThreshold)

        val draggableState = rememberDraggableState { delta ->
            offsetX += delta
        }

        EnhancedAreaCard(
            area = area,
            isCurrent = isCurrent,
            elevation = elevation,
            modifier = Modifier
                .offset { IntOffset(offsetX.roundToInt(), 0) }
                .draggable(
                    state = draggableState,
                    orientation = Orientation.Horizontal,
                    onDragStopped = {
                        coroutineScope.launch {
                            if (offsetX >= swipeThreshold) {
                                onSwipeComplete(true)
                            } else if (offsetX <= -swipeThreshold) {
                                onSwipeComplete(false)
                            }
                            offsetX = 0f
                        }
                    }
                )
        )
    }
}

@Composable
private fun EnhancedAreaCard(
    area: AreaProgress,
    isCurrent: Boolean,
    elevation: Dp,
    modifier: Modifier = Modifier
) {
    val animatedColor by animateColorAsState(
        targetValue = when {
            area.isCompleted -> MaterialTheme.colorScheme.primaryContainer
            isCurrent -> MaterialTheme.colorScheme.tertiaryContainer
            else -> MaterialTheme.colorScheme.errorContainer
        },
        animationSpec = tween(500),
        label = "cardColor"
    )

    val animatedContentColor by animateColorAsState(
        targetValue = when {
            area.isCompleted -> MaterialTheme.colorScheme.onPrimaryContainer
            isCurrent -> MaterialTheme.colorScheme.onTertiaryContainer
            else -> MaterialTheme.colorScheme.onErrorContainer
        },
        animationSpec = tween(500),
        label = "cardContentColor"
    )

    val borderColor by animateColorAsState(
        targetValue = if (isCurrent) MaterialTheme.colorScheme.tertiary else Color.Transparent,
        animationSpec = tween(300),
        label = "borderColor"
    )

    Card(
        modifier = modifier
            .fillMaxWidth()
            .height(80.dp),
        colors = CardDefaults.cardColors(containerColor = animatedColor),
        elevation = CardDefaults.cardElevation(defaultElevation = elevation),
        shape = RoundedCornerShape(16.dp),
        border = if (isCurrent) BorderStroke(2.dp, borderColor) else null
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            val iconRotation by animateFloatAsState(
                targetValue = if (area.isCompleted) 360f else 0f,
                animationSpec = tween(600),
                label = "iconRotation"
            )

            Icon(
                imageVector = when {
                    area.isCompleted -> Icons.Default.CheckCircle
                    isCurrent -> Icons.Default.LocationOn
                    else -> Icons.Default.HourglassTop
                },
                contentDescription = when {
                    area.isCompleted -> "Completed"
                    isCurrent -> "Current Location"
                    else -> "Pending"
                },
                tint = animatedContentColor,
                modifier = Modifier
                    .size(32.dp)
                    .graphicsLayer(rotationZ = iconRotation)
            )

            Column {
                Text(
                    area.areaName,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = if (isCurrent) FontWeight.ExtraBold else FontWeight.Bold
                    ),
                    color = animatedContentColor
                )

                if (isCurrent) {
                    Text(
                        "Current Stop",
                        style = MaterialTheme.typography.bodySmall,
                        color = animatedContentColor.copy(alpha = 0.7f)
                    )
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            if (isCurrent && !area.isCompleted) {
                val infiniteTransition = rememberInfiniteTransition(label = "currentIndicator")
                val alpha by infiniteTransition.animateFloat(
                    initialValue = 0.3f,
                    targetValue = 1f,
                    animationSpec = infiniteRepeatable(
                        animation = tween(1000),
                        repeatMode = RepeatMode.Reverse
                    ),
                    label = "currentAlpha"
                )

                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    color = animatedContentColor.copy(alpha = alpha),
                    strokeWidth = 3.dp
                )
            }
        }
    }
}

@Composable
private fun RouteProgressContent(
    progress: RouteProgressModel,
    route: RouteModel,
    truck: TruckModel?,
    role: String,
    currentUserId: String,
    currentUserName: String,
    routeProgressViewModel: RouteProgressViewModel,
    coroutineScope: kotlinx.coroutines.CoroutineScope
) {
    val configuration = LocalConfiguration.current
    val density = LocalDensity.current
    val screenWidthPx = with(density) { configuration.screenWidthDp.dp.toPx() }
    val swipeThreshold = screenWidthPx * 0.5f

    val completedAreas = progress.areaProgress.count { it.isCompleted }
    val totalAreas = progress.areaProgress.size
    val progressPercentage = if (totalAreas > 0) completedAreas.toFloat() / totalAreas else 0f
    val animatedProgress by animateFloatAsState(
        targetValue = progressPercentage,
        animationSpec = tween(1000),
        label = "progressAnimation"
    )

    val currentAreaIndex = progress.areaProgress.indexOfFirst { !it.isCompleted }
        .takeIf { it != -1 } ?: progress.areaProgress.size - 1

    Row(modifier = Modifier.fillMaxSize()) {
        EnhancedRouteMapIndicator(
            areas = progress.areaProgress,
            currentIndex = currentAreaIndex,
            modifier = Modifier
                .width(50.dp)
                .fillMaxHeight()
                .padding(vertical = 16.dp)
        )

        LazyColumn(
            modifier = Modifier.weight(1f),
            contentPadding = PaddingValues(start = 8.dp, end = 16.dp, bottom = 16.dp)
        ) {
            item {
                RouteInfoHeader(
                    progress = progress,
                    route = route,
                    truck = truck,
                    role = role,
                    currentUserId = currentUserId,
                    currentUserName = currentUserName,
                    completedAreas = completedAreas,
                    totalAreas = totalAreas,
                    progressPercentage = animatedProgress
                )

                AreaProgressHeader()
            }

            items(progress.areaProgress.size) { index ->
                val area = progress.areaProgress[index]
                val isCurrent = index == currentAreaIndex

                EnhancedAreaProgressItem(
                    area = area,
                    index = index,
                    isFirst = index == 0,
                    isLast = index == progress.areaProgress.size - 1,
                    isCurrent = isCurrent,
                    swipeThreshold = swipeThreshold,
                    onSwipeComplete = { isCompleted ->
                        coroutineScope.launch {
                            if (area.isCompleted != isCompleted) {
                                routeProgressViewModel.updateAreaCompleted(
                                    progress.routeId, area.areaId, isCompleted
                                )
                            }
                        }
                    }
                )
            }
        }
    }
}

@Composable
private fun RouteInfoHeader(
    progress: RouteProgressModel,
    route: RouteModel,
    truck: TruckModel?,
    role: String,
    currentUserId: String,
    currentUserName: String,
    completedAreas: Int,
    totalAreas: Int,
    progressPercentage: Float
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        shape = RoundedCornerShape(20.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text(
                "Route Details",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(
                        progress = { progressPercentage },
                        modifier = Modifier.size(60.dp),
                        color = MaterialTheme.colorScheme.primary,
                        trackColor = MaterialTheme.colorScheme.surface,
                        strokeWidth = 5.dp
                    )
                    Text(
                        text = "${(progressPercentage * 100).toInt()}%",
                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                Column {
                    Text(
                        "Overall Progress",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        "$completedAreas of $totalAreas Areas Completed",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                    )
                }
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 20.dp), color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))

            InfoGrid(
                progress = progress,
                route = route,
                truck = truck,
                role = role,
                currentUserId = currentUserId,
                currentUserName = currentUserName
            )
        }
    }
}

@Composable
private fun InfoGrid(
    progress: RouteProgressModel,
    route: RouteModel,
    truck: TruckModel?,
    role: String,
    currentUserId: String,
    currentUserName: String
) {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            InfoItem(icon = Icons.Default.CalendarToday, title = "Date", value = progress.date, modifier = Modifier.weight(1f))
            InfoItem(icon = Icons.Default.Map, title = "Route", value = route.name, modifier = Modifier.weight(1f))
        }
        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            InfoItem(icon = Icons.Default.LocalShipping, title = "Truck", value = truck?.truckNumber ?: "N/A", modifier = Modifier.weight(1f))
            InfoItem(icon = Icons.Default.Badge, title = "Your Role", value = role, modifier = Modifier.weight(1f))
        }
    }
}

@Composable
private fun InfoItem(icon: ImageVector, title: String, value: String, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = title,
            modifier = Modifier
                .size(40.dp)
                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f), CircleShape)
                .padding(8.dp),
            tint = MaterialTheme.colorScheme.primary
        )
        Column {
            Text(
                text = title,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = value,
                style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun AreaProgressHeader() {
    Spacer(modifier = Modifier.height(24.dp))

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier.padding(horizontal = 8.dp)
    ) {
        Icon(
            Icons.Default.LocationOn,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(28.dp)
        )
        Text(
            "Area Stops",
            style = MaterialTheme.typography.headlineSmall.copy(
                fontWeight = FontWeight.SemiBold
            ),
            color = MaterialTheme.colorScheme.onBackground
        )
    }

    Spacer(modifier = Modifier.height(12.dp))

    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.4f)
        ),
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                Icons.Default.Swipe,
                contentDescription = "Swipe hint",
                tint = MaterialTheme.colorScheme.onSecondaryContainer
            )
            Text(
                text = "Swipe right to complete, left to undo",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSecondaryContainer,
            )
        }
    }

    Spacer(modifier = Modifier.height(16.dp))
}

@Composable
fun SwipeBackground(offsetX: Float, swipeThreshold: Float) {
    val completeColor = MaterialTheme.colorScheme.primaryContainer
    val pendingColor = MaterialTheme.colorScheme.errorContainer

    val color = when {
        offsetX > 0 -> completeColor
        offsetX < 0 -> pendingColor
        else -> Color.Transparent
    }

    val icon = when {
        offsetX > swipeThreshold -> Icons.Default.Check
        offsetX < -swipeThreshold -> Icons.Default.Replay
        else -> null
    }

    val iconAlignment = if (offsetX > 0) Alignment.CenterStart else Alignment.CenterEnd
    val iconColor = if (offsetX > 0) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onErrorContainer

    Box(
        modifier = Modifier
            .fillMaxSize()
            .clip(RoundedCornerShape(16.dp))
            .background(color),
        contentAlignment = iconAlignment
    ) {
        if (icon != null) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = iconColor,
                modifier = Modifier
                    .padding(horizontal = 24.dp)
                    .size(32.dp)
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RouteProgressDialog(
    truckState: CommonTruckState<List<TruckModel>>,
    routeState: CommonRouteState<List<RouteModel>>,
    onSubmit: (TruckModel, RouteModel, String) -> Unit,
    onDismiss: () -> Unit
) {
    var selectedTruck by remember { mutableStateOf<TruckModel?>(null) }
    var expandedTruck by remember { mutableStateOf(false) }
    var selectedRoute by remember { mutableStateOf<RouteModel?>(null) }
    var expandedRoute by remember { mutableStateOf(false) }
    var selectedRole by remember { mutableStateOf("") }
    var expandedRole by remember { mutableStateOf(false) }

    val roleOptions = listOf("Driver", "Collector")
    val isSubmitEnabled = selectedTruck != null && selectedRoute != null && selectedRole.isNotBlank()

    BasicAlertDialog(
        onDismissRequest = onDismiss,
    ) {
        Card(
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    Icons.Default.Route,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(32.dp)
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    "Start Your Day",
                    style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    "Select your truck, route, and role to begin.",
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(24.dp))

                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    DropdownField(
                        label = "Select Truck",
                        selectedValue = selectedTruck?.truckNumber.orEmpty(),
                        leadingIcon = Icons.Default.LocalShipping,
                        expanded = expandedTruck,
                        onExpandedChange = { expandedTruck = it },
                        isLoading = truckState.isLoading,
                        error = truckState.error,
                        items = truckState.success.orEmpty(),
                        itemDisplay = { it.truckNumber },
                        onItemSelected = {
                            selectedTruck = it
                            expandedTruck = false
                        }
                    )

                    DropdownField(
                        label = "Select Route",
                        selectedValue = selectedRoute?.name.orEmpty(),
                        leadingIcon = Icons.Default.Map,
                        expanded = expandedRoute,
                        onExpandedChange = { expandedRoute = it },
                        isLoading = routeState.isLoading,
                        error = routeState.error,
                        items = routeState.success.orEmpty(),
                        itemDisplay = { it.name },
                        onItemSelected = {
                            selectedRoute = it
                            expandedRoute = false
                        }
                    )

                    DropdownField(
                        label = "Select Role",
                        selectedValue = selectedRole,
                        leadingIcon = Icons.Default.Badge,
                        expanded = expandedRole,
                        onExpandedChange = { expandedRole = it },
                        isLoading = false,
                        error = "",
                        items = roleOptions,
                        itemDisplay = { it },
                        onItemSelected = {
                            selectedRole = it
                            expandedRole = false
                        }
                    )
                }

                Spacer(Modifier.height(24.dp))

                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.End)
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Cancel")
                    }
                    Button(
                        onClick = {
                            if (isSubmitEnabled) {
                                onSubmit(selectedTruck!!, selectedRoute!!, selectedRole)
                            }
                        },
                        enabled = isSubmitEnabled
                    ) {
                        Text("Start Route")
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun <T> DropdownField(
    label: String,
    selectedValue: String,
    leadingIcon: ImageVector,
    expanded: Boolean,
    onExpandedChange: (Boolean) -> Unit,
    isLoading: Boolean,
    error: String,
    items: List<T>,
    itemDisplay: (T) -> String,
    onItemSelected: (T) -> Unit
) {
    if (isLoading) {
        LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
    } else if (error.isNotEmpty()) {
        Text(
            "Error loading ${label.lowercase()}: $error",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.error
        )
    } else {
        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = onExpandedChange
        ) {
            OutlinedTextField(
                value = selectedValue,
                onValueChange = {},
                readOnly = true,
                label = { Text(label) },
                modifier = Modifier
                    .menuAnchor()
                    .fillMaxWidth(),
                leadingIcon = { Icon(leadingIcon, contentDescription = null) },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                colors = OutlinedTextFieldDefaults.colors()
            )
            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { onExpandedChange(false) },
                modifier = Modifier.background(MaterialTheme.colorScheme.surface)
            ) {
                items.forEach { item ->
                    DropdownMenuItem(
                        text = { Text(itemDisplay(item), style = MaterialTheme.typography.bodyLarge) },
                        onClick = { onItemSelected(item) },
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp)
                    )
                }
            }
        }
    }
}

private fun getUserRole(progress: RouteProgressModel, currentUserId: String): String {
    return when (currentUserId) {
        progress.assignedDriverId -> "Driver"
        progress.assignedCollectorId -> "Collector"
        else -> "Unknown"
    }
}

private fun getCollectorName(
    progress: RouteProgressModel,
    currentUserId: String,
    currentUserName: String
): String {
    return if (progress.assignedCollectorId.isNotEmpty()) {
        if (progress.assignedCollectorId == currentUserId) currentUserName else "Unknown"
    } else "Not assigned"
}