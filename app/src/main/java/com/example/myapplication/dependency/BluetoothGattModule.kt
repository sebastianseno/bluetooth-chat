package com.example.myapplication.dependency

import android.app.Application
import android.content.Context
import com.example.myapplication.bluetooth.BluetoothGattController
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import javax.inject.Qualifier
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object BluetoothGattModule {
    @Retention(AnnotationRetention.BINARY)
    @Qualifier
    annotation class ApplicationScope

    @ApplicationScope
    @Provides
    @Singleton
    fun providesApplicationScope() = CoroutineScope(SupervisorJob())

    @Singleton
    @Provides
    fun provideBluetoothController(
        @ApplicationContext context: Context,
        application: Application,
        @ApplicationScope coroutineScope: CoroutineScope
    ) = BluetoothGattController(context, application, coroutineScope)

}


