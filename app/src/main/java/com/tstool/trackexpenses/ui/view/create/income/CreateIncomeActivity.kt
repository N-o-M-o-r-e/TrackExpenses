package com.tstool.trackexpenses.ui.view.create.income

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.util.Log
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.net.toUri
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import com.tstool.trackexpenses.data.model.IncomeTag
import com.tstool.trackexpenses.data.room.entity.IncomeEntity
import com.tstool.trackexpenses.databinding.ActivityCreateIncomeBinding
import com.tstool.trackexpenses.ui.view.create.MonthPickerHelper
import com.tstool.trackexpenses.ui.view.create.income.adapter.IncomeTagAdapter
import com.tstool.trackexpenses.ui.view.create.income.adapter.ListenerIncomeTag
import com.tstool.trackexpenses.ui.view.viewmodel.IncomeEvent
import com.tstool.trackexpenses.ui.view.viewmodel.IncomeUiAction
import com.tstool.trackexpenses.ui.view.viewmodel.IncomeViewModel
import com.tstool.trackexpenses.utils.base.BaseActivity
import com.tstool.trackexpenses.utils.ktx.formatCurrencyInput
import com.tstool.trackexpenses.utils.ktx.getCurrencyValue
import com.tstool.trackexpenses.utils.ktx.toCurrencyString
import com.yalantis.ucrop.UCrop
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class CreateIncomeActivity :
    BaseActivity<ActivityCreateIncomeBinding>(ActivityCreateIncomeBinding::inflate) {

    private val viewModel by viewModel<IncomeViewModel>()
    private lateinit var adapter: IncomeTagAdapter
    private val listTag = IncomeTag.entries
    private var selectedTag: String = IncomeTag.entries[0].nameTag
    private var imagePath: String? = null
    private var isAdding = false

    private lateinit var itemName: String
    private lateinit var priceText: String
    private lateinit var note: String

    private var income: IncomeEntity? = null
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
                    IncomeEvent.IncomeAdded -> {
                        onBackPressed()
                    }

                    IncomeEvent.IncomeUpdated -> {
                        returnData(Activity.RESULT_OK, KEY_RETURN_DATA, true)
                        onBackPressed()
                    }

                    IncomeEvent.IncomeDeleted -> {}
                    IncomeEvent.IncomeFiltered -> {

                    }
                    IncomeEvent.IncomeSearched -> {}
                    is IncomeEvent.ShowToast -> {
                        toastMessage(event.message)
                    }

                    is IncomeEvent.ShowAmounts -> {}
                }
            }
        }
    }

    override fun initData() {
        this.income = getIntentData(KEY_INCOME_EDIT, null)
        if (income == null) {
            binding.tvTitleBar.text = "Create Income"
            binding.btnAddIncome.visibility = View.VISIBLE
            binding.btnUpdateIncome.visibility = View.GONE
            selectedTimestamp = System.currentTimeMillis()

        } else {
            binding.tvTitleBar.text = "Update Income"
            binding.btnAddIncome.visibility = View.GONE
            binding.btnUpdateIncome.visibility = View.VISIBLE
        }
        income?.let {
            binding.edtInputName.setText(it.sourceName)
            binding.edtInputPrice.setText(it.amount.toCurrencyString())
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
            binding.tvDateTime.text = formatMonthYear(it.date)
        }
        if (selectedTimestamp == null) {
            binding.tvDateTime.isEnabled = false
            binding.tvDateTime.text = formatMonthYear(System.currentTimeMillis())
        } else {
            binding.tvDateTime.isEnabled = true
        }
    }

    override fun initView() {
        binding.edtInputPrice.formatCurrencyInput()
        adapter = IncomeTagAdapter(object : ListenerIncomeTag {
            override fun onClickIncomeTag(folder: IncomeTag) {
                selectedTag = folder.nameTag
            }
        })
        binding.rcvIncomeTag.adapter = adapter
        adapter.submitList(listTag)
    }

    override fun initAction() {
        binding.btnBack.setOnClickListener { onBackPressedDispatcher.onBackPressed() }
        binding.btnQuestion.setOnClickListener { toastMessage("Show") }
        binding.btnImportImage.setOnClickListener { pickImageLauncher.launch("image/*") }
        binding.btnDatePicker.setOnClickListener {
            MonthPickerHelper.showMonthPicker(this) { startOfMonth, _ ->
                selectedTimestamp = startOfMonth
                binding.tvDateTime.text = formatMonthYear(startOfMonth)
            }
        }
        binding.btnAddIncome.setOnClickListener {
            if (!isAdding) {
                isAdding = true
                binding.btnAddIncome.isEnabled = false

                itemName = binding.edtInputName.text.toString().trim()
                priceText = binding.edtInputPrice.text.toString().trim()
                note = binding.edtInputNote.text.toString().trim()

                if (itemName.isEmpty()) {
                    Log.w(TAG, "Item name is empty")
                    toastMessage("Please enter income name")
                    isAdding = false
                    binding.btnAddIncome.isEnabled = true
                    return@setOnClickListener
                }
                if (priceText.isEmpty()) {
                    toastMessage("Please enter amount")
                    isAdding = false
                    binding.btnAddIncome.isEnabled = true
                    return@setOnClickListener
                }
                val amount = binding.edtInputPrice.getCurrencyValue()
                if (amount == null || amount < 0) {
                    toastMessage("Invalid amount")
                    isAdding = false
                    binding.btnAddIncome.isEnabled = true
                    return@setOnClickListener
                }

                val income = IncomeEntity(
                    id = 0,
                    category = selectedTag,
                    sourceName = itemName,
                    amount = amount,
                    imageUri = imagePath,
                    note = note.ifEmpty { null },
                    date = selectedTimestamp ?: System.currentTimeMillis()
                )

                Log.i(TAG, "insert: $income")
                viewModel.dispatch(IncomeUiAction.Add(income))
            }
        }

        binding.btnUpdateIncome.setOnClickListener {
            if (!isAdding) {
                isAdding = true
                binding.btnUpdateIncome.isEnabled = false

                itemName = binding.edtInputName.text.toString().trim()
                priceText = binding.edtInputPrice.text.toString().trim()
                note = binding.edtInputNote.text.toString().trim()

                if (itemName.isEmpty()) {
                    Log.w(TAG, "Item name is empty")
                    toastMessage("Please enter income name")
                    isAdding = false
                    binding.btnUpdateIncome.isEnabled = true
                    return@setOnClickListener
                }
                if (priceText.isEmpty()) {
                    toastMessage("Please enter amount")
                    isAdding = false
                    binding.btnUpdateIncome.isEnabled = true
                    return@setOnClickListener
                }
                val amount = binding.edtInputPrice.getCurrencyValue()
                if (amount == null || amount < 0) {
                    toastMessage("Invalid amount")
                    isAdding = false
                    binding.btnUpdateIncome.isEnabled = true
                    return@setOnClickListener
                }

                this.income?.let {
                    val updatedIncome = it.copy(
                        category = selectedTag,
                        sourceName = itemName,
                        amount = amount,
                        imageUri = imagePath,
                        note = note.ifEmpty { null },
                        date = selectedTimestamp ?: it.date
                    )
                    Log.i(TAG, "update: $updatedIncome")
                    viewModel.dispatch(IncomeUiAction.Update(updatedIncome))
                }
            }
        }
    }

    private fun formatMonthYear(timestamp: Long): String {
        val sdf = SimpleDateFormat("MM/yyyy", Locale("vi", "VN"))
        return sdf.format(Date(timestamp))
    }

    private val pickImageLauncher =
        registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
            uri?.let { startCrop(it) }
        }

    private fun startCrop(sourceUri: Uri) {
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val destinationFileName = "Income_$timeStamp.jpg"
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
                Log.e(TAG, "Crop error: ${cropError?.message}")
                toastMessage("Crop error: ${cropError?.message}")
            }
        }
    }

    private fun saveImageToInternalStorage(uri: Uri): String? {
        return try {
            val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
            val fileName = "Income_$timeStamp.jpg"
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

    companion object {
        const val TAG = "__CR_INCOME"
        const val KEY_INCOME_EDIT = "KEY_INCOME_EDIT"
        const val KEY_RETURN_DATA = "KEY_RETURN_DATA"
    }
}