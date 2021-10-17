package com.userfaltakas.calculator.di

import com.userfaltakas.calculator.api.CurrencyAPI
import com.userfaltakas.calculator.constant.Constants.BASE_URL
import com.userfaltakas.calculator.repository.CurrencyRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Singleton
    @Provides
    fun provideCurrencyAPI(): CurrencyAPI = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .addConverterFactory(GsonConverterFactory.create())
        .build()
        .create(CurrencyAPI::class.java)

    @Singleton
    @Provides
    fun provideCurrencyRepository(currencyAPI: CurrencyAPI) =
        CurrencyRepository(currencyAPI)
}