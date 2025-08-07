package com.example.smartwaste_waste_collector.data.repoimpl.dailyassignrepoimpl

import com.example.smartwaste_waste_collector.common.DAILY_ASSIGN_PATH
import com.example.smartwaste_waste_collector.common.ResultState
import com.example.smartwaste_waste_collector.data.models.DailyAssignment
import com.example.smartwaste_waste_collector.domain.repo.dailyAssignRepo.DailyAssignRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class DailyAssignRepositryImpl @Inject constructor(
    private val firebaseFirestore: FirebaseFirestore,
    private val firebaseAuth: FirebaseAuth
) : DailyAssignRepository{
    override suspend fun getTodayAssignment(): ResultState<DailyAssignment?> {

        return try {
          val value=  firebaseFirestore.collection(DAILY_ASSIGN_PATH).document(firebaseAuth.currentUser!!.uid).get().await()

            val assignment = value?.toObject(DailyAssignment::class.java)
            ResultState.Success(assignment)
        } catch (e: Exception) {
            ResultState.Error(e.message ?: "Unknown error occurred")
        }
    }

    override suspend fun submitTodayAssignment(assignment: DailyAssignment): ResultState<Unit> {
        try {
            firebaseFirestore.collection(DAILY_ASSIGN_PATH).document(firebaseAuth.currentUser!!.uid).set(assignment).await()

            return ResultState.Success(Unit)
        } catch (e: Exception) {
            return ResultState.Error(e.message ?: "Unknown error occurred")

        }

    }


}