package com.example.smartwaste_waste_collector.data.models

data class TruckModel(
    val id: String = "",
    val truckNumber : String="",
    val timestamp: Long = System.currentTimeMillis()
)