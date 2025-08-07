package com.example.smartwaste_waste_collector.data.repoimpl.truckrepoimpl

import com.example.smartwaste_waste_collector.common.ResultState
import com.example.smartwaste_waste_collector.common.TRUCKS_PATH
import com.example.smartwaste_waste_collector.data.models.TruckModel
import com.example.smartwaste_waste_collector.domain.repo.trucksrepo.TrucksRepositry
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import javax.inject.Inject

class TruckRepositryImpl @Inject constructor(
    private val firebaseFirestore: FirebaseFirestore
) : TrucksRepositry {
    override suspend fun getallTrucks(): Flow<ResultState<List<TruckModel>>> = callbackFlow {
        trySend(ResultState.Loading)

        try {
            firebaseFirestore.collection(TRUCKS_PATH).addSnapshotListener { value, error ->

                if (error != null) {
                    trySend(ResultState.Error(error.message ?: "Unknown error occurred"))
                } else {
                    val trucks = value?.mapNotNull{
                        it.toObject<TruckModel>(TruckModel::class.java).copy(
                            id = it.id
                        )
                    } ?: emptyList()
                    trySend(ResultState.Success(trucks))

                }
            }
            awaitClose {
                close()
            }
        } catch (e: Exception) {
            trySend(ResultState.Error(e.message ?: "Unknown error occurred"))
        }

        awaitClose {
            close()
        }
    }
}
