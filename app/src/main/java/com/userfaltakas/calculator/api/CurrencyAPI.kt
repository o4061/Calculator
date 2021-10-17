package com.userfaltakas.calculator.api

import com.userfaltakas.calculator.constant.Constants.API_KEY
import com.userfaltakas.calculator.data.CurrencyResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface CurrencyAPI {
    @GET("latest")
    suspend fun getCurrencies(
        @Query("access_key") apiKey: String = API_KEY
    ): Response<CurrencyResponse>
}