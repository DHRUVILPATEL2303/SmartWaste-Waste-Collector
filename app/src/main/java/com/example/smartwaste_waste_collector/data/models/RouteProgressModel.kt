package com.example.smartwaste_waste_collector.data.models

data class RouteProgressModel(
    val routeId: String = "",
    val date: String = "",
    val assignedCollectorId: String = "",
    val assignedDriverId: String = "",
    val assignedTruckId: String = "",
    val areaProgress: List<AreaProgress> = emptyList(),
    val isRouteCompleted: Boolean = false,
    val lastUpdated: Long = System.currentTimeMillis()
)

data class AreaProgress(
    val areaId: String = "",
    val areaName: String = "",
    val isCompleted: Boolean = false,
    val completedAt: Long? = null
)