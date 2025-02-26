package com.tstool.trackexpenses.ui.view.home.fragment.expenses

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.core.widget.addTextChangedListener
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.tstool.trackexpenses.data.room.entity.ExpenseEntity
import com.tstool.trackexpenses.databinding.FragmentExpensesBinding
import com.tstool.trackexpenses.ui.view.home.fragment.DayExpenses
import com.tstool.trackexpenses.ui.view.home.fragment.ExpensesAdapter
import com.tstool.trackexpenses.ui.view.viewmodel.ExpenseAction
import com.tstool.trackexpenses.ui.view.viewmodel.ExpenseEvent
import com.tstool.trackexpenses.ui.view.viewmodel.ExpenseViewModel
import com.tstool.trackexpenses.utils.base.BaseFragment
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.text.DecimalFormat
import java.util.Calendar

class ExpensesFragment(private val expenseViewModel: ExpenseViewModel) :
    BaseFragment<FragmentExpensesBinding>(FragmentExpensesBinding::inflate) {

    private val expensesAdapter: ExpensesAdapter by lazy { ExpensesAdapter() }

    companion object {
        fun newInstance(viewModel: ExpenseViewModel) = ExpensesFragment(viewModel)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView()
        initViewModel()
        initAction()
    }

    override fun initAds() {}

    override fun initViewModel() {
        lifecycleScope.launch {
            expenseViewModel.uiStateFlow.collect { state ->
                updateTotalCost(state.expenses)
                updateExpensesByDay(state.expenses)
            }
        }

        lifecycleScope.launch {
            expenseViewModel.eventFlow.collect { event ->
                when (event) {
                    is ExpenseEvent.ShowPrices -> {
                        Log.d("ExpensesFragment", "Prices for ${event.category}: ${event.prices}")
                    }
                    is ExpenseEvent.ShowSuccess -> {
                        Log.d("ExpensesFragment", "Success: ${event.message}")
                    }
                    is ExpenseEvent.ShowError -> {
                        Log.e("ExpensesFragment", "Error: ${event.message}")
                    }
                }
            }
        }
    }

    override fun initData() {}

    override fun initView() {}

    override fun initAction() {
        binding.edtSearchExpense.addTextChangedListener { text ->
            val query = text.toString().trim()
            if (query.isNotEmpty()) {
                expenseViewModel.dispatch(ExpenseAction.Search(query))
            } else {
                expenseViewModel.dispatch(ExpenseAction.GetByDay(System.currentTimeMillis()))
            }
        }

        binding.btnCalendar.setOnClickListener {
            expenseViewModel.dispatch(ExpenseAction.GetByDay(System.currentTimeMillis()))
        }
    }

    private fun setupRecyclerView() {
        binding.rcvExpenses.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = expensesAdapter
        }
    }

    private fun updateTotalCost(expenses: List<ExpenseEntity>) {
        val totalCost = expenses.sumOf { it.price }
        val formatter = DecimalFormat("#,###")
        binding.tvTotalCost.text = formatter.format(totalCost)
    }

    private fun updateExpensesByDay(expenses: List<ExpenseEntity>) {
        val groupedByDay = expenses.groupBy { expense ->
            val calendar = Calendar.getInstance().apply {
                timeInMillis = expense.date
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }
            calendar.timeInMillis
        }

        val dayExpensesList = groupedByDay.map { (dayTimestamp, expensesList) ->
            DayExpenses(dayTimestamp, expensesList)
        }.sortedByDescending { it.dayTimestamp }

        expensesAdapter.submitList(dayExpensesList)
    }
}