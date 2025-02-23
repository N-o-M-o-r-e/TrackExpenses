package com.tstool.trackexpenses.ui.view.home

import androidx.viewpager2.widget.ViewPager2
import com.tstool.trackexpenses.databinding.ActivityHomeBinding
import com.tstool.trackexpenses.ui.view.home.fragment.adapter.ViewPagerAdapter
import com.tstool.trackexpenses.ui.view.viewmodel.FinanceViewModel
import com.tstool.trackexpenses.utils.base.BaseActivity
import org.koin.androidx.viewmodel.ext.android.viewModel

class HomeActivity : BaseActivity<ActivityHomeBinding>(ActivityHomeBinding::inflate) {
    private lateinit var viewPager: ViewPager2
    private lateinit var viewPagerAdapter: ViewPagerAdapter
    private val viewModel by viewModel<FinanceViewModel>()


    override fun initAds() {}

    override fun initViewModel() {}

    override fun initData() {}

    override fun initView() {
        viewPager = binding.viewPager
        viewPagerAdapter = ViewPagerAdapter(this)
        viewPager.adapter = viewPagerAdapter
        viewPager.setOffscreenPageLimit(3)
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
            }
        })
    }

    override fun initAction() {
        binding.bottomBar.cardAdd.setOnClickListener {
            viewModel.triggerCreateExpensesEvent()
        }
    }
}

