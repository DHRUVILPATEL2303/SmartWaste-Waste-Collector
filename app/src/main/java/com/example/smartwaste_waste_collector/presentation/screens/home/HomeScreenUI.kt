package com.example.smartwaste_waste_collector.presentation.screens.home

import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.example.smartwaste_waste_collector.data.models.RouteModel
import com.example.smartwaste_waste_collector.data.models.TruckModel
import com.example.smartwaste_waste_collector.presentation.viewmodels.routeprogressviewmodel.CommonRouteProgressState
import com.example.smartwaste_waste_collector.presentation.viewmodels.routeprogressviewmodel.RouteProgressViewModel
import com.example.smartwaste_waste_collector.presentation.viewmodels.rrouteviewmodel.CommonRouteState
import com.example.smartwaste_waste_collector.presentation.viewmodels.rrouteviewmodel.RouteViewModel
import com.example.smartwaste_waste_collector.presentation.viewmodels.truckviewmodel.CommonTruckState
import com.example.smartwaste_waste_collector.presentation.viewmodels.truckviewmodel.TruckViewModel
import com.google.firebase.auth.FirebaseAuth

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
    val truckState by truckViewModel.allTruckState.collectAsState()
    val routeState by routeViewModel.allroutestate.collectAsState()

    val currentUserId = FirebaseAuth.getInstance().currentUser?.uid ?: ""


    var showDialog by remember {
        mutableStateOf(false)
    }


    LaunchedEffect(routeProgressState) {
        if (!routeProgressState.isLoading) {
            val progress = routeProgressState.success
            val hasAssignment = progress != null &&
                    (progress.assignedDriverId == currentUserId || progress.assignedCollectorId == currentUserId)
            showDialog = !hasAssignment

            if (showDialog) {
                truckViewModel.getAllTrucks()
                routeViewModel.getAllRoutes()
            }
        }
    }

    LaunchedEffect(createState) {
        if (!createState.isLoading && createState.success != null) {
            routeProgressViewModel.getRouteProgress()
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(title = { Text("SmartWaste Collector") })
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentAlignment = Alignment.Center
        ) {
            when {
                routeProgressState.isLoading -> {
                    CircularProgressIndicator()
                }

                routeProgressState.success != null -> {
                    val progress = routeProgressState.success!!
                    val role = when (currentUserId) {
                        progress.assignedDriverId -> "driver"
                        progress.assignedCollectorId -> "collector"
                        else -> "unknown"
                    }
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text("Today's Route Progress", style = MaterialTheme.typography.titleLarge)
                        Spacer(modifier = Modifier.height(16.dp))
                        Text("Date: ${progress.date}")
                        Text("Truck ID: ${progress.assignedTruckId}")
                        Text("Route ID: ${progress.routeId}")
                        Text("Role: $role")
                        Spacer(modifier = Modifier.height(16.dp))
                        Text("Area Progress:", style = MaterialTheme.typography.titleMedium)
                        progress.areaProgress.forEach { area ->
                            Text("${area.areaName}: ${if (area.isCompleted) "Completed" else "Pending"}")
                        }
                    }
                }

                routeProgressState.error.isNotEmpty() -> {
                    Text("Error: ${routeProgressState.error}", color = MaterialTheme.colorScheme.error)
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
                showDialog = false
            }
        )
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

    val roleOptions = listOf("driver", "collector")

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("No Route Progress Found") },
        text = {
            Column {
                Text("Fill in route progress details:")
                Spacer(modifier = Modifier.height(12.dp))

                if (truckState.isLoading) {
                    CircularProgressIndicator()
                } else if (truckState.success != null) {
                    ExposedDropdownMenuBox(
                        expanded = expandedTruck,
                        onExpandedChange = { expandedTruck = !expandedTruck }
                    ) {
                        OutlinedTextField(
                            value = selectedTruck?.truckNumber ?: "",
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Truck") },
                            modifier = Modifier.menuAnchor().fillMaxWidth(),
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedTruck) },
                            colors = ExposedDropdownMenuDefaults.textFieldColors()
                        )
                        ExposedDropdownMenu(
                            expanded = expandedTruck,
                            onDismissRequest = { expandedTruck = false }
                        ) {
                            truckState.success.forEach { truck ->
                                DropdownMenuItem(
                                    text = { Text(truck.truckNumber) },
                                    onClick = {
                                        selectedTruck = truck
                                        expandedTruck = false
                                    }
                                )
                            }
                        }
                    }
                } else if (truckState.error.isNotEmpty()) {
                    Text("Error loading trucks: ${truckState.error}", color = MaterialTheme.colorScheme.error)
                }

                Spacer(modifier = Modifier.height(12.dp))

                if (routeState.isLoading) {
                    CircularProgressIndicator()
                } else if (routeState.success != null) {
                    ExposedDropdownMenuBox(
                        expanded = expandedRoute,
                        onExpandedChange = { expandedRoute = !expandedRoute }
                    ) {
                        OutlinedTextField(
                            value = selectedRoute?.name ?: "",
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Route") },
                            modifier = Modifier.menuAnchor().fillMaxWidth(),
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedRoute) },
                            colors = ExposedDropdownMenuDefaults.textFieldColors()
                        )
                        ExposedDropdownMenu(
                            expanded = expandedRoute,
                            onDismissRequest = { expandedRoute = false }
                        ) {
                            routeState.success.forEach { route ->
                                DropdownMenuItem(
                                    text = { Text(route.name) },
                                    onClick = {
                                        selectedRoute = route
                                        expandedRoute = false
                                    }
                                )
                            }
                        }
                    }
                } else if (routeState.error.isNotEmpty()) {
                    Text("Error loading routes: ${routeState.error}", color = MaterialTheme.colorScheme.error)
                }

                Spacer(modifier = Modifier.height(12.dp))

                ExposedDropdownMenuBox(
                    expanded = expandedRole,
                    onExpandedChange = { expandedRole = !expandedRole }
                ) {
                    OutlinedTextField(
                        value = selectedRole.ifEmpty { "Select Role" },
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Role") },
                        modifier = Modifier
                            .menuAnchor()
                            .fillMaxWidth(),
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedRoute) },
                        colors = ExposedDropdownMenuDefaults.textFieldColors()
                    )
                    ExposedDropdownMenu(
                        expanded = expandedRole,
                        onDismissRequest = { expandedRole = false }
                    ) {
                        roleOptions.forEach { role ->
                            DropdownMenuItem(
                                text = { Text(role) },
                                onClick = {
                                    selectedRole = role
                                    expandedRole = false
                                }
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (selectedTruck != null && selectedRoute != null && selectedRole.isNotBlank()) {
                        onSubmit(selectedTruck!!, selectedRoute!!, selectedRole)
                    }
                }
            ) {
                Text("Submit")
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}