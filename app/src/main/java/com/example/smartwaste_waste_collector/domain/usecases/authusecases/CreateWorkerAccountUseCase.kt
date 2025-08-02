package com.example.smartwaste_waste_collector.domain.usecases.authusecases

import com.example.smartwaste_waste_collector.data.models.WorkerModel
import com.example.smartwaste_waste_collector.domain.repo.authrepo.AuthRepositry
import javax.inject.Inject

class CreateWorkerAccountUseCase @Inject constructor(
    private val authRepositry: AuthRepositry
) {
    suspend fun createAccount(model: WorkerModel,password: String)=authRepositry.createAccount(model,password)
}