package com.userfaltakas.calculator.ui.activity

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.userfaltakas.calculator.api.Resource
import com.userfaltakas.calculator.constant.Constants.FROM_CURRENCY
import com.userfaltakas.calculator.constant.Constants.TO_CURRENCY
import com.userfaltakas.calculator.data.CurrencyResponse
import com.userfaltakas.calculator.repository.CurrencyRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.mariuszgromada.math.mxparser.Expression
import java.text.DecimalFormat
import javax.inject.Inject

@HiltViewModel
class CalculatorViewModel @Inject constructor(
    private val currencyRepository: CurrencyRepository
) : ViewModel() {
    val currencyResponse = MutableLiveData<Resource<CurrencyResponse>>()
    private var isCurrenciesInitialized = false
    private var canAddOperation = false
    private var canAddDecimal = true
    private var canAddNegative = true
    private var isNegative = false
    private var result = ""
    private var currenciesArray = ArrayList<String>()
    var canConvert = false
    var fromCurrency = FROM_CURRENCY
    var toCurrency = TO_CURRENCY
    var _result = MutableLiveData<String>()
    var _error = MutableLiveData<String>()

    fun getCurrencies() = viewModelScope.launch {
        delay(500)
        currencyResponse.postValue(Resource.Loading())
        val response = currencyRepository.getCurrencies()
        currencyResponse.postValue(response)
    }

    fun setCurrenciesStateToInitial() {
        isCurrenciesInitialized = true
    }

    fun isCurrenciesInitialized() = isCurrenciesInitialized

    private fun isExpression(): Boolean {
        return result.contains(" * ") ||
                result.contains(" / ") ||
                result.contains(" + ") ||
                result.contains(" - ")
    }

    fun conversion() {
        val fromCurrencyValue = currencyResponse.value?.data?.rates?.get(fromCurrency)
        val toCurrencyValue = currencyResponse.value?.data?.rates?.get(toCurrency)
        if (fromCurrencyValue != null && toCurrencyValue != null && result.isNotEmpty() && !isExpression()) {
            val number = (result.toDouble() * (1 / fromCurrencyValue) * toCurrencyValue)
            result = String.format("%.3f", number).replace(",", ".")
            _result.postValue(result)
        }
    }

    fun getRatePosition(rate: String) =
        currenciesArray.indexOf(rate)

    fun getArrayAdapter(): ArrayList<String> {
        currencyResponse.value?.data?.rates?.forEach {
            currenciesArray.add(it.key)
        }
        return currenciesArray
    }

    fun clearResult() {
        canAddOperation = false
        canAddDecimal = true
        canAddNegative = true
        isNegative = false
        result = ""
        _result.postValue(result)
    }

    fun number(num: String) {
        canAddOperation = true
        canAddNegative = false
        result = result.plus(num)
        _result.postValue(result)
    }

    fun negativeNumbers() {
        if (isNegative && canAddNegative) {
            val length = result.length
            isNegative = false
            canAddNegative = true
            result = result.subSequence(0, length - 2) as String
            _result.postValue(result)
        } else if (!isNegative && canAddNegative) {
            isNegative = true
            canAddNegative = true
            result = result.plus("(-")
            _result.postValue(result)
        }
    }

    fun operation(operator: String) {
        if (canAddOperation) {
            if (isNegative) {
                isNegative = false
                result = result.plus(")")
                _result.postValue(result)
            }
            canAddOperation = false
            canAddDecimal = true
            canAddNegative = true
            result = result.plus(" $operator ")
            _result.postValue(result)
        }
    }

    fun decimal() {
        if (canAddOperation && canAddDecimal) {
            canAddDecimal = false
            result = result.plus(".")
            _result.postValue(result)
        }
    }

    fun calculateResult() {
        if (isNegative) {
            canAddOperation = true
            result = result.plus(")")
            _result.postValue(result)
        }
        if (canAddOperation && isExpression()) {
            showResult()
        }
        canAddDecimal = false
        canAddNegative = false
        isNegative = false
    }

    private fun showResult() {
        try {
            val expression = Expression(result).calculate()
            if (expression.isNaN()) {
                _error.postValue("Error!")
            } else {
                result = DecimalFormat("0.######").format(expression).replace(",", ".")
                _result.postValue(result)
            }
        } catch (e: Exception) {
            _error.postValue("Error!")
        }
    }
}