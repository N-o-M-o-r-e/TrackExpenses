package com.tstool.trackexpenses.utils.ktx

import android.content.Context
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.net.Uri
import android.util.Log
import android.widget.ImageView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.target.Target
import com.tstool.trackexpenses.R

fun Context.loadFileDirWithGlide(
    viewImage: ImageView,
    bitmap: Bitmap? = null,
    uri: Uri? = null) {
    // Kiểm tra bitmap trước, nếu không có thì dùng Uri
    val loadTarget = bitmap ?: uri

    // Sử dụng Glide để load ảnh linh hoạt từ Bitmap hoặc Uri
    Glide.with(this)
        .load(loadTarget)
        .placeholder(R.drawable.ic_place_holder)
        .error(R.drawable.ic_error)
        .listener(object : com.bumptech.glide.request.RequestListener<Drawable> {
            override fun onLoadFailed(
                e: GlideException?,
                model: Any?,
                target: Target<Drawable>,
                isFirstResource: Boolean
            ): Boolean {
                Log.e("__GLIDE", "onLoadFailed: $e")
                return false
            }

            override fun onResourceReady(
                resource: Drawable,
                model: Any,
                target: Target<Drawable>?,
                dataSource: DataSource,
                isFirstResource: Boolean
            ): Boolean {
                return false
            }
        })
        .into(viewImage)
}