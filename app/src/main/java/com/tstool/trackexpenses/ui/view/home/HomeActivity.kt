package com.tstool.trackexpenses.ui.view.home

import android.util.Log
import androidx.viewpager2.widget.ViewPager2
import com.tstool.trackexpenses.databinding.ActivityHomeBinding
import com.tstool.trackexpenses.ui.view.create.expenses.CreateExpensesActivity
import com.tstool.trackexpenses.ui.view.create.income.CreateIncomeActivity
import com.tstool.trackexpenses.ui.view.home.fragment.ViewPagerAdapter
import com.tstool.trackexpenses.ui.view.viewmodel.ExpenseViewModel
import com.tstool.trackexpenses.utils.base.BaseActivity
import org.koin.androidx.viewmodel.ext.android.viewModel

class HomeActivity : BaseActivity<ActivityHomeBinding>(ActivityHomeBinding::inflate) {
    private lateinit var viewPager: ViewPager2
    private lateinit var viewPagerAdapter: ViewPagerAdapter
    private val viewModel by viewModel<ExpenseViewModel>()
    private var indexPage = 0

    override fun initAds() {

    }

    override fun initViewModel() {
        Log.d("__INSTANCE", "Instance Home: ${viewModel.hashCode()}")
    }

    override fun initData() {}

    override fun initView() {
        viewPager = binding.viewPager
        viewPagerAdapter = ViewPagerAdapter(this)
        viewPager.adapter = viewPagerAdapter
        viewPager.offscreenPageLimit = 3
        viewPager.setPageTransformer(FadePageTransformer())

        setUpNavigation()
    }

    private fun setUpNavigation() {
        val radioButtons = listOf(
            binding.bottomBar.navExpenses,
            binding.bottomBar.navIncome,
            binding.bottomBar.navChart,
            binding.bottomBar.navSetting
        )

        radioButtons.forEachIndexed { index, radioButton ->
            radioButton.setOnCheckedChangeListener { _, isChecked ->
                if (isChecked) {
                    viewPager.currentItem = index
                    radioButtons.forEach {
                        if (it != radioButton) {
                            it.isChecked = false
                        }
                    }
                }
            }
        }

        viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                radioButtons[position].isChecked = true
                indexPage = position
            }
        })
    }

    override fun initAction() {
        binding.bottomBar.cardAdd.setOnClickListener {
            when(indexPage){
                0-> {
                    goToNewActivity(CreateExpensesActivity::class.java)
                    return@setOnClickListener
                }
                1-> {
                    goToNewActivity(CreateIncomeActivity::class.java)
                    return@setOnClickListener
                }
                else->{
                    goToNewActivity(CreateExpensesActivity::class.java)
                    return@setOnClickListener
                }
            }

        }
    }
}