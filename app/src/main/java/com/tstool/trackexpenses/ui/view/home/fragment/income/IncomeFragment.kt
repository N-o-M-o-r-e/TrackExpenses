package com.tstool.trackexpenses.ui.view.home.fragment.income

import android.util.Log
import android.view.View
import androidx.lifecycle.lifecycleScope
import com.tstool.trackexpenses.data.model.IncomeTag
import com.tstool.trackexpenses.data.room.entity.IncomeEntity
import com.tstool.trackexpenses.databinding.FragmentIncomeBinding
import com.tstool.trackexpenses.ui.view.create.MonthPickerHelper
import com.tstool.trackexpenses.ui.view.detail.DetailActivity
import com.tstool.trackexpenses.ui.view.home.fragment.expenses.ExpensesFragment
import com.tstool.trackexpenses.ui.view.home.fragment.income.adapter.CategoriesAdapter
import com.tstool.trackexpenses.ui.view.home.fragment.income.adapter.IncomeAdapter
import com.tstool.trackexpenses.ui.view.home.fragment.income.adapter.OnListenerIncome
import com.tstool.trackexpenses.ui.view.home.fragment.income.adapter.OnListenerTagIncome
import com.tstool.trackexpenses.ui.view.viewmodel.IncomeUiAction
import com.tstool.trackexpenses.ui.view.viewmodel.IncomeViewModel
import com.tstool.trackexpenses.utils.base.BaseFragment
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.text.DecimalFormat
import java.util.Calendar

class IncomeFragment : BaseFragment<FragmentIncomeBinding>(FragmentIncomeBinding::inflate) {

    private val viewModel by viewModel<IncomeViewModel>()
    private val listCategory = IncomeTag.entries

    private val adapterIncome: IncomeAdapter by lazy {
        IncomeAdapter(object : OnListenerIncome {
            override fun onClickIncome(income: IncomeEntity) {
                Log.d(TAG, "onClickIncome: $income")
                goToNewActivity(
                    activity = DetailActivity::class.java,
                    isFinish = false,
                    data = income,
                    key = KEY_INCOME
                )
            }
        })
    }

    private val adapterCategory: CategoriesAdapter by lazy {
        CategoriesAdapter(object : OnListenerTagIncome {
            override fun onClickTag(tag: IncomeTag) {
                Log.d(TAG, "onClickTag: ${tag.nameTag}")
                viewModel.dispatch(IncomeUiAction.Search(tag.nameTag))
                binding.tvTitleSpending.text = buildString {
                    append("Total ")
                    append(tag.nameTag)
                    append(":")
                }
            }
        })
    }

    override fun initAds() {}

    override fun initViewModel() {
        lifecycleScope.launch {
            viewModel.uiState.collect { state ->
                adapterIncome.submitList(state.incomes)
                updateTotalCost(state.incomes)
            }
        }
    }

    override fun initData() {
        binding.rcvCategory.adapter = adapterCategory
        adapterCategory.submitList(listCategory)
    }

    override fun initView() {
        binding.rcvIncome.adapter = adapterIncome
        val calendar = Calendar.getInstance()
        val currentMonth = calendar.get(Calendar.MONTH) + 1
        val currentYear = calendar.get(Calendar.YEAR)
        binding.tvMonth.text = "Thg $currentMonth"
        binding.tvYear.text = "$currentYear"
    }

    private fun updateTotalCost(incomes: List<IncomeEntity>) {
        val totalCost = incomes.sumOf { it.amount }
        val formatter = DecimalFormat("#,###")
        binding.tvTotalCost.text = formatter.format(totalCost)
    }

    override fun initAction() {
        binding.btnTag.setOnCheckedChangeListener { _, isChecked ->
            when (isChecked) {
                true -> {
                    binding.rcvCategory.visibility = View.VISIBLE
                    binding.viewLine.visibility = View.VISIBLE
                    viewModel.dispatch(IncomeUiAction.Search(listCategory.first().nameTag))
                    binding.tvTitleSpending.text = buildString {
                        append("Total ")
                        append(listCategory.first().nameTag)
                        append(":")
                    }
                }
                false -> {
                    binding.rcvCategory.visibility = View.GONE
                    binding.viewLine.visibility = View.GONE
                    binding.tvTitleSpending.text = "Total income:"
                    val currentState = viewModel.uiState.value
                    if (currentState.startDate != null && currentState.endDate != null) {
                        viewModel.dispatch(
                            IncomeUiAction.FilterByDateRange(
                                currentState.startDate,
                                currentState.endDate
                            )
                        )
                    } else {
                        viewModel.dispatch(IncomeUiAction.Load)
                    }
                }
            }
        }

        binding.btnDateTime.setOnClickListener {
            Log.i(ExpensesFragment.TAG, "initAction: click date")
            MonthPickerHelper.showMonthPicker(requireContext()) { startOfMonth, endOfMonth ->
                viewModel.dispatch(IncomeUiAction.FilterByDateRange(startOfMonth, endOfMonth))
                val calendar = Calendar.getInstance().apply { timeInMillis = startOfMonth }
                val month = calendar.get(Calendar.MONTH) + 1
                val year = calendar.get(Calendar.YEAR)
                binding.tvMonth.text = "Thg $month"
                binding.tvYear.text = "$year"
            }
        }
    }

    companion object {
        const val TAG = "__IncomeFragment"
        const val KEY_INCOME = "KEY_INCOME"
    }
}