package com.tstool.trackexpenses.ui.view.create.expenses

import android.app.Activity
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Intent
import android.net.Uri
import android.util.Log
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.net.toUri
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import com.tstool.trackexpenses.data.model.ExpenseTag
import com.tstool.trackexpenses.data.room.entity.ExpenseEntity
import com.tstool.trackexpenses.databinding.ActivityCreateExpensesBinding
import com.tstool.trackexpenses.ui.view.create.expenses.adapter.ExpensesTagAdapter
import com.tstool.trackexpenses.ui.view.create.expenses.adapter.ListenerExpensesTag
import com.tstool.trackexpenses.ui.view.viewmodel.ExpenseEvent
import com.tstool.trackexpenses.ui.view.viewmodel.ExpenseUiAction
import com.tstool.trackexpenses.ui.view.viewmodel.ExpenseViewModel
import com.tstool.trackexpenses.utils.base.BaseActivity
import com.tstool.trackexpenses.utils.ktx.formatCurrencyInput
import com.tstool.trackexpenses.utils.ktx.getCurrencyValue
import com.tstool.trackexpenses.utils.ktx.toCurrencyString
import com.tstool.trackexpenses.utils.ktx.toFormattedDateTime
import com.yalantis.ucrop.UCrop
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Calendar
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

    private lateinit var itemName: String
    private lateinit var priceText: String
    private lateinit var note: String

    private var expense: ExpenseEntity? = null
    private var selectedTimestamp: Long? = null

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
                    ExpenseEvent.ExpenseDeleted -> {

                    }

                    ExpenseEvent.ExpenseFiltered -> {

                    }

                    ExpenseEvent.ExpenseSearched -> {

                    }

                    ExpenseEvent.ExpenseUpdated -> {

                    }
                    is ExpenseEvent.ShowToast -> {
                        toastMessage(event.message)
                    }
                    is ExpenseEvent.ShowPrices -> TODO()
                }
            }
        }
    }

    override fun initData() {
        this.expense = getIntentData(KEY_EXPENSES_EDIT, null)
        if (expense == null) {
            binding.tvTitleBar.text = "Create Expenses"
            binding.btnAddExpense.visibility = View.VISIBLE
            binding.btnUpdateExpense.visibility = View.GONE
            selectedTimestamp = System.currentTimeMillis()
        } else {
            binding.tvTitleBar.text = "Update Expenses"
            binding.btnAddExpense.visibility = View.GONE
            binding.btnUpdateExpense.visibility = View.VISIBLE
        }
        expense?.let {
            binding.edtInputName.setText(it.itemName)
            binding.edtInputPrice.setText(it.price.toCurrencyString())
            binding.edtInputNote.setText(it.note)
            binding.imgDes.visibility = View.VISIBLE

            if (it.imageUri != null) {
                binding.imgDes.setImageURI(it.imageUri.toUri())
                binding.imgDes.visibility = View.VISIBLE
            } else {
                binding.imgDes.visibility = View.GONE
            }

            imagePath = it.imageUri
            selectedTag = it.category
            selectedTimestamp = it.date
            binding.tvDateTime.text = it.date.toFormattedDateTime()
        }
        if (selectedTimestamp == null) {
            binding.tvDateTime.isEnabled = false
            binding.tvDateTime.text = System.currentTimeMillis().toFormattedDateTime()
        }else{
            binding.tvDateTime.isEnabled = true
        }
    }

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
        binding.btnDatePicker.setOnClickListener {
            showDatePickerDialog()
        }
        binding.btnAddExpense.setOnClickListener {
            if (!isAdding) {
                isAdding = true
                binding.btnAddExpense.isEnabled = false

                itemName = binding.edtInputName.text.toString().trim()
                priceText = binding.edtInputPrice.text.toString().trim()
                note = binding.edtInputNote.text.toString().trim()

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
                    category = selectedTag,
                    itemName = itemName,
                    price = price,
                    imageUri = imagePath,
                    note = note.ifEmpty { null },
                    date = selectedTimestamp ?: System.currentTimeMillis() // Dùng timestamp đã chọn hoặc mặc định
                )

                Log.i("__CR", "insert: $expense")
                viewModel.dispatch(ExpenseUiAction.Add(expense))
            }
        }

        binding.btnUpdateExpense.setOnClickListener {
            if (!isAdding) {
                isAdding = true
                binding.btnUpdateExpense.isEnabled = false

                itemName = binding.edtInputName.text.toString().trim()
                priceText = binding.edtInputPrice.text.toString().trim()
                note = binding.edtInputNote.text.toString().trim()

                if (itemName.isEmpty()) {
                    Log.w("__CR", "Item name is empty")
                    toastMessage("Please enter expense name")
                    isAdding = false
                    binding.btnUpdateExpense.isEnabled = true
                    return@setOnClickListener
                }
                if (priceText.isEmpty()) {
                    toastMessage("Please enter price")
                    isAdding = false
                    binding.btnUpdateExpense.isEnabled = true
                    return@setOnClickListener
                }
                val price = binding.edtInputPrice.getCurrencyValue()
                if (price == null || price < 0) {
                    toastMessage("Invalid price")
                    isAdding = false
                    binding.btnUpdateExpense.isEnabled = true
                    return@setOnClickListener
                }

                this.expense?.let {
                    val updatedExpense = it.copy(
                        category = selectedTag,
                        itemName = itemName,
                        price = price,
                        imageUri = imagePath,
                        note = note.ifEmpty { null },
                        date = selectedTimestamp ?: it.date
                    )
                    Log.i("__CR", "update: $updatedExpense")
                    viewModel.dispatch(ExpenseUiAction.Update(updatedExpense))
                    returnData(Activity.RESULT_OK, KEY_RETURN_DATA, true)
                }
            }
        }
    }

    private fun showDatePickerDialog() {
        val calendar = Calendar.getInstance()
        // Nếu đã có timestamp được chọn trước đó, sử dụng nó
        selectedTimestamp?.let { calendar.timeInMillis = it }

        val datePickerDialog = DatePickerDialog(
            this,
            { _, year, month, dayOfMonth ->
                calendar.set(year, month, dayOfMonth)
                val timePickerDialog = TimePickerDialog(
                    this,
                    { _, hourOfDay, minute ->
                        calendar.set(Calendar.HOUR_OF_DAY, hourOfDay)
                        calendar.set(Calendar.MINUTE, minute)
                        updateDateTimeDisplay(calendar)
                    },
                    calendar.get(Calendar.HOUR_OF_DAY),
                    calendar.get(Calendar.MINUTE),
                    true // Định dạng 24h
                )
                timePickerDialog.show()
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        )
        datePickerDialog.show()
    }

    private fun updateDateTimeDisplay(calendar: Calendar) {
        val sdf = SimpleDateFormat("HH:mm - dd 'thg' M yyyy", Locale("vi", "VN"))
        val formattedDateTime = sdf.format(calendar.time)
        binding.tvDateTime.text = formattedDateTime
        selectedTimestamp = calendar.timeInMillis
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
        const val KEY_EXPENSES_EDIT = "KEY_EXPENSES_EDIT"
        const val KEY_RETURN_DATA = "KEY_RETURN_DATA"
    }
}