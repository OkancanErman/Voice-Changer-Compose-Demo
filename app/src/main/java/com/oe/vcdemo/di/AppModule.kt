package com.oe.vcdemo.di

import android.app.Application
import com.oe.vcdemo.repository.RecordRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideRecordRepository(app: Application): RecordRepository {
        return RecordRepository(app)
    }

}