package com.tstool.trackexpenses.utils.ktx

import android.app.Activity
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import com.yalantis.ucrop.UCrop
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale


fun Activity.getImageDirectory(): File {
    val directory = File(filesDir, "images")
    if (!directory.exists()) {
        directory.mkdirs() // Tạo thư mục nếu chưa có
    }
    return directory
}

fun generateFileName(prefix: String = "IMG"): String {
    val timeStamp: String = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
    return prefix + "_" + "$timeStamp.jpg"
}

fun Activity.startCrop(uri: Uri) {

    val fileName = generateFileName("CROP") // Đặt tên file cho ảnh đã crop
    val destinationUri = Uri.fromFile(File(getImageDirectory(), fileName))

    UCrop.of(uri, destinationUri).withAspectRatio(1f, 1f) // Tỉ lệ 1:1 (có thể thay đổi)
        .withMaxResultSize(1080, 1080).start(this)
}

fun Activity.saveBitmapToInternalStorage(bitmap: Bitmap): Uri {
    val fileName = generateFileName("IMG") // Đặt tên file cho ảnh lưu
    val file = File(getImageDirectory(), fileName)

    try {
        FileOutputStream(file).use { out ->
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
        }
    } catch (e: IOException) {
        e.printStackTrace()
    }

    return Uri.fromFile(file)
}

fun Activity.deleteImage(uri: Uri): Boolean {
    val file = File(uri.path ?: return false)
    return if (file.exists()) {
        file.delete()
    } else {
        false
    }
}

fun Activity.uriToBitmapOptimized(uri: Uri): Bitmap {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
        val source = ImageDecoder.createSource(contentResolver, uri)
        ImageDecoder.decodeBitmap(source) { decoder, _, _ ->
            decoder.isMutableRequired = true
        }
    } else {
        val options = BitmapFactory.Options().apply {
            inJustDecodeBounds = true
            BitmapFactory.decodeStream(contentResolver.openInputStream(uri), null, this)

            // Giảm kích thước ảnh nếu quá lớn
            inSampleSize = calculateInSampleSize(this, 1080, 1080)
            inJustDecodeBounds = false
        }
        BitmapFactory.decodeStream(contentResolver.openInputStream(uri), null, options)!!
    }
}

fun calculateInSampleSize(options: BitmapFactory.Options, reqWidth: Int, reqHeight: Int): Int {
    val (height: Int, width: Int) = options.outHeight to options.outWidth
    var inSampleSize = 1

    if (height > reqHeight || width > reqWidth) {
        val halfHeight: Int = height / 2
        val halfWidth: Int = width / 2

        while (halfHeight / inSampleSize >= reqHeight && halfWidth / inSampleSize >= reqWidth) {
            inSampleSize *= 2
        }
    }
    return inSampleSize
}
