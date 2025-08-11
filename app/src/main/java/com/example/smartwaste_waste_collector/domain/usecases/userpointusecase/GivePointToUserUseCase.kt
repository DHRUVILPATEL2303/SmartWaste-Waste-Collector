package com.example.smartwaste_waste_collector.domain.usecases.userpointusecase

import com.example.smartwaste_waste_collector.data.models.UserPointModel
import com.example.smartwaste_waste_collector.domain.repo.userpointrepo.UserPointRepositry
import javax.inject.Inject

class GivePointToUserUseCase @Inject constructor(
    private val userPointRepositry: UserPointRepositry
) {


    suspend fun givePointsToUser(userPointModel: UserPointModel)=userPointRepositry.givePointsToUser(userPointModel)
}