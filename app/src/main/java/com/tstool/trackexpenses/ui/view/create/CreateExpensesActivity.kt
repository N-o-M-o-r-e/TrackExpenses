package com.tstool.trackexpenses.ui.view.create

import android.content.Intent
import android.net.Uri
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts.GetContent
import com.bumptech.glide.Glide
import com.tstool.trackexpenses.data.model.ExpenseTag
import com.tstool.trackexpenses.databinding.ActivityCreateExpensesBinding
import com.tstool.trackexpenses.ui.view.create.adapter.ExpensesTagAdapter
import com.tstool.trackexpenses.ui.view.create.adapter.ListenerExpensesTag
import com.tstool.trackexpenses.utils.base.BaseActivity
import com.tstool.trackexpenses.utils.ktx.CommonObject
import com.tstool.trackexpenses.utils.ktx.deleteImage
import com.tstool.trackexpenses.utils.ktx.loadFileDirWithGlide
import com.tstool.trackexpenses.utils.ktx.saveBitmapToInternalStorage
import com.tstool.trackexpenses.utils.ktx.startCrop
import com.tstool.trackexpenses.utils.ktx.uriToBitmapOptimized
import com.yalantis.ucrop.UCrop

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

        binding.btnImportImage.setOnClickListener {
            pickImage.launch("image/*")
            binding.imgDes.visibility = View.VISIBLE
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        when {
            requestCode == UCrop.REQUEST_CROP && resultCode == RESULT_OK -> {
                val croppedUri = UCrop.getOutput(data!!)
                if (croppedUri == null) {
                    binding.imgDes.visibility = View.GONE
                    return
                }
                croppedUri.let {
                    val bitmap = uriToBitmapOptimized(it)
                    CommonObject.bitmap = bitmap
                    val savedUri = saveBitmapToInternalStorage(bitmap)
                    CommonObject.uriBimap = savedUri

                    Glide.with(this).load(savedUri).into(binding.imgDes)
                    loadFileDirWithGlide(
                        viewImage = binding.imgDes, uri = savedUri
                    )
                    deleteImage(it)
                }
            }

            resultCode == UCrop.RESULT_ERROR -> {
                val cropError = UCrop.getError(data!!)
                toastMessage("Lỗi khi crop ảnh: $cropError")
            }
        }
    }

    private val pickImage = registerForActivityResult(GetContent()) { uri: Uri? ->
        uri?.let { startCrop(it) }
    }
} 