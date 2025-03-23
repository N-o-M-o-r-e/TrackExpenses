package com.tstool.trackexpenses.ui.view.home

import android.util.Log
import androidx.navigation.NavController
import androidx.navigation.findNavController
import com.tstool.trackexpenses.R
import com.tstool.trackexpenses.databinding.ActivityHomeBinding
import com.tstool.trackexpenses.ui.view.create.expenses.CreateExpensesActivity
import com.tstool.trackexpenses.ui.view.create.income.CreateIncomeActivity
import com.tstool.trackexpenses.ui.view.viewmodel.ExpenseViewModel
import com.tstool.trackexpenses.utils.base.BaseActivity
import org.koin.androidx.viewmodel.ext.android.viewModel

class HomeActivity : BaseActivity<ActivityHomeBinding>(ActivityHomeBinding::inflate) {

    private lateinit var navController: NavController

    private val viewModel by viewModel<ExpenseViewModel>()
    private var indexPage = 0

    override fun initAds() {

    }

    override fun initViewModel() {
        Log.d("__INSTANCE", "Instance Home: ${viewModel.hashCode()}")
    }

    override fun initData() {}

    override fun initView() {
        navController = findNavController(R.id.navComponent)

    }

    private fun setupBottomNavigation() {
        binding.bottomBar.navExpenses.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) navController.navigate(R.id.expensesFragment)
        }
        binding.bottomBar.navIncome.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) navController.navigate(R.id.incomeFragment)
        }
        binding.bottomBar.navChart.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) navController.navigate(R.id.chartFragment)
        }
        binding.bottomBar.navSetting.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) navController.navigate(R.id.settingFragment)
        }

        navController.addOnDestinationChangedListener { _, destination, _ ->
            Log.d("NAV_DEBUG", "Destination changed to: ${destination.id}")
            when (destination.id) {
                R.id.expensesFragment -> {
                    binding.bottomBar.navExpenses.isChecked = true
                    binding.bottomBar.navIncome.isChecked = false
                    binding.bottomBar.navChart.isChecked = false
                    binding.bottomBar.navSetting.isChecked = false
                    indexPage = 0
                }
                R.id.incomeFragment -> {
                    binding.bottomBar.navExpenses.isChecked = false
                    binding.bottomBar.navIncome.isChecked = true
                    binding.bottomBar.navChart.isChecked = false
                    binding.bottomBar.navSetting.isChecked = false
                    indexPage = 1
                }
                R.id.chartFragment -> {
                    binding.bottomBar.navExpenses.isChecked = false
                    binding.bottomBar.navIncome.isChecked = false
                    binding.bottomBar.navChart.isChecked = true
                    binding.bottomBar.navSetting.isChecked = false
                    indexPage = 2
                }
                R.id.settingFragment -> {
                    binding.bottomBar.navExpenses.isChecked = false
                    binding.bottomBar.navIncome.isChecked = false
                    binding.bottomBar.navChart.isChecked = false
                    binding.bottomBar.navSetting.isChecked = true
                    indexPage = 3
                }
            }
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        return navController.navigateUp() || super.onSupportNavigateUp()
    }


    override fun initAction() {
        setupBottomNavigation()

        binding.bottomBar.cardAdd.setOnClickListener {
            when (navController.currentDestination?.id) {
                R.id.expensesFragment -> goToNewActivity(CreateExpensesActivity::class.java)
                R.id.incomeFragment -> goToNewActivity(CreateIncomeActivity::class.java)
                else -> goToNewActivity(CreateExpensesActivity::class.java)
            }
        }
    }
}