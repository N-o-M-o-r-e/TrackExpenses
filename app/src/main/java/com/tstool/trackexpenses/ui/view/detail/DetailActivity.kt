package com.tstool.trackexpenses.ui.view.detail

import android.view.View
import androidx.core.net.toUri
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import com.tstool.trackexpenses.data.model.ExpenseTag
import com.tstool.trackexpenses.data.room.entity.ExpenseEntity
import com.tstool.trackexpenses.databinding.ActivityDetailBinding
import com.tstool.trackexpenses.ui.view.home.fragment.expenses.ExpensesFragment.Companion.KEY_EXPENSE
import com.tstool.trackexpenses.ui.view.viewmodel.ExpenseEvent
import com.tstool.trackexpenses.ui.view.viewmodel.ExpenseUiAction
import com.tstool.trackexpenses.ui.view.viewmodel.ExpenseViewModel
import com.tstool.trackexpenses.utils.base.BaseActivity
import com.tstool.trackexpenses.utils.ktx.loadImageWithGlide
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel

class DetailActivity : BaseActivity<ActivityDetailBinding>(ActivityDetailBinding::inflate) {

    private val viewModel: ExpenseViewModel by viewModel()
    private var expense: ExpenseEntity? = null

    private var tag = Int

    override fun initAds() {}

    override fun initViewModel() {
        expense = getIntentData(KEY_EXPENSE, null)
        expense?.let {
            binding.tvExpenses.text = it.itemName
            binding.edtDescription.setText(it.note)

            if (it.imageUri == null) binding.imgExpenses.visibility = View.VISIBLE
            else binding.imgExpenses.visibility = View.GONE

            loadImageWithGlide(imageView = binding.imgExpenses, uri = it.imageUri?.toUri())

            loadImageWithGlide(imageView = binding.imgTag, resource = getResourceByTag(it.category))
        }

        lifecycleScope.launch {
            viewModel.uiState.collect { event ->
                if (event.isLoading){
                    toastMessage("Loading....")
                }else{
                    toastMessage("Success")
                }
            }
        }

        lifecycleScope.launch {
            viewModel.eventFlow.collect { event ->
                when (event) {
                    is ExpenseEvent.ExpenseUpdated ->{
                        onBackPressed()
                    }
                    is ExpenseEvent.ExpenseDeleted -> {
                        onBackPressed()
                    }
                    else -> {}
                }
            }

        }
    }

    override fun initData() {}

    override fun initView() {
        // Hiện các nút edit/delete nếu cần
        binding.actEdit.visibility = View.VISIBLE
        binding.actDelete.visibility = View.VISIBLE
    }

    override fun initAction() {
        binding.btnBack.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        binding.btnUpdate.setOnClickListener {
            expense?.let {
                val updatedExpense = it.copy(note = binding.edtDescription.text.toString().trim())
                viewModel.dispatch(ExpenseUiAction.Update(updatedExpense))
            }
        }

        binding.btnDelete.setOnClickListener {
            expense?.let { viewModel.dispatch(ExpenseUiAction.Delete(it)) }
        }

        binding.actEdit.isVisible = false

        binding.actDelete.isVisible = false
    }

    private fun getResourceByTag(nameTag: String): Int? {
        return ExpenseTag.entries.firstOrNull { it.nameTag == nameTag }?.resource
    }

}