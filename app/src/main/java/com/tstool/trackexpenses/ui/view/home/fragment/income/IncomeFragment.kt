package com.tstool.trackexpenses.ui.view.home.fragment.income

import android.util.Log
import android.view.View
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModel
import androidx.lifecycle.lifecycleScope
import com.tstool.trackexpenses.data.model.IncomeTag
import com.tstool.trackexpenses.data.room.entity.ExpenseEntity
import com.tstool.trackexpenses.data.room.entity.IncomeEntity
import com.tstool.trackexpenses.databinding.FragmentIncomeBinding
import com.tstool.trackexpenses.ui.view.home.fragment.income.adapter.CategoriesAdapter
import com.tstool.trackexpenses.ui.view.home.fragment.income.adapter.IncomeAdapter
import com.tstool.trackexpenses.ui.view.home.fragment.income.adapter.OnListenerIncome
import com.tstool.trackexpenses.ui.view.home.fragment.income.adapter.OnListenerTagIncome
import com.tstool.trackexpenses.ui.view.viewmodel.IncomeEvent
import com.tstool.trackexpenses.ui.view.viewmodel.IncomeUiAction
import com.tstool.trackexpenses.ui.view.viewmodel.IncomeViewModel
import com.tstool.trackexpenses.utils.base.BaseFragment
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.text.DecimalFormat


/**
 * A simple [Fragment] subclass.
 * Use the [IncomeFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class IncomeFragment : BaseFragment<FragmentIncomeBinding>(FragmentIncomeBinding::inflate) {

    private val viewModel by viewModel<IncomeViewModel>()
    private val listCategory = IncomeTag.entries

    private  val adapterIncome: IncomeAdapter by lazy {
        IncomeAdapter(object : OnListenerIncome {
            override fun onClickIncome(income: IncomeEntity) {
                Log.d(TAG, "onClickIncome: $income")
            }
        })
    }

    private val adapterCategory : CategoriesAdapter by lazy {
        CategoriesAdapter(object : OnListenerTagIncome {
            override fun onClickTag(tag: IncomeTag) {
                Log.d(TAG, "onClickTag: ${tag.nameTag}")
                viewModel.dispatch(IncomeUiAction.Search(tag.nameTag))
            }
        })
    }

    override fun initAds() {

    }

    override fun initViewModel() {
        lifecycleScope.launch {
            viewModel.uiState.collect{stater ->
                adapterIncome.submitList(stater.incomes)
                updateTotalCost(stater.incomes)
            }
        }
        lifecycleScope.launch {
            viewModel.eventFlow.collect{event ->

            }
        }
    }

    override fun initData() {
        binding.rcvCategory.adapter = adapterCategory
        adapterCategory.submitList(listCategory)

    }

    override fun initView() {
        binding.rcvIncome.adapter = adapterIncome
    }

    private fun updateTotalCost(incomes: List<IncomeEntity>) {
        val totalCost = incomes.sumOf { it.amount }
        val formatter = DecimalFormat("#,###")
        binding.tvTotalCost.text = formatter.format(totalCost)
    }

    override fun initAction() {
        binding.btnTag.setOnCheckedChangeListener { _, isChecked ->
            when(isChecked){
                true -> {
                    binding.rcvCategory.visibility = View.VISIBLE
                    binding.viewLine.visibility = View.VISIBLE
                    viewModel.dispatch(IncomeUiAction.Search(listCategory.first().nameTag))
                }
                false -> {
                    binding.rcvCategory.visibility = View.GONE
                    binding.viewLine.visibility = View.GONE
                    viewModel.dispatch(IncomeUiAction.Load)
                }
            }
        }
    }

    companion object{
        const val TAG = "__IncomeFragment"

    }
} 