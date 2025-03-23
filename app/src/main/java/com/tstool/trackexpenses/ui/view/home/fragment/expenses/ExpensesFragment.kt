package com.tstool.trackexpenses.ui.view.home.fragment.expenses

import android.util.Log
import androidx.core.widget.addTextChangedListener
import androidx.lifecycle.lifecycleScope
import com.tstool.trackexpenses.data.room.entity.ExpenseEntity
import com.tstool.trackexpenses.databinding.FragmentExpensesBinding
import com.tstool.trackexpenses.ui.view.create.MonthPickerHelper
import com.tstool.trackexpenses.ui.view.detail.DetailActivity
import com.tstool.trackexpenses.ui.view.home.fragment.expenses.adapter.DayExpenses
import com.tstool.trackexpenses.ui.view.home.fragment.expenses.adapter.ExpensesAdapter
import com.tstool.trackexpenses.ui.view.home.fragment.expenses.adapter.OnListenerExpenses
import com.tstool.trackexpenses.ui.view.viewmodel.ExpenseUiAction
import com.tstool.trackexpenses.ui.view.viewmodel.ExpenseViewModel
import com.tstool.trackexpenses.utils.base.BaseFragment
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.text.DecimalFormat
import java.util.Calendar

class ExpensesFragment : BaseFragment<FragmentExpensesBinding>(FragmentExpensesBinding::inflate) {

    private val viewModel by viewModel<ExpenseViewModel>()

    private val expensesAdapter: ExpensesAdapter by lazy {
        ExpensesAdapter(object : OnListenerExpenses {
            override fun onClickExpense(expense: ExpenseEntity) {
                Log.d(TAG, "onClickExpense: ")
                goToNewActivity(
                    activity = DetailActivity::class.java,
                    isFinish = false,
                    data = expense,
                    key = KEY_EXPENSE
                )
            }
        })
    }

    override fun initViewModel() {
        Log.d("__INSTANCE", "Instance VM in Frg: ${viewModel.hashCode()}")
        lifecycleScope.launch {
            viewModel.uiState.collect { state ->
                updateTotalCost(state.expenses)
                updateExpensesByDay(state.expenses)
            }
        }

        lifecycleScope.launch {
            viewModel.eventFlow.collect { event ->

            }
        }
    }

    override fun initData() {}

    override fun initView() {
        binding.rcvExpenses.adapter = expensesAdapter
    }

    override fun initAction() {
        binding.edtSearchExpense.addTextChangedListener { text ->
            val query = text.toString().trim()
            if (query.isNotEmpty()) {
                viewModel.dispatch(ExpenseUiAction.Search(query))
            } else {
                viewModel.dispatch(ExpenseUiAction.GetByDay(System.currentTimeMillis()))
            }
        }

        binding.btnDateTime.setOnClickListener {
            Log.i(TAG, "initAction: click date")
            MonthPickerHelper.showMonthPicker(requireContext()) { startOfMonth, endOfMonth ->
                viewModel.dispatch(ExpenseUiAction.FilterByDateRange(startOfMonth, endOfMonth))
//                val sdf = SimpleDateFormat("MM/yyyy", Locale("vi", "VN"))
//                binding.edtDateTime.setText(sdf.format(Date(startOfMonth))) // Hiển thị tháng đã chọn
            }
        }
    }

    override fun initAds() {}

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

    companion object {
        const val KEY_EXPENSE = "KEY_EXPENSE"
        const val TAG = "__ExpensesFragment"
    }
}