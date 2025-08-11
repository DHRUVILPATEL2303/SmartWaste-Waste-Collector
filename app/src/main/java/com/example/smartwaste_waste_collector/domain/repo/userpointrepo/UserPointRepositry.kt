package com.example.smartwaste_waste_collector.domain.repo.userpointrepo

import com.example.smartwaste_waste_collector.common.ResultState
import com.example.smartwaste_waste_collector.data.models.UserPointModel
import kotlinx.coroutines.flow.Flow

interface UserPointRepositry {

    suspend fun givePointsToUser(userPointModel: UserPointModel) : Flow<ResultState<String>>


}