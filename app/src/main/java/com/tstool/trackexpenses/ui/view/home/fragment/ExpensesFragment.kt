package com.tstool.trackexpenses.ui.view.home.fragment

import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.tstool.trackexpenses.data.room.entity.ExpenseEntity
import com.tstool.trackexpenses.databinding.FragmentExpensesBinding
import com.tstool.trackexpenses.ui.view.viewmodel.ExpenseAction
import com.tstool.trackexpenses.ui.view.viewmodel.ExpenseEvent
import com.tstool.trackexpenses.ui.view.viewmodel.ExpenseViewModel
import com.tstool.trackexpenses.utils.base.BaseFragment
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel


/**
 * A simple [Fragment] subclass.
 * Use the [ExpensesFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class ExpensesFragment : BaseFragment<FragmentExpensesBinding>(FragmentExpensesBinding::inflate) {

    private val viewModel: ExpenseViewModel by viewModel()

    override fun initAds() {

    }

    override fun initViewModel() {
        // Lắng nghe UI State
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.uiStateFlow.collect { state ->
                // Cập nhật UI với state.expenses
            }
        }

        // Lắng nghe Events
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.eventFlow.collect { event ->
                when (event) {
                    is ExpenseEvent.ShowSuccess -> {
                        Toast.makeText(context, event.message, Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }

    override fun initData() {

        val newExpense = ExpenseEntity(
            category = "Food",
            itemName = "Pizza",
            amount = 15.0,
            date = System.currentTimeMillis()
        )
        viewModel.dispatch(ExpenseAction.Add(newExpense))
    }

    override fun initView() {

    }

    override fun initAction() {

    }


}

