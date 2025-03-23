package com.tstool.trackexpenses.ui.view.home.fragment

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.tstool.trackexpenses.ui.view.home.fragment.chart.ChartFragment
import com.tstool.trackexpenses.ui.view.home.fragment.expenses.ExpensesFragment
import com.tstool.trackexpenses.ui.view.home.fragment.income.IncomeFragment
import com.tstool.trackexpenses.ui.view.home.fragment.setting.SettingFragment

class ViewPagerAdapter(fragmentActivity: FragmentActivity) : FragmentStateAdapter(fragmentActivity) {
    private val fragments = listOf(
        ExpensesFragment(),
        IncomeFragment(),
        ChartFragment(),
        SettingFragment()
    )

    override fun getItemCount(): Int = fragments.size

    override fun createFragment(position: Int): Fragment = fragments[position]
}