package com.example.smartwaste_waste_collector.data.repoimpl.routeprogressrepoimpl

import android.util.Log
import com.example.smartwaste_waste_collector.common.ROUTE_PROGRESS_MODEL
import com.example.smartwaste_waste_collector.common.ResultState
import com.example.smartwaste_waste_collector.data.models.RouteProgressModel
import com.example.smartwaste_waste_collector.domain.repo.RouteProgressRepo
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.Filter
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import javax.inject.Inject

class RouteProgressRepositoryImpl @Inject constructor(
    private val firebaseFirestore: FirebaseFirestore,
    private val firebaseAuth: FirebaseAuth
) : RouteProgressRepo {




    override fun getTodayRouteProgress(): Flow<ResultState<RouteProgressModel?>> = callbackFlow {
        trySend(ResultState.Loading)

        val currentUserId = firebaseAuth.currentUser?.uid
        if (currentUserId.isNullOrEmpty()) {
            trySend(ResultState.Success(null))
            close()
            return@callbackFlow
        }

        val todayDate = LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE)


        val query = firebaseFirestore.collection(ROUTE_PROGRESS_MODEL)
            .whereEqualTo("date", todayDate)
            .where(
                Filter.or(
                    Filter.equalTo("assignedDriverId", currentUserId),
                    Filter.equalTo("assignedCollectorId", currentUserId)
                )
            )
            .limit(1)

        val listener = query.addSnapshotListener { snapshot, error ->
            if (error != null) {
                trySend(ResultState.Error(error.message ?: "An unknown error occurred."))
                close()
                return@addSnapshotListener
            }

            if (snapshot != null && !snapshot.isEmpty) {
                val progress = snapshot.documents.first().toObject(RouteProgressModel::class.java)
                trySend(ResultState.Success(progress))
            } else {

                trySend(ResultState.Success(null))
            }
        }


        awaitClose {
            Log.d("RouteProgressRepo", "Closing today's route progress listener.")
            listener.remove()
        }
    }

    override suspend fun updateAreaCompletionStatus(
        documentId: String,
        areaId: String,
        isCompleted: Boolean
    ): ResultState<Unit> {
        return try {
            val docRef = firebaseFirestore.collection(ROUTE_PROGRESS_MODEL).document(documentId)
            val snapshot = docRef.get().await()
            val routeProgress = snapshot.toObject(RouteProgressModel::class.java)

            if (routeProgress != null) {
                val updatedAreas = routeProgress.areaProgress.map { area ->
                    if (area.areaId == areaId) {
                        area.copy(
                            isCompleted = isCompleted,
                            completedAt = if (isCompleted) System.currentTimeMillis() else null
                        )
                    } else {
                        area
                    }
                }

                docRef.update("areaProgress", updatedAreas).await()

                if (updatedAreas.all { it.isCompleted }) {
                    docRef.update("isRouteCompleted", true).await()
                } else {
                    docRef.update("isRouteCompleted", false).await()
                }

                ResultState.Success(Unit)
            } else {
                ResultState.Error("Route progress not found")
            }
        } catch (e: Exception) {
            ResultState.Error(e.message ?: "Error updating area status")
        }
    }

    override suspend fun markRouteCompleted(documentId: String): ResultState<Unit> {
        return try {
            firebaseFirestore.collection(ROUTE_PROGRESS_MODEL)
                .document(documentId)
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
            if (snapshot.exists()) {
                val updates = mutableMapOf<String, Any>()
                if (newRouteProgress.assignedDriverId.isNotEmpty()) {
                    updates["assignedDriverId"] = newRouteProgress.assignedDriverId
                }
                if (newRouteProgress.assignedCollectorId.isNotEmpty()) {
                    updates["assignedCollectorId"] = newRouteProgress.assignedCollectorId
                }
                if (newRouteProgress.assignedTruckId.isNotEmpty()) {
                    updates["assignedTruckId"] = newRouteProgress.assignedTruckId
                }
                docRef.update(updates).await()
            } else {
                docRef.set(newRouteProgress).await()
            }
            ResultState.Success(Unit)
        } catch (e: Exception) {
            ResultState.Error(e.message ?: "Error creating route progress")
        }
    }
}