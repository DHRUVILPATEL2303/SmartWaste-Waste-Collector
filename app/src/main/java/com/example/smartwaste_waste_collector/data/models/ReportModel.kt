package com.example.smartwaste_waste_collector.data.models

data class ReportModel(
    var reportId: String = "",
    val userId: String = "",
    val againstDriverId: String? = null,
    val againstCollectorId: String? = null,
    val routeId: String = "",
    val areaId: String = "",
    val areaName: String = "",
    val description: String = "",
    val attachments: List<String> = emptyList(),
    val status: String = "Pending",
    val reportDate: String = ""
)
