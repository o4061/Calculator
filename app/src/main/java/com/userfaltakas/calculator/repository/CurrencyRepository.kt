package com.userfaltakas.calculator.repository

import com.userfaltakas.calculator.api.CurrencyAPI
import com.userfaltakas.calculator.api.Resource
import com.userfaltakas.calculator.data.CurrencyResponse
import javax.inject.Inject

class CurrencyRepository @Inject constructor(
    private val currencyApi: CurrencyAPI
) {
    suspend fun getCurrencies(): Resource<CurrencyResponse> {
        return try {
            val response = currencyApi.getCurrencies()
            if (response.isSuccessful) {
                response.body()?.let {
                    return@let Resource.Success(it)
                } ?: Resource.Error(response.message())
            } else {
                Resource.Error(response.message())
            }
        } catch (e: Exception) {
            Resource.Error(e.message.toString())
        }
    }
}