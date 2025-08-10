package com.example.smartwaste_waste_collector.data.models

data class WorkerFeedBackModel(
    val feedbackId:String="",
    val driverId:String="",

    val collectorId:String="",
    val userId:String="",
    val routeId:String="",
    val outOf5: String="",
    val feedbackDate:String="",
    val improvement:String=""
)

