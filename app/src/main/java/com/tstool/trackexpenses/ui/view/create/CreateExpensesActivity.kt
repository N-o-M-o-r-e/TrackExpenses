package com.tstool.trackexpenses.ui.view.create

import android.content.Intent
import android.net.Uri
import android.util.Log
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.lifecycle.lifecycleScope
import com.tstool.trackexpenses.data.model.ExpenseTag
import com.tstool.trackexpenses.data.room.entity.ExpenseEntity
import com.tstool.trackexpenses.databinding.ActivityCreateExpensesBinding
import com.tstool.trackexpenses.ui.view.create.adapter.ExpensesTagAdapter
import com.tstool.trackexpenses.ui.view.create.adapter.ListenerExpensesTag
import com.tstool.trackexpenses.ui.view.viewmodel.ExpenseAction
import com.tstool.trackexpenses.ui.view.viewmodel.ExpenseEvent
import com.tstool.trackexpenses.ui.view.viewmodel.ExpenseViewModel
import com.tstool.trackexpenses.utils.base.BaseActivity

import com.yalantis.ucrop.UCrop
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class CreateExpensesActivity :
    BaseActivity<ActivityCreateExpensesBinding>(ActivityCreateExpensesBinding::inflate) {

    private val viewModel: ExpenseViewModel by viewModel()
    private lateinit var adapter: ExpensesTagAdapter
    private val listTag = ExpenseTag.entries
    private var selectedTag: String = ExpenseTag.entries[0].nameTag
    private var imagePath: String? = null
    private var isAdding = false // Biến kiểm soát trạng thái thêm

    private val pickImageLauncher =
        registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
            uri?.let { startCrop(it) }
        }

    override fun initAds() {}

    override fun initViewModel() {
        lifecycleScope.launch {
            viewModel.eventFlow.collectLatest { event ->
                when (event) {
                    is ExpenseEvent.ShowSuccess -> {
                        Log.d("CreateExpenses", "Success: ${event.message}")
                        toastMessage(event.message)
                        finish()
                    }

                    is ExpenseEvent.ShowError -> {
                        Log.e("CreateExpenses", "Error: ${event.message}")
                        toastMessage(event.message)
                        isAdding = false // Reset trạng thái nếu lỗi
                        binding.btnAddExpense.isEnabled = true
                    }

                    else -> {}
                }
            }
        }
    }

    override fun initData() {
        adapter = ExpensesTagAdapter(object : ListenerExpensesTag {
            override fun onClickExpensesTag(folder: ExpenseTag) {
                selectedTag = folder.nameTag
                Log.i("CreateExpenses", "Selected tag: $selectedTag")
            }
        })
        binding.rcvExpensesTag.adapter = adapter
        adapter.submitList(listTag)
    }

    override fun initView() {}

    override fun initAction() {
        binding.btnBack.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        binding.btnQuestion.setOnClickListener {
            toastMessage("Show")
        }

        binding.btnImportImage.setOnClickListener {
            pickImageLauncher.launch("image/*")
        }

        binding.btnAddExpense.setOnClickListener {
            if (!isAdding) { // Chỉ cho phép thêm khi không đang xử lý
                Log.i("CreateExpenses", "Add expense button clicked")
                isAdding = true
                binding.btnAddExpense.isEnabled = false // Disable nút để tránh nhấn nhiều lần
                addExpense()
            }
        }
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
                        Log.i("CreateExpenses", "Image saved at: $imagePath")
                        binding.imgDes.setImageURI(Uri.parse(imagePath))
                        binding.imgDes.visibility = View.VISIBLE
                    } else {
                        Log.e("CreateExpenses", "Failed to save image")
                        toastMessage("Failed to save image")
                    }
                }
            }
            resultCode == UCrop.RESULT_ERROR -> {
                val cropError = data?.let { UCrop.getError(it) }
                Log.e("CreateExpenses", "Crop error: ${cropError?.message}")
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

    private fun addExpense() {
        val itemName = binding.edtInputName.text.toString().trim()
        val priceText = binding.edtInputPrice.text.toString().trim()
        val note = binding.edtInputNote.text.toString().trim()
        val category = selectedTag

        Log.i(
            "CreateExpenses",
            "Adding expense - Category: $category, Item: $itemName, Price: $priceText, Note: $note, Image: $imagePath"
        )

        if (itemName.isEmpty()) {
            Log.w("CreateExpenses", "Item name is empty")
            toastMessage("Please enter expense name")
            resetAddState()
            return
        }
        if (priceText.isEmpty()) {
            Log.w("CreateExpenses", "Price is empty")
            toastMessage("Please enter price")
            resetAddState()
            return
        }

        val price = priceText.toDoubleOrNull()
        if (price == null || price < 0) {
            Log.w("CreateExpenses", "Invalid price: $priceText")
            toastMessage("Invalid price")
            resetAddState()
            return
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

        Log.i("CreateExpenses", "Dispatching expense: $expense")
        viewModel.dispatch(ExpenseAction.Add(expense))
    }

    private fun resetAddState() {
        isAdding = false
        binding.btnAddExpense.isEnabled = true
    }
}