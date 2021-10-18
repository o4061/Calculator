package com.userfaltakas.calculator.ui.activity

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.userfaltakas.calculator.R
import com.userfaltakas.calculator.api.Resource
import com.userfaltakas.calculator.constant.Constants.FROM_CURRENCY
import com.userfaltakas.calculator.constant.Constants.TO_CURRENCY
import com.userfaltakas.calculator.databinding.CalculatorBinding
import com.userfaltakas.calculator.databinding.ChangeCurrencyDialogBinding
import com.userfaltakas.calculator.network.NetworkLiveData
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@AndroidEntryPoint
class Calculator : AppCompatActivity() {
    private lateinit var binding: CalculatorBinding
    private val viewModel: CalculatorViewModel by viewModels()
    private lateinit var dialogBinding: ChangeCurrencyDialogBinding
    private lateinit var dialog: Dialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = CalculatorBinding.inflate(layoutInflater)
        setContentView(binding.root)
        NetworkLiveData.init(application)
        clickListeners()
        observers()
    }

    private fun clickListeners() {
        binding.apply {
            num0.setOnClickListener { viewModel.number("0") }
            num1.setOnClickListener { viewModel.number("1") }
            num2.setOnClickListener { viewModel.number("2") }
            num3.setOnClickListener { viewModel.number("3") }
            num4.setOnClickListener { viewModel.number("4") }
            num5.setOnClickListener { viewModel.number("5") }
            num6.setOnClickListener { viewModel.number("6") }
            num7.setOnClickListener { viewModel.number("7") }
            num8.setOnClickListener { viewModel.number("8") }
            num9.setOnClickListener { viewModel.number("9") }
            add.setOnClickListener { viewModel.operation("+") }
            minus.setOnClickListener { viewModel.operation("-") }
            div.setOnClickListener { viewModel.operation("/") }
            times.setOnClickListener { viewModel.operation("*") }
            comma.setOnClickListener { viewModel.decimal() }
            clear.setOnClickListener { viewModel.clearResult() }
            plusMinus.setOnClickListener { viewModel.negativeNumbers() }
            equals.setOnClickListener { viewModel.calculateResult() }
            changeCurrencyBtn.setOnClickListener {
                if (viewModel.canConvert) {
                    dialog.show()
                } else {
                    Toast.makeText(
                        this@Calculator,
                        resources.getString(R.string.currency_availability_message),
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
            currency.setOnClickListener {
                viewModel.conversion()
            }
        }
    }

    private fun getCurrencies() {
        viewModel.getCurrencies()
        viewModel.currencyResponse.observe(this, { response ->
            when (response) {
                is Resource.Loading -> {
                    setCurrencyUnavailable()
                }
                is Resource.Success -> {
                    viewModel.setCurrenciesStateToInitial()
                    setCurrencyAvailable()
                }
                is Resource.Error -> {
                    setCurrencyUnavailable()
                    if (viewModel.canResendRequest()) {
                        CoroutineScope(Dispatchers.Main).launch {
                            resendRequest()
                        }
                    }
                }
            }
        })
    }

    private suspend fun resendRequest() {
        delay(1000)
        getCurrencies()
    }

    private fun setCurrencyUnavailable() {
        binding.currency.setTextColor(
            ContextCompat.getColor(
                this,
                R.color.light_green
            )
        )
    }

    private fun setCurrencyAvailable() {
        viewModel.canConvert = true
        binding.currency.setTextColor(
            ContextCompat.getColor(
                this,
                R.color.green
            )
        )
        setCurrencySettingDialog()
    }

    private fun observers() {
        viewModel._result.observe(this, { value ->
            binding.result.text = value
        })
        viewModel._error.observe(this, { value ->
            Toast.makeText(this, value, Toast.LENGTH_SHORT).show()
        })
        NetworkLiveData.observe(this, { state ->
            if (state && !viewModel.isCurrenciesInitialized()) {
                getCurrencies()
            }
        })
    }

    private fun setCurrencySettingDialog() {
        var fromCurrency = ""
        var toCurrency = ""
        dialog = Dialog(this)
        dialog.apply {
            setContentView(R.layout.change_currency_dialog)
            setCancelable(false)
            window?.setLayout(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
        }
        dialogBinding = ChangeCurrencyDialogBinding.inflate(
            LayoutInflater.from(this)
        )
        dialog.setContentView(dialogBinding.root)
        val currenciesAdapter = ArrayAdapter(
            this,
            R.layout.support_simple_spinner_dropdown_item,
            viewModel.getArrayAdapter()
        )
        dialogBinding.apply {
            spinnerFromCurrency.adapter = currenciesAdapter
            spinnerToCurrency.adapter = currenciesAdapter
            spinnerFromCurrency.setSelection(viewModel.getRatePosition(FROM_CURRENCY))
            spinnerToCurrency.setSelection(viewModel.getRatePosition(TO_CURRENCY))
            spinnerFromCurrency.onItemSelectedListener =
                object : AdapterView.OnItemSelectedListener {
                    override fun onItemSelected(
                        parent: AdapterView<*>?,
                        view: View?,
                        position: Int,
                        id: Long
                    ) {
                        fromCurrency = parent?.getItemAtPosition(position).toString()
                    }

                    override fun onNothingSelected(parent: AdapterView<*>?) {}
                }
            spinnerToCurrency.onItemSelectedListener =
                object : AdapterView.OnItemSelectedListener {
                    override fun onItemSelected(
                        parent: AdapterView<*>?,
                        view: View?,
                        position: Int,
                        id: Long
                    ) {
                        toCurrency = parent?.getItemAtPosition(position).toString()
                    }

                    override fun onNothingSelected(parent: AdapterView<*>?) {}
                }
            okBtn.setOnClickListener {
                viewModel.fromCurrency = fromCurrency
                viewModel.toCurrency = toCurrency
                dialog.hide()
            }
            cancelBtn.setOnClickListener {
                dialog.hide()
            }
        }
    }
}