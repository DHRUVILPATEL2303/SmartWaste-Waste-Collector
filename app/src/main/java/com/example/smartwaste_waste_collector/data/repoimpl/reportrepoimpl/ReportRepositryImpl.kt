package com.example.smartwaste_waste_collector.data.repoimpl.reportrepoimpl

import android.util.Log
import com.example.smartwaste_waste_collector.common.REPORTS_PATH
import com.example.smartwaste_waste_collector.common.ResultState
import com.example.smartwaste_waste_collector.data.models.ReportModel
import com.example.smartwaste_waste_collector.domain.repo.reportrepo.ReportRepositry
import com.example.smartwaste_waste_collector.presentation.screens.authscreen.LoginScreenUI
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class ReportRepositryImpl @Inject constructor(
    private val firebaseFirestore: FirebaseFirestore,
    private val firebaseAuth: FirebaseAuth
) : ReportRepositry{

    override suspend fun getAllReports(): Flow<ResultState<List<ReportModel>>> = callbackFlow {
        trySend(ResultState.Loading)

        val uid = firebaseAuth.currentUser?.uid
        if (uid == null) {
            trySend(ResultState.Error("User not logged in"))
            close()
            return@callbackFlow
        }

        var driverReports: List<ReportModel> = emptyList()
        var collectorReports: List<ReportModel> = emptyList()

        fun sendMergedResults() {
            val merged = (driverReports + collectorReports).distinctBy { it.reportId }
            trySend(ResultState.Success(merged))
        }

        val driverListener = firebaseFirestore.collection(REPORTS_PATH)
            .whereEqualTo("againstDriverId", uid)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(ResultState.Error(error.message ?: "Unknown error"))
                    return@addSnapshotListener
                }
                driverReports = snapshot?.documents?.mapNotNull {
                    it.toObject(ReportModel::class.java).apply{
                        this?.reportId = it.id

                    }

                } ?: emptyList()
                sendMergedResults()
            }

        val collectorListener = firebaseFirestore.collection(REPORTS_PATH)
            .whereEqualTo("againstCollectorId", uid)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(ResultState.Error(error.message ?: "Unknown error"))
                    return@addSnapshotListener
                }
                collectorReports = snapshot?.documents?.mapNotNull {
                    it.toObject(ReportModel::class.java).apply {
                        this?.reportId = it.id
                    }
                } ?: emptyList()
                sendMergedResults()
            }

        awaitClose {
            driverListener.remove()
            collectorListener.remove()
        }
    }
    override suspend fun updateReport(
        reportId: String,
        status: String,
        attachments: List<String>
    ): ResultState<String> {

        Log.d("updateReport",reportId)
        Log.d("updateReport",status)
        Log.d("updateReport",attachments.toString())



        return try {
            firebaseFirestore.collection(REPORTS_PATH)
                .document(reportId)
                .update(
                    mapOf(
                        "status" to status,
                        "attachments" to attachments
                    )
                )
                .await()

            ResultState.Success("Report updated successfully")
        } catch (e: Exception) {
            ResultState.Error(e.message ?: "Unknown error occurred")
        }
    }
}