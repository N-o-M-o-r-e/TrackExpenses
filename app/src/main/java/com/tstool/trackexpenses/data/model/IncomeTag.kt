package com.tstool.trackexpenses.data.model

import com.tstool.trackexpenses.R

enum class IncomeTag(val nameTag: String, val resource: Int) {
    Salary("Salary", R.drawable.ic_salary),
    Bonus("Bonus", R.drawable.ic_bonus),
    Investment("Investment", R.drawable.ic_investment),
    Gift("Gift", R.drawable.ic_gift)

}

