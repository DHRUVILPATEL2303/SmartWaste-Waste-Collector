package com.example.smartwaste_waste_collector.data.repoimpl.routerepositryimpl

import com.example.smartwaste_waste_collector.common.ROUTE_PATH
import com.example.smartwaste_waste_collector.common.ResultState
import com.example.smartwaste_waste_collector.data.models.RouteModel
import com.example.smartwaste_waste_collector.domain.repo.trucksrepo.RouteRepositry
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import javax.inject.Inject

class RouteRepositryImpl @Inject constructor(
    private val firebaseFirestore: FirebaseFirestore
) : RouteRepositry {
    override suspend fun getAllRoutes(): Flow<ResultState<List<RouteModel>>> = callbackFlow {

        trySend(ResultState.Loading)

        try {
            firebaseFirestore.collection(ROUTE_PATH).addSnapshotListener { snapshot, error ->


                if (error != null) {
                    trySend(ResultState.Error(error.message ?: "Unknown error"))
                    return@addSnapshotListener
                }


                if (snapshot != null && !snapshot.isEmpty) {
                    val routes = snapshot.toObjects(RouteModel::class.java)
                    trySend(ResultState.Success(routes))
                } else {
                    trySend(ResultState.Success(emptyList()))
                }
            }
        } catch (e: Exception) {
            trySend(ResultState.Error(e.message ?: "Unknown error"))
        }


        awaitClose {
            close()
        }
    }



}