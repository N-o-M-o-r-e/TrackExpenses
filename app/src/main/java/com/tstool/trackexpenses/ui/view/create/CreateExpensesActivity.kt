package com.tstool.trackexpenses.ui.view.create

import android.content.Intent
import android.net.Uri
import android.util.Log
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import com.tstool.trackexpenses.data.model.ExpenseTag
import com.tstool.trackexpenses.data.room.entity.ExpenseEntity
import com.tstool.trackexpenses.databinding.ActivityCreateExpensesBinding
import com.tstool.trackexpenses.ui.view.create.adapter.ExpensesTagAdapter
import com.tstool.trackexpenses.ui.view.create.adapter.ListenerExpensesTag
import com.tstool.trackexpenses.ui.view.viewmodel.ExpenseEvent
import com.tstool.trackexpenses.ui.view.viewmodel.ExpenseUiAction
import com.tstool.trackexpenses.ui.view.viewmodel.ExpenseViewModel
import com.tstool.trackexpenses.utils.base.BaseActivity
import com.tstool.trackexpenses.utils.ktx.formatCurrencyInput
import com.tstool.trackexpenses.utils.ktx.getCurrencyValue
import com.yalantis.ucrop.UCrop
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class CreateExpensesActivity :
    BaseActivity<ActivityCreateExpensesBinding>(ActivityCreateExpensesBinding::inflate) {

    private val viewModel by viewModel<ExpenseViewModel>()
    private lateinit var adapter: ExpensesTagAdapter
    private val listTag = ExpenseTag.entries
    private var selectedTag: String = ExpenseTag.entries[0].nameTag
    private var imagePath: String? = null
    private var isAdding = false

    override fun initAds() {}

    override fun initViewModel() {
        Log.d("__INSTANCE", "Instance VM in CreateAct: ${viewModel.hashCode()}")

        lifecycleScope.launch {
            viewModel.uiState.collect { state ->
                binding.progressBar.isVisible = state.isLoading
            }
        }

        lifecycleScope.launch {
            viewModel.eventFlow.collect { event ->
                when (event) {
                    ExpenseEvent.ExpenseAdded -> {
                        onBackPressed()
                    }
                    ExpenseEvent.ExpenseDeleted -> TODO()
                    ExpenseEvent.ExpenseFiltered -> TODO()
                    ExpenseEvent.ExpenseSearched -> TODO()
                    ExpenseEvent.ExpenseUpdated -> TODO()
                    is ExpenseEvent.ShowToast -> {
                        toastMessage(event.message)
                    }
                    is ExpenseEvent.ShowPrices -> TODO()
                }
            }
        }
    }

    override fun initData() {}

    override fun initView() {
        binding.edtInputPrice.formatCurrencyInput()
        adapter = ExpensesTagAdapter(object : ListenerExpensesTag {
            override fun onClickExpensesTag(folder: ExpenseTag) {
                selectedTag = folder.nameTag
            }
        })
        binding.rcvExpensesTag.adapter = adapter
        adapter.submitList(listTag)
    }

    override fun initAction() {
        binding.btnBack.setOnClickListener { onBackPressedDispatcher.onBackPressed() }
        binding.btnQuestion.setOnClickListener { toastMessage("Show") }
        binding.btnImportImage.setOnClickListener { pickImageLauncher.launch("image/*") }
        binding.btnAddExpense.setOnClickListener {
            if (!isAdding) {
                isAdding = true
                binding.btnAddExpense.isEnabled = false

                val itemName = binding.edtInputName.text.toString().trim()
                val priceText = binding.edtInputPrice.text.toString().trim()
                val note = binding.edtInputNote.text.toString().trim()
                val category = selectedTag

                if (itemName.isEmpty()) {
                    Log.w("__CR", "Item name is empty")
                    toastMessage("Please enter expense name")
                    isAdding = false
                    binding.btnAddExpense.isEnabled = true
                    return@setOnClickListener
                }
                if (priceText.isEmpty()) {
                    toastMessage("Please enter price")
                    isAdding = false
                    binding.btnAddExpense.isEnabled = true
                    return@setOnClickListener
                }
                val price = binding.edtInputPrice.getCurrencyValue()
                if (price == null || price < 0) {
                    toastMessage("Invalid price")
                    isAdding = false
                    binding.btnAddExpense.isEnabled = true
                    return@setOnClickListener
                }

                val expense = ExpenseEntity(
                    id = 0,
                    category = category,
                    itemName = itemName,
                    price = price,
                    imageUri = imagePath,
                    note = note.ifEmpty { null },
                    date = System.currentTimeMillis()
                )

                Log.i("__CR", "insert: $expense")
                viewModel.dispatch(ExpenseUiAction.Add(expense))
            }
        }
    }

    private val pickImageLauncher =
        registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
            uri?.let { startCrop(it) }
        }

    private fun startCrop(sourceUri: Uri) {
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val destinationFileName = "Expense_$timeStamp.jpg"
        val destinationUri = Uri.fromFile(File(cacheDir, destinationFileName))
        UCrop.of(sourceUri, destinationUri).withAspectRatio(1f, 1f).withMaxResultSize(512, 512)
            .start(this)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when {
            resultCode == RESULT_OK && requestCode == UCrop.REQUEST_CROP -> {
                val resultUri = data?.let { UCrop.getOutput(it) }
                resultUri?.let {
                    imagePath = saveImageToInternalStorage(it)
                    if (imagePath != null) {
                        binding.imgDes.setImageURI(Uri.parse(imagePath))
                        binding.imgDes.visibility = View.VISIBLE
                    } else {
                        toastMessage("Failed to save image")
                    }
                }
            }
            resultCode == UCrop.RESULT_ERROR -> {
                val cropError = data?.let { UCrop.getError(it) }
                Log.e("__CR", "Crop error: ${cropError?.message}")
                toastMessage("Crop error: ${cropError?.message}")
            }
        }
    }

    private fun saveImageToInternalStorage(uri: Uri): String? {
        return try {
            val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
            val fileName = "Expense_$timeStamp.jpg"
            val internalFile = File(filesDir, fileName)
            contentResolver.openInputStream(uri)?.use { inputStream ->
                FileOutputStream(internalFile).use { outputStream ->
                    inputStream.copyTo(outputStream)
                }
            }
            internalFile.absolutePath
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    companion object{
        const val TAG = "__CR"
    }
}