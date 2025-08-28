package com.example.smartwaste_waste_collector.data.repoimpl.locationrepoimpl

import android.location.Location
import com.example.smartwaste_waste_collector.common.ROUTE_PROGRESS_MODEL
import com.example.smartwaste_waste_collector.domain.repo.locationrepo.LocationRepositry
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class LocationRepositryImpl @Inject constructor(
    private val firestore: FirebaseFirestore
) : LocationRepositry{
    override suspend fun updateCollectorLocation(routeId : String,lat: Double, lng: Double) {
        try {
            firestore.collection(ROUTE_PROGRESS_MODEL)
                .document(routeId)
                .update(
                    mapOf(
                        "workerLat" to lat,
                        "workerLng" to lng,
                        "lastUpdated" to System.currentTimeMillis()
                    )
                )
                .await()
        } catch (e: Exception) {
            e.printStackTrace()
        }

    }
}