package com.example.nkosana_maluleke_restuarant_discovery.di

import com.example.nkosana_maluleke_restuarant_discovery.data.remote.MockRestaurantRemoteDataSource
import com.example.nkosana_maluleke_restuarant_discovery.data.remote.RestaurantRemoteDataSource
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Qualifier
import javax.inject.Singleton
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.serialization.json.Json

/** Qualifies the dispatcher used for disk and parsing work. */
@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class IoDispatcher

/** Binds data-layer abstractions to their implementations. */
@Module
@InstallIn(SingletonComponent::class)
abstract class DataBindingsModule {

    /**
     * Binds the remote data source contract to the mock implementation.
     *
     * @param impl mock remote source; swap this binding for a real API client in production.
     */
    @Binds
    @Singleton
    abstract fun bindRestaurantRemoteDataSource(
        impl: MockRestaurantRemoteDataSource,
    ): RestaurantRemoteDataSource
}

/** Provides third-party and platform dependencies used by the data layer. */
@Module
@InstallIn(SingletonComponent::class)
object DataProvidersModule {

    /** Provides a lenient JSON configuration that tolerates missing or unknown fields. */
    @Provides
    @Singleton
    fun provideJson(): Json = Json {
        ignoreUnknownKeys = true
        explicitNulls = false
    }

    /** Provides the IO dispatcher so tests can replace it. */
    @Provides
    @IoDispatcher
    fun provideIoDispatcher(): CoroutineDispatcher = Dispatchers.IO
}
