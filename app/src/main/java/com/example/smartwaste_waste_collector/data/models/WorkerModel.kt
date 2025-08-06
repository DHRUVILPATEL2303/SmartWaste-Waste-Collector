package com.example.smartwaste_waste_collector.data.models

data class WorkerModel(
    val id: String = "",
    val name: String = "",
    val phone: String = "",
    val email: String = "",
    val assignedTruckId: String = "",
    val city: String = "",
    val ward: String = "",
    val role: WorkerRole = WorkerRole.COLLECTOR
)

enum class WorkerRole {
    DRIVER,
    COLLECTOR
}
