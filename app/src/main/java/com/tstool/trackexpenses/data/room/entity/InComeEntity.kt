package com.tstool.trackexpenses.data.room.entity

import android.os.Parcelable
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.android.parcel.Parcelize

@Parcelize
@Entity(tableName = "income_db")
data class IncomeEntity(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id") val id: Int,
    @ColumnInfo(name = "category") val category: String,         // Phân loại thu nhập (ví dụ: lương, đầu tư,...)
    @ColumnInfo(name = "source_name") val sourceName: String,   // Tên nguồn thu nhập (ví dụ: lương tháng 3, cổ tức,...)
    @ColumnInfo(name = "amount") val amount: Double,            // Số tiền thu nhập
    @ColumnInfo(name = "image_uri") val imageUri: String? = null, // URI của ảnh liên quan (nếu có)
    @ColumnInfo(name = "note") val note: String? = null,        // Ghi chú (nếu có)
    @ColumnInfo(name = "date") val date: Long                  // Ngày nhận thu nhập
) : Parcelable