package com.example.myapplication.dependency

import android.app.Application
import android.content.Context
import com.example.myapplication.repository.BluetoothGattRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object BluetoothGattModule {

    @Singleton
    @Provides
    fun provideBluetoothController(
        @ApplicationContext context: Context,
        application: Application,
    ) = BluetoothGattRepository(context, application)

}


