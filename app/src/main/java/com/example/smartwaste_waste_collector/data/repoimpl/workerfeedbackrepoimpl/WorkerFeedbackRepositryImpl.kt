package com.example.smartwaste_waste_collector.data.repoimpl.workerfeedbackrepoimpl

import com.example.smartwaste_waste_collector.common.ResultState
import com.example.smartwaste_waste_collector.common.WORKER_FEEDBACK_PATH
import com.example.smartwaste_waste_collector.data.models.WorkerFeedBackModel
import com.example.smartwaste_waste_collector.domain.repo.feedbackrepo.WorkerFeedBackRepositry
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class WorkerFeedbackRepositryImpl @Inject constructor(
    private val firebaseFirestore: FirebaseFirestore,
    private val firebaseAuth: FirebaseAuth
) : WorkerFeedBackRepositry {

    override suspend fun getWorkerFeedBack(): Flow<ResultState<List<WorkerFeedBackModel>>> = flow {
        emit(ResultState.Loading)

        try {
            val currentUserId = firebaseAuth.currentUser?.uid ?: throw Exception("User not logged in")

            val driverResults = firebaseFirestore.collection(WORKER_FEEDBACK_PATH)
                .whereEqualTo("driverId", currentUserId)
                .get()
                .await()
                .toObjects(WorkerFeedBackModel::class.java)


            val collectorResults = firebaseFirestore.collection(WORKER_FEEDBACK_PATH)
                .whereEqualTo("collectorId", currentUserId)
                .get()
                .await()
                .toObjects(WorkerFeedBackModel::class.java)

            val allResults = (driverResults + collectorResults).distinctBy { it.feedbackId }

            emit(ResultState.Success(allResults))
        } catch (e: Exception) {
            emit(ResultState.Error(e.message ?: "Unknown error"))
        }
    }
}