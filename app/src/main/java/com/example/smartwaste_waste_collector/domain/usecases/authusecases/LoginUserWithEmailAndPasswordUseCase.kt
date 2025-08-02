package com.example.smartwaste_waste_collector.domain.usecases.authusecases

import com.example.smartwaste_waste_collector.domain.repo.authrepo.AuthRepositry
import javax.inject.Inject

class LoginUserWithEmailAndPasswordUseCase @Inject constructor(
    private val authRepositry: AuthRepositry
) {

    suspend fun loginWithEmailAndPassword(email: String, password: String) =
        authRepositry.loginWithEmailAndPassword(email, password)

}