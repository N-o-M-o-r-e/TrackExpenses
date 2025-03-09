package com.tstool.trackexpenses.ui.view.detail

import android.content.Intent
import android.util.Log
import android.view.View
import androidx.core.net.toUri
import androidx.lifecycle.lifecycleScope
import com.tstool.trackexpenses.data.room.entity.ExpenseEntity
import com.tstool.trackexpenses.databinding.ActivityDetailBinding
import com.tstool.trackexpenses.ui.view.create.CreateExpensesActivity
import com.tstool.trackexpenses.ui.view.create.CreateExpensesActivity.Companion.KEY_EXPENSES_EDIT
import com.tstool.trackexpenses.ui.view.create.CreateExpensesActivity.Companion.KEY_RETURN_DATA
import com.tstool.trackexpenses.ui.view.home.fragment.expenses.ExpensesFragment.Companion.KEY_EXPENSE
import com.tstool.trackexpenses.ui.view.viewmodel.ExpenseEvent
import com.tstool.trackexpenses.ui.view.viewmodel.ExpenseUiAction
import com.tstool.trackexpenses.ui.view.viewmodel.ExpenseViewModel
import com.tstool.trackexpenses.utils.base.BaseActivity
import com.tstool.trackexpenses.utils.ktx.getResourceByTag
import com.tstool.trackexpenses.utils.ktx.loadImageWithGlide
import com.tstool.trackexpenses.utils.ktx.toCurrencyString
import com.tstool.trackexpenses.utils.ktx.toFormattedDateTime
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel

class DetailActivity : BaseActivity<ActivityDetailBinding>(ActivityDetailBinding::inflate) {

    private val viewModel: ExpenseViewModel by viewModel()
    private var expense: ExpenseEntity? = null

    private var tag = Int

    override fun initAds() {}

    override fun initViewModel() {
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

    override fun initData() {
        expense = getIntentData(KEY_EXPENSE, null)
        expense?.let { exp ->
            binding.tvExpenses.text = exp.itemName
            binding.tvDescription.text = exp.note
            binding.tvMoney.text = exp.price.toCurrencyString()
            binding.tvCalendar.text = exp.date.toFormattedDateTime()

            Log.e("__EX", "initViewModel: ${exp.imageUri}")

            if (exp.imageUri == null) {
                binding.imgExpenses.visibility = View.GONE
            } else {
                binding.imgExpenses.visibility = View.VISIBLE
                binding.imgExpenses.setImageURI(exp.imageUri.toUri())

            }

            loadImageWithGlide(
                imageView = binding.imgTag,
                resource = getResourceByTag(exp.category)
            )
        }
    }

    override fun initView() {

    }

    override fun initAction() {
        binding.btnBack.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        binding.btnEdit.setOnClickListener {
            goToNewActivityForResult(
                activity = CreateExpensesActivity::class.java,
                key = KEY_EXPENSES_EDIT,
                data = expense
            )
            Log.d("__DATA", "expense: $expense: ")
        }

        binding.btnDelete.setOnClickListener {
            expense?.let {
                viewModel.dispatch(ExpenseUiAction.Delete(it))
                onBackPressed()
            }
        }
    }

    override fun onActivityResultReceived(data: Intent?) {
        val message = data?.getBooleanExtra(KEY_RETURN_DATA, false)
        if (message == true) {
            onBackPressed()
        }

    }

}