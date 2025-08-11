package com.example.smartwaste_waste_collector.data.models

data class UserPointModel(
    val id: String = "",
    val userId: String = "",
    val workerID: String = "",
    val points: String="",
    val timestamp: Long = System.currentTimeMillis(),
    val images : List<String> = emptyList()
)
