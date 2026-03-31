package com.verihubs.nfcreader.utils

import com.verihubs.nfcreader.data.api.VerihubsApiClient
import com.verihubs.nfcreader.data.api.VerihubsAuthInterceptor
import com.verihubs.nfcreader.data.repository.VerihubsRepository
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
    fun provideAuthInterceptor(): VerihubsAuthInterceptor = VerihubsAuthInterceptor()

    @Provides
    @Singleton
    fun provideApiClient(authInterceptor: VerihubsAuthInterceptor): VerihubsApiClient =
        VerihubsApiClient(authInterceptor)

    @Provides
    @Singleton
    fun provideRepository(apiClient: VerihubsApiClient): VerihubsRepository =
        VerihubsRepository(apiClient)
}
