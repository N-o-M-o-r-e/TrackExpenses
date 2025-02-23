package com.tstool.trackexpenses.test

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.tstool.trackexpenses.R
import com.tstool.trackexpenses.data.room.entity.ExpenseEntity
import com.tstool.trackexpenses.data.room.entity.IncomeEntity
import com.tstool.trackexpenses.data.room.entity.TransactionEntity
import com.tstool.trackexpenses.databinding.ActivityTestBinding
import com.tstool.trackexpenses.ui.view.viewmodel.FinanceViewModel
import com.tstool.trackexpenses.utils.base.BaseActivity
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel

class TestActivity : BaseActivity<ActivityTestBinding>(ActivityTestBinding::inflate) {
    private val financeViewModel: FinanceViewModel by viewModel()

    override fun initAds() {

    }

    override fun initViewModel() {

    }

    override fun initData() {

    }

    override fun initView() {

    }

    override fun initAction() {
// ========================== TÌM KIẾM CHI TIÊU ==========================
        binding.btnSearchExpense.setOnClickListener {
            val keyword = binding.edtSearchExpense.text.toString().trim()
            financeViewModel.searchExpenses(keyword)
        }

        // ========================== TÌM KIẾM THU NHẬP ==========================
        binding.btnSearchIncome.setOnClickListener {
            val keyword = binding.edtSearchIncome.text.toString().trim()
            financeViewModel.searchIncomes(keyword)
        }

        // ========================== TÌM KIẾM GIAO DỊCH ==========================
        binding.btnSearchTransaction.setOnClickListener {
            val keyword = binding.edtSearchTransaction.text.toString().trim()
            financeViewModel.searchTransactions(keyword)
        }

        // ========================== HIỂN THỊ KẾT QUẢ ==========================
        lifecycleScope.launch {
            financeViewModel.expenses.collect { results ->
                binding.textResults.text = results.joinToString("\n") { "${it.itemName} - ${it.amount} VND" }
            }
        }

        lifecycleScope.launch {
            financeViewModel.incomes.collect { results ->
                binding.textResults.text = results.joinToString("\n") { "${it.source} - ${it.amount} VND" }
            }
        }

        lifecycleScope.launch {
            financeViewModel.transactions.collect { results ->
                binding.textResults.text = results.joinToString("\n") { "${it.description} - ${it.amount} VND" }
            }
        }

        // ========================== XỬ LÝ THÊM DỮ LIỆU ==========================

        binding.btnAddExpense.setOnClickListener {
            val name = binding.edtExpenseName.text.toString()
            val amount = binding.edtExpenseAmount.text.toString().toDoubleOrNull() ?: 0.0

            if (name.isNotEmpty() && amount > 0) {
                financeViewModel.insertExpense(
                    ExpenseEntity(
                        category = "Khác",
                        itemName = name,
                        amount = amount,
                        date = System.currentTimeMillis(),
                        imageUri = null,
                        note = null
                    )
                )

                binding.edtExpenseName.text.clear()
                binding.edtExpenseAmount.text.clear()
            }
        }

        binding.btnAddIncome.setOnClickListener {
            val source = binding.edtIncomeSource.text.toString()
            val amount = binding.edtIncomeAmount.text.toString().toDoubleOrNull() ?: 0.0

            if (source.isNotEmpty() && amount > 0) {
                financeViewModel.insertIncome(
                    IncomeEntity(
                    source = source,
                    amount = amount,
                    date = System.currentTimeMillis(),
                    category = "khác",
                    imageUri = null,
                    note = null,
                )
                )
                binding.edtIncomeSource.text.clear()
                binding.edtIncomeAmount.text.clear()
            }
        }

        binding.btnAddTransaction.setOnClickListener {
            val amount = binding.edtTransactionAmount.text.toString().toDoubleOrNull() ?: 0.0
            val desc = binding.edtTransactionDesc.text.toString()

            if (desc.isNotEmpty() && amount > 0) {
                financeViewModel.insertTransaction(
                    TransactionEntity(
                    amount = amount,
                    description = desc,
                    date = System.currentTimeMillis(),
                    type = "send",
                    image_uri = null
                )
                )
                binding.edtTransactionAmount.text.clear()
                binding.edtTransactionDesc.text.clear()
            }
        }

    }

}