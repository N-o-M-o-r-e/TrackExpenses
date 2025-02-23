package com.tstool.trackexpenses.ui.view.home.fragment

import androidx.fragment.app.Fragment
import com.tstool.trackexpenses.databinding.FragmentExpensesBinding
import com.tstool.trackexpenses.ui.view.create.CreateExpensesActivity
import com.tstool.trackexpenses.ui.view.viewmodel.FinanceViewModel
import com.tstool.trackexpenses.utils.base.BaseFragment
import org.koin.androidx.viewmodel.ext.android.activityViewModel


/**
 * A simple [Fragment] subclass.
 * Use the [ExpensesFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class ExpensesFragment : BaseFragment<FragmentExpensesBinding>(FragmentExpensesBinding::inflate) {

    private val viewModel by activityViewModel<FinanceViewModel>()


    override fun initAds() {

    }

    override fun initViewModel() {

    }

    override fun initData() {
        viewModel.eventCreateExpenses.observe(viewLifecycleOwner) {expenses ->
            goToNewActivity(CreateExpensesActivity::class.java, false)
        }
    }

    override fun initView() {

    }

    override fun initAction() {

    }


}

