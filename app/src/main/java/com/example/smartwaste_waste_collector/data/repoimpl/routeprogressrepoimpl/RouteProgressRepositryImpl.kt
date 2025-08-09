package com.example.smartwaste_waste_collector.data.repoimpl.routeprogressrepoimpl

import com.example.smartwaste_waste_collector.common.ROUTE_PROGRESS_MODEL
import com.example.smartwaste_waste_collector.common.ResultState
import com.example.smartwaste_waste_collector.data.models.RouteProgressModel
import com.example.smartwaste_waste_collector.domain.repo.RouteProgressRepo
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class RouteProgressRepositoryImpl @Inject constructor(
    private val firebaseFirestore: FirebaseFirestore,
    private val firebaseAuth: FirebaseAuth
) : RouteProgressRepo {



    override fun getTodayRouteProgress(): Flow<ResultState<RouteProgressModel?>> = callbackFlow {
        trySend(ResultState.Loading)

        val currentUserId = firebaseAuth.currentUser!!.uid

        val collection = firebaseFirestore.collection(ROUTE_PROGRESS_MODEL)

        val listener1 = collection
            .whereEqualTo("assignedCollectorId", currentUserId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(ResultState.Error(error.message ?: "Unknown error"))
                    return@addSnapshotListener
                }
                if (snapshot != null && !snapshot.isEmpty) {
                    val progress = snapshot.documents[0].toObject(RouteProgressModel::class.java)
                    trySend(ResultState.Success(progress))
                } else {

                }
            }


        val listener2 = collection
            .whereEqualTo("assignedDriverId", currentUserId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(ResultState.Error(error.message ?: "Unknown error"))
                    return@addSnapshotListener
                }
                if (snapshot != null && !snapshot.isEmpty) {
                    val progress = snapshot.documents[0].toObject(RouteProgressModel::class.java)
                    trySend(ResultState.Success(progress))
                } else {
            }
            }

        awaitClose {
            listener1.remove()
            listener2.remove()
        }
    }

    override suspend fun updateAreaCompletionStatus(
        routeId: String,
        areaId: String,
        isCompleted: Boolean
    ): ResultState<Unit> {
        return try {
            val docRef = firebaseFirestore.collection(ROUTE_PROGRESS_MODEL).document(routeId)
            val snapshot = docRef.get().await()
            val routeProgress = snapshot.toObject(RouteProgressModel::class.java)

            if (routeProgress != null) {
                val updatedAreas = routeProgress.areaProgress.map { area ->
                    if (area.areaId == areaId) {
                        area.copy(
                            isCompleted = isCompleted,
                            completedAt = if (isCompleted) System.currentTimeMillis() else null
                        )
                    } else area
                }
                docRef.update("areaProgress", updatedAreas).await()
            }

            ResultState.Success(Unit)
        } catch (e: Exception) {
            ResultState.Error(e.message ?: "Error updating area status")
        }
    }

    override suspend fun markRouteCompleted(routeId: String): ResultState<Unit> {
        return try {
            firebaseFirestore.collection(ROUTE_PROGRESS_MODEL)
                .document(routeId)
                .update("isRouteCompleted", true)
                .await()
            ResultState.Success(Unit)
        } catch (e: Exception) {
            ResultState.Error(e.message ?: "Error marking route completed")
        }
    }

    override suspend fun createAndSubmitRouteProgress(
        newRouteProgress: RouteProgressModel
    ): ResultState<Unit> {
        return try {
            val docRef = firebaseFirestore.collection(ROUTE_PROGRESS_MODEL).document(newRouteProgress.routeId)


            val snapshot = docRef.get().await()
            val existing = snapshot.toObject(RouteProgressModel::class.java)


            val merged = RouteProgressModel(
                routeId = newRouteProgress.routeId.ifEmpty { existing?.routeId ?: "" },
                assignedTruckId = newRouteProgress.assignedTruckId.ifEmpty { existing?.assignedTruckId ?: "" },
                assignedDriverId = newRouteProgress.assignedDriverId.ifEmpty { existing?.assignedDriverId ?: "" },
                assignedCollectorId = newRouteProgress.assignedCollectorId.ifEmpty { existing?.assignedCollectorId ?: "" },
                date = newRouteProgress.date.ifEmpty { existing?.date ?: "" },
                areaProgress = if (newRouteProgress.areaProgress.isNotEmpty()) newRouteProgress.areaProgress else (existing?.areaProgress ?: emptyList()),
                isRouteCompleted = newRouteProgress.isRouteCompleted ?: existing?.isRouteCompleted ?: false

            )


            docRef.set(merged).await()
            ResultState.Success(Unit)
        } catch (e: Exception) {
            ResultState.Error(e.message ?: "Error creating route progress")
        }
    }
}