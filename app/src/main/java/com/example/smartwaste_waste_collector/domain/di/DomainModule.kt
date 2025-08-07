package com.example.smartwaste_waste_collector.domain.di

import com.example.smartwaste_waste_collector.data.models.DailyAssignment
import com.example.smartwaste_waste_collector.data.repoimpl.authrepoimpl.AuthRepositryImpl
import com.example.smartwaste_waste_collector.data.repoimpl.dailyassignrepoimpl.DailyAssignRepositryImpl
import com.example.smartwaste_waste_collector.data.repoimpl.truckrepoimpl.TruckRepositryImpl
import com.example.smartwaste_waste_collector.domain.repo.authrepo.AuthRepositry
import com.example.smartwaste_waste_collector.domain.repo.dailyAssignRepo.DailyAssignRepository
import com.example.smartwaste_waste_collector.domain.repo.trucksrepo.TrucksRepositry
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class DomainModule {

    @Singleton
    @Binds
    abstract fun provideAuthRepo(authRepositryImpl: AuthRepositryImpl): AuthRepositry



    @Singleton
    @Binds
    abstract fun provideDailyAssignRepo(dailyAssignRepositryImpl: DailyAssignRepositryImpl): DailyAssignRepository

    @Singleton
    @Binds
    abstract fun provideTruckRepo(truckRepositryImpl: TruckRepositryImpl): TrucksRepositry

}