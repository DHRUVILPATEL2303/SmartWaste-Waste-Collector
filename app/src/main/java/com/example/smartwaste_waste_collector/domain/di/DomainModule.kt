package com.example.smartwaste_waste_collector.domain.di

import com.example.smartwaste_waste_collector.data.models.DailyAssignment
import com.example.smartwaste_waste_collector.data.models.WorkerRole
import com.example.smartwaste_waste_collector.data.repoimpl.authrepoimpl.AuthRepositryImpl
import com.example.smartwaste_waste_collector.data.repoimpl.dailyassignrepoimpl.DailyAssignRepositryImpl
import com.example.smartwaste_waste_collector.data.repoimpl.locationrepoimpl.LocationRepositryImpl
import com.example.smartwaste_waste_collector.data.repoimpl.reportrepoimpl.ReportRepositryImpl
import com.example.smartwaste_waste_collector.data.repoimpl.routeprogressrepoimpl.RouteProgressRepositoryImpl
import com.example.smartwaste_waste_collector.data.repoimpl.routerepositryimpl.RouteRepositryImpl
import com.example.smartwaste_waste_collector.data.repoimpl.truckrepoimpl.TruckRepositryImpl
import com.example.smartwaste_waste_collector.data.repoimpl.userpointrepoimpl.UserPointRepositryImpl
import com.example.smartwaste_waste_collector.data.repoimpl.workerfeedbackrepoimpl.WorkerFeedbackRepositryImpl
import com.example.smartwaste_waste_collector.domain.repo.RouteProgressRepo
import com.example.smartwaste_waste_collector.domain.repo.authrepo.AuthRepositry
import com.example.smartwaste_waste_collector.domain.repo.dailyAssignRepo.DailyAssignRepository
import com.example.smartwaste_waste_collector.domain.repo.feedbackrepo.WorkerFeedBackRepositry
import com.example.smartwaste_waste_collector.domain.repo.locationrepo.LocationRepositry
import com.example.smartwaste_waste_collector.domain.repo.reportrepo.ReportRepositry
import com.example.smartwaste_waste_collector.domain.repo.trucksrepo.RouteRepositry
import com.example.smartwaste_waste_collector.domain.repo.trucksrepo.TrucksRepositry
import com.example.smartwaste_waste_collector.domain.repo.userpointrepo.UserPointRepositry
import dagger.Binds
import dagger.BindsInstance
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


    @Singleton
    @Binds
    abstract fun provideRouteAssignRepo(RepositryImpl: RouteProgressRepositoryImpl): RouteProgressRepo

    @Singleton
    @Binds
    abstract fun provideRoutesRepo(RepositryImpl: RouteRepositryImpl): RouteRepositry


    @Singleton
    @Binds
    abstract fun provideWorkerFeedbackRepo(RepositryImpl: WorkerFeedbackRepositryImpl): WorkerFeedBackRepositry


    @Singleton
    @Binds
    abstract fun provideUserPointRepositry(RepositryImpl: UserPointRepositryImpl): UserPointRepositry


    @Singleton
    @Binds
    abstract fun provideReportRepositry(RepositryImpl: ReportRepositryImpl): ReportRepositry


    @Singleton
    @Binds
    abstract fun provideLocationRepositry(repositryImpl: LocationRepositryImpl): LocationRepositry
}