package com.example.smartwaste_waste_collector.presentation.screens.home

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.example.smartwaste_waste_collector.data.models.DailyAssignment
import com.example.smartwaste_waste_collector.data.models.TruckModel
import com.example.smartwaste_waste_collector.presentation.viewmodels.dailyassignviewmodel.DailyAssignViewModel
import com.example.smartwaste_waste_collector.presentation.viewmodels.truckviewmodel.CommonTruckState
import com.example.smartwaste_waste_collector.presentation.viewmodels.truckviewmodel.TruckViewModel
import com.google.firebase.auth.FirebaseAuth

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreenUI(
    modifier: Modifier = Modifier,
    dailyAssignViewModel: DailyAssignViewModel = hiltViewModel(),
    truckViewModel: TruckViewModel = hiltViewModel(),
    navController: NavHostController
) {
    val dailyAssignState by dailyAssignViewModel.dailyAssignState.collectAsState()
    val submitState by dailyAssignViewModel.submitAssignState.collectAsState()
    val truckState by truckViewModel.allTruckState.collectAsState()

    var showDialog by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        dailyAssignViewModel.getDailyAssignment()
    }

    LaunchedEffect(showDialog) {
        if (showDialog) {
            truckViewModel.getAllTrucks()
        }
    }

    LaunchedEffect(dailyAssignState) {
        if (!dailyAssignState.isLoading && dailyAssignState.success == null && dailyAssignState.error.isEmpty()) {
            showDialog = true
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
                dailyAssignState.isLoading -> {
                    CircularProgressIndicator()
                }

                dailyAssignState.success != null -> {
                    val assignment = dailyAssignState.success!!
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text("Today's Assignment", style = MaterialTheme.typography.titleLarge)
                        Spacer(modifier = Modifier.height(16.dp))
                        Text("Date: ${assignment.date}")
                        Text("Truck ID: ${assignment.truckId}")
                        Text("Route ID: ${assignment.routeId}")
                        Text("Role: ${assignment.role}")
                    }
                }

                dailyAssignState.error.isNotEmpty() -> {
                    Text("Error: ${dailyAssignState.error}", color = MaterialTheme.colorScheme.error)
                }
            }
        }
    }

    if (showDialog) {
        AssignmentDialog(
            truckState = truckState,
            onSubmit = { assignment ->
                dailyAssignViewModel.submitDailyAssignment(assignment)
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
fun AssignmentDialog(
    truckState: CommonTruckState<List<TruckModel>>,
    onSubmit: (DailyAssignment) -> Unit,
    onDismiss: () -> Unit
) {
    var selectedTruck by remember { mutableStateOf<TruckModel?>(null) }
    var expandedTruck by remember { mutableStateOf(false) }

    var routeId by remember { mutableStateOf("") }

    var selectedRole by remember { mutableStateOf("") }
    var expandedRole by remember { mutableStateOf(false) }

    val roleOptions = listOf("driver", "collector")

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("No Assignment Found") },
        text = {
            Column {
                Text("Fill in assignment details:")
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

                OutlinedTextField(
                    value = routeId,
                    onValueChange = { routeId = it },
                    label = { Text("Route ID") },
                    keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Text),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(12.dp))


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
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedRole) },
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
                    if (selectedTruck != null && routeId.isNotBlank() && selectedRole.isNotBlank()) {
                        val today = java.time.LocalDate.now().toString()
                        val assignment = DailyAssignment(
                            date = today,
                            truckId = selectedTruck!!.id,
                            routeId = routeId,
                            role = selectedRole,
                            collectorId = if (selectedRole == "collector") FirebaseAuth.getInstance().currentUser!!.uid else "",
                            driverId = if (selectedRole == "driver") FirebaseAuth.getInstance().currentUser!!.uid else ""
                        )
                        onSubmit(assignment)
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