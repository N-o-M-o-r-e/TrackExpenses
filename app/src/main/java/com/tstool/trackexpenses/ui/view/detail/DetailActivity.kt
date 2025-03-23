package com.tstool.trackexpenses.ui.view.detail

import android.content.Intent
import android.util.Log
import android.view.View
import androidx.core.net.toUri
import androidx.lifecycle.lifecycleScope
import com.tstool.trackexpenses.data.room.entity.ExpenseEntity
import com.tstool.trackexpenses.data.room.entity.IncomeEntity
import com.tstool.trackexpenses.databinding.ActivityDetailBinding
import com.tstool.trackexpenses.ui.view.create.expenses.CreateExpensesActivity
import com.tstool.trackexpenses.ui.view.create.expenses.CreateExpensesActivity.Companion.KEY_EXPENSES_EDIT
import com.tstool.trackexpenses.ui.view.create.expenses.CreateExpensesActivity.Companion.KEY_RETURN_DATA
import com.tstool.trackexpenses.ui.view.create.income.CreateIncomeActivity
import com.tstool.trackexpenses.ui.view.home.fragment.expenses.ExpensesFragment.Companion.KEY_EXPENSE
import com.tstool.trackexpenses.ui.view.home.fragment.income.IncomeFragment
import com.tstool.trackexpenses.ui.view.viewmodel.ExpenseEvent
import com.tstool.trackexpenses.ui.view.viewmodel.ExpenseUiAction
import com.tstool.trackexpenses.ui.view.viewmodel.ExpenseViewModel
import com.tstool.trackexpenses.ui.view.viewmodel.IncomeEvent
import com.tstool.trackexpenses.ui.view.viewmodel.IncomeUiAction
import com.tstool.trackexpenses.ui.view.viewmodel.IncomeViewModel
import com.tstool.trackexpenses.utils.base.BaseActivity
import com.tstool.trackexpenses.utils.ktx.getResourceByTagEx
import com.tstool.trackexpenses.utils.ktx.getResourceByTagIc
import com.tstool.trackexpenses.utils.ktx.loadImageWithGlide
import com.tstool.trackexpenses.utils.ktx.toCurrencyString
import com.tstool.trackexpenses.utils.ktx.toFormattedDateTime
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel

class DetailActivity : BaseActivity<ActivityDetailBinding>(ActivityDetailBinding::inflate) {

    private val viewModelEx: ExpenseViewModel by viewModel()
    private val viewModelIc: IncomeViewModel by viewModel()

    private var expense: ExpenseEntity? = null
    private var income: IncomeEntity? = null

    private var tag = Int

    override fun initAds() {}

    override fun initViewModel() {
        lifecycleScope.launch {
            viewModelEx.uiState.collect { event ->
                if (event.isLoading){
                    toastMessage("Loading....")
                }else{
                    toastMessage("Success")
                }
            }
        }

        lifecycleScope.launch {
            viewModelIc.uiState.collect { event ->
                if (event.isLoading){
                    toastMessage("Loading....")
                }else{
                    toastMessage("Success")
                }
            }
        }

        lifecycleScope.launch {
            viewModelEx.eventFlow.collect { event ->
                when (event) {
                    is ExpenseEvent.ExpenseUpdated ->{
                        onBackPressed()
                    }
                    is ExpenseEvent.ExpenseDeleted -> {
                        onBackPressed()
                    }
                    else -> {}
                }
            }
        }

        lifecycleScope.launch {
            viewModelIc.eventFlow.collect{event->
                when (event) {
                    is IncomeEvent.IncomeUpdated -> {
                        onBackPressed()
                    }

                    is IncomeEvent.IncomeDeleted -> {

                    }

                    else -> {}
                }
            }
        }
    }

    override fun initData() {
        expense = getIntentData(KEY_EXPENSE, null)
        expense?.let { exp ->
            binding.titleDetail.text = "Expenses"

            binding.tvExpenses.text = exp.itemName
            binding.tvDescription.text = exp.note
            binding.tvMoney.text = exp.price.toCurrencyString()
            binding.tvCalendar.text = exp.date.toFormattedDateTime()

            Log.e("__EX", "initViewModel: ${exp.imageUri}")

            if (exp.imageUri == null) {
                binding.imgExpenses.visibility = View.GONE
            } else {
                binding.imgExpenses.visibility = View.VISIBLE
                binding.imgExpenses.setImageURI(exp.imageUri.toUri())

            }

            loadImageWithGlide(
                imageView = binding.imgTag,
                resource = getResourceByTagEx(exp.category)
            )
        }

        income = getIntentData(IncomeFragment.KEY_INCOME, null)
        income?.let {
            binding.titleDetail.text = "Incomes"

            binding.tvExpenses.text = it.sourceName
            binding.tvDescription.text = it.note
            binding.tvMoney.text = it.amount.toCurrencyString()
            binding.tvCalendar.text = it.date.toFormattedDateTime()

            Log.e("__EX", "initViewModel: ${it.imageUri}")

            if (it.imageUri == null) {
                binding.imgExpenses.visibility = View.GONE
            } else {
                binding.imgExpenses.visibility = View.VISIBLE
                binding.imgExpenses.setImageURI(it.imageUri.toUri())

            }

            loadImageWithGlide(
                imageView = binding.imgTag,
                resource = getResourceByTagIc(it.category)
            )
        }
    }

    override fun initView() {

    }

    override fun initAction() {
        binding.btnBack.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        binding.btnEdit.setOnClickListener {
            expense?.let {
                goToNewActivityForResult(
                    activity = CreateExpensesActivity::class.java,
                    key = KEY_EXPENSES_EDIT,
                    data = it
                )
                Log.d("__DATA", "expense: $it: ")
            }
            income?.let {
                goToNewActivityForResult(
                    activity = CreateIncomeActivity::class.java,
                    key = CreateIncomeActivity.KEY_INCOME_EDIT,
                    data = it
                )
                Log.d("__DATA", "income: $it: ")
            }

        }

        binding.btnDelete.setOnClickListener {
            expense?.let {
                viewModelEx.dispatch(ExpenseUiAction.Delete(it))
                onBackPressed()
            }
            income?.let {
                viewModelIc.dispatch(IncomeUiAction.Delete(it))
                onBackPressed()
            }
        }
    }

    override fun onActivityResultReceived(data: Intent?) {
        val message = data?.getBooleanExtra(KEY_RETURN_DATA, false)
        if (message == true) {
            onBackPressed()
        }

    }

}