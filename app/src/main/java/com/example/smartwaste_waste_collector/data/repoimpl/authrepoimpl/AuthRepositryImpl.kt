package com.example.smartwaste_waste_collector.data.repoimpl.authrepoimpl

import com.example.smartwaste_waste_collector.common.ResultState
import com.example.smartwaste_waste_collector.common.WORKER_PATH
import com.example.smartwaste_waste_collector.data.models.WorkerModel
import com.example.smartwaste_waste_collector.domain.repo.authrepo.AuthRepositry
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class AuthRepositryImpl @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val firebaseAuth: FirebaseAuth
) : AuthRepositry{
    override suspend fun loginWithEmailAndPassword(
        email: String,
        password: String
    ): ResultState<FirebaseUser> {
       return try {
            firebaseAuth.signInWithEmailAndPassword(email, password).await()
            val user = firebaseAuth.currentUser
             if (user != null) {
                ResultState.Success(user)
            } else {
                ResultState.Error("User not found")
            }
        } catch (e: Exception) {
             ResultState.Error(e.message ?: "An error occurred")
        }

    }

    override suspend fun createAccount(
        model: WorkerModel,
        password: String
    ): ResultState<FirebaseUser> {
        return try {
            firebaseAuth.createUserWithEmailAndPassword(model.email, password).await()

            val user = firebaseAuth.currentUser

            firestore.collection(WORKER_PATH).document(firebaseAuth.currentUser!!.uid).set(model).await()

            ResultState.Success(user!!)

        }catch (e: Exception){
            ResultState.Error(e.message ?: "An error occurred")

        }
    }
}