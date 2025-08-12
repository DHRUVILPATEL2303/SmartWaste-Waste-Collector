package com.example.smartwaste_waste_collector.data.repoimpl.userpointrepoimpl

import com.example.smartwaste_waste_collector.common.ResultState
import com.example.smartwaste_waste_collector.common.USERS_PATH
import com.example.smartwaste_waste_collector.common.USER_POINTS_PATH
import com.example.smartwaste_waste_collector.data.models.UserPointModel
import com.example.smartwaste_waste_collector.domain.repo.userpointrepo.UserPointRepositry
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class UserPointRepositryImpl @Inject constructor(
    private val firebaseFirestore: FirebaseFirestore,
    private val firebaseAuth: FirebaseAuth
) : UserPointRepositry {

    override suspend fun givePointsToUser(userPointModel: UserPointModel): Flow<ResultState<String>> =
        callbackFlow {
            trySend(ResultState.Loading)

            val workerId = firebaseAuth.currentUser?.uid
            val userPointModelWithWorkerId = userPointModel.copy(workerID = workerId.orEmpty())

            try {
                val userDocRef = firebaseFirestore.collection(USERS_PATH).document(userPointModel.userId)
                val snapshot = userDocRef.get().await()

                val currentTotalPointsStr = snapshot.getString("totalPoints") ?: "0"
                val currentTotalPoints = currentTotalPointsStr.toLongOrNull() ?: 0L

                val newPoints = userPointModel.points.toLongOrNull() ?: 0L
                val updatedTotalPoints = (currentTotalPoints + newPoints).toString()

                userDocRef.update("totalPoints", updatedTotalPoints).await()

                firebaseFirestore
                    .collection(USER_POINTS_PATH)
                    .document()
                    .set(userPointModelWithWorkerId)
                    .await()

                trySend(ResultState.Success("Points given successfully"))
            } catch (e: Exception) {
                trySend(ResultState.Error(e.message ?: "Unknown error"))
            }

            awaitClose { close() }
        }
}