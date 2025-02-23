package com.tstool.trackexpenses.ui.view.create

import com.tstool.trackexpenses.data.model.ExpenseTag
import com.tstool.trackexpenses.databinding.ActivityCreateExpensesBinding
import com.tstool.trackexpenses.ui.view.create.adapter.ExpensesTagAdapter
import com.tstool.trackexpenses.ui.view.create.adapter.ListenerExpensesTag
import com.tstool.trackexpenses.utils.base.BaseActivity

class CreateExpensesActivity :
    BaseActivity<ActivityCreateExpensesBinding>(ActivityCreateExpensesBinding::inflate) {
    private lateinit var adapter: ExpensesTagAdapter
    private val listTag = ExpenseTag.entries

    override fun initAds() {

    }

    override fun initViewModel() {

    }

    override fun initData() {
        adapter = ExpensesTagAdapter(object : ListenerExpensesTag {
            override fun onClickExpensesTag(folder: ExpenseTag) {
                toastMessage(folder.nameTag)
            }
        })
        binding.rcvExpensesTag.adapter = adapter
        adapter.submitList(listTag)
    }

    override fun initView() {

    }

    override fun initAction() {
        binding.btnBack.setOnClickListener {
            onBackPressed()
        }

        binding.btnQuestion.setOnClickListener {
            toastMessage("Show")
        }
    }
} 