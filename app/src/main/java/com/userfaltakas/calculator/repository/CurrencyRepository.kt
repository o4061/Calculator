package com.userfaltakas.calculator.repository

import com.userfaltakas.calculator.api.CurrencyAPI
import com.userfaltakas.calculator.api.Resource
import com.userfaltakas.calculator.data.CurrencyResponse
import javax.inject.Inject

class CurrencyRepository @Inject constructor(
    private val currencyApi: CurrencyAPI
) {
    suspend fun getCurrencies(): Resource<CurrencyResponse> {
        val response = currencyApi.getCurrencies()
        if (response.isSuccessful) {
            response.body()?.let {
                return Resource.Success(it)
            }
        }
        return Resource.Error(response.message())
    }
}