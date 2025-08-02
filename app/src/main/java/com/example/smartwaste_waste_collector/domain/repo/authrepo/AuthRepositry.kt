package com.example.smartwaste_waste_collector.domain.repo.authrepo

import com.example.smartwaste_waste_collector.common.ResultState
import com.example.smartwaste_waste_collector.data.models.WorkerModel
import com.google.firebase.auth.FirebaseUser

interface AuthRepositry {

    suspend fun loginWithEmailAndPassword(email: String, password: String): ResultState<FirebaseUser>

    suspend fun createAccount(model : WorkerModel,password: String) : ResultState<FirebaseUser>
}