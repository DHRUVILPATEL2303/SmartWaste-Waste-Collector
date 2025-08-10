package com.example.smartwaste_waste_collector.domain.usecases.workerfeedbackusecases

import com.example.smartwaste_waste_collector.domain.repo.feedbackrepo.WorkerFeedBackRepositry
import com.google.firebase.firestore.FirebaseFirestore
import javax.inject.Inject

class GetAllFeedBackUseCase @Inject constructor(
    private val workerFeedBackRepositry: WorkerFeedBackRepositry
) {

    suspend fun getAllFeedbacks()=workerFeedBackRepositry.getWorkerFeedBack()
}