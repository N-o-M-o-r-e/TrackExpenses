package com.tstool.trackexpenses.data.model

import com.tstool.trackexpenses.R


enum class ExpenseTag(val nameTag: String, val resource: Int) {
    BillsAndUtilities("Bills & Utilities", R.drawable.ic_bill),
    FoodAndDrinks("Food & Drinks", R.drawable.ic_drink),
    Transportation("Transportation", R.drawable.ic_transportation),
    PersonalShopping("Personal Shopping", R.drawable.ic_shopping),
    EntertainmentAndLeisure("Entertainment & Leisure", R.drawable.ic_entertainment),
    EducationAndWork("Education & Work", R.drawable.ic_education),
    HealthAndMedical("Health & Medical", R.drawable.ic_hospital),
    MiscellaneousExpenses("Miscellaneous Expenses", R.drawable.ic_more),
}