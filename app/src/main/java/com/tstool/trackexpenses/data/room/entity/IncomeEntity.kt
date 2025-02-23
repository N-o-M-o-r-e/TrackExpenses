package com.tstool.trackexpenses.data.room.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "incomes")
data class IncomeEntity(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id") val id: Int = 0,
    @ColumnInfo(name = "source") val source: String,  // Ví dụ: "Lương chính", "Khoản đầu tư", "Thưởng"
    @ColumnInfo(name = "category") val category: String,  // Tên khoản thu nhập
    @ColumnInfo(name = "amount") val amount: Double,  // Số tiền thu nhập
    @ColumnInfo(name = "image_uri") val imageUri: String?, // Hình ảnh (nếu có)
    @ColumnInfo(name = "note") val note: String?,   // Ghi chú (nếu có)
    @ColumnInfo(name = "date") val date: Long       // Timestamp
)