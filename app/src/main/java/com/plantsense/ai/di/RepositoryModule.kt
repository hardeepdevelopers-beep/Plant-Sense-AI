package com.plantsense.ai.di

import com.plantsense.ai.data.repository.PlantRepositoryImpl
import com.plantsense.ai.data.repository.SettingsRepositoryImpl
import com.plantsense.ai.domain.repository.PlantRepository
import com.plantsense.ai.domain.repository.SettingsRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindPlantRepository(
        plantRepositoryImpl: PlantRepositoryImpl
    ): PlantRepository

    @Binds
    @Singleton
    abstract fun bindSettingsRepository(
        settingsRepositoryImpl: SettingsRepositoryImpl
    ): SettingsRepository

    @Binds
    @Singleton
    abstract fun bindImageStorage(
        imageStorageImpl: com.plantsense.ai.data.local.ImageStorageImpl
    ): com.plantsense.ai.domain.repository.ImageStorage
}
