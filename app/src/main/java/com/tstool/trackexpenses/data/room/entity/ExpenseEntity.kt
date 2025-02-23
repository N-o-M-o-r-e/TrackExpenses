package com.tstool.trackexpenses.data.room.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "expenses")
data class ExpenseEntity(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id") val id: Int = 0,
    @ColumnInfo(name = "category") val category: String,  // Ví dụ: "Mua sắm", "Giáo dục", "Sức khỏe"
    @ColumnInfo(name = "item_name") val itemName: String, // Tên món đồ
    @ColumnInfo(name = "amount") val amount: Double,    // Giá tiền
    @ColumnInfo(name = "image_uri") val imageUri: String?, // Lưu đường dẫn ảnh (nếu có)
    @ColumnInfo(name = "note") val note: String?,     // Ghi chú (nếu có)
    @ColumnInfo(name = "date") val date: Long         // Timestamp
)