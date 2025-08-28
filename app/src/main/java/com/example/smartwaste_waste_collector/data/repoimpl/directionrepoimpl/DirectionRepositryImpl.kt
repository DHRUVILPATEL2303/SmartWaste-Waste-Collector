package com.example.smartwaste_waste_collector.data.repoimpl.directionrepoimpl

import com.example.smartwaste_waste_collector.data.remote.NetworkModule
import com.example.smartwaste_waste_collector.domain.repo.directionRepo.DirectionsRepositry
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.osmdroid.util.GeoPoint
import javax.inject.Inject

class DirectionRepositryImpl @Inject constructor(

) : DirectionsRepositry {

    private val api= NetworkModule.provideORSApi()
    override suspend fun fetchRoute(
        startLat: Double,
        startLng: Double,
        endLat: Double,
        endLng: Double
    ): List<GeoPoint> = withContext(Dispatchers.IO){
        try {
            val start = "${startLng},${startLat}"
            val end = "${endLng},${endLat}"
            val response = api.getDrivingRoute("eyJvcmciOiI1YjNjZTM1OTc4NTExMTAwMDFjZjYyNDgiLCJpZCI6ImQwNmU1ZDNjMmVhZTQxYjI5ODdjZThjMGVhNTBhNTc2IiwiaCI6Im11cm11cjY0In0=", start, end)

            val coords = response.features
                ?.firstOrNull()
                ?.geometry
                ?.coordinates
                ?: emptyList()

            coords.mapNotNull { pair ->
                if (pair.size >= 2) GeoPoint(pair[1], pair[0]) else null
            }
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }


}