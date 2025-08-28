package com.example.smartwaste_waste_collector.data.models

data class ORSRouteResponse(
    val features: List<Feature>?
) {
    data class Feature(
        val geometry: Geometry?
    )
    data class Geometry(
        val coordinates: List<List<Double>>?
    )
}