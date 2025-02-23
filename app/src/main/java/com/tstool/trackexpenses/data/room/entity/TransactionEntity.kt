package com.tstool.trackexpenses.data.room.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "transactions")
data class TransactionEntity(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id") val id: Int = 0,
    @ColumnInfo(name = "amount") val amount: Double,   // Số tiền chuyển khoản
    @ColumnInfo(name = "type") val type: String,     // "send" hoặc "receive"
    @ColumnInfo(name = "description") val description: String, // Nội dung giao dịch
    @ColumnInfo(name = "image_uri") val image_uri: String?, // Hình ảnh hóa đơn (nếu có)
    @ColumnInfo(name = "date") val date: Long        // Timestamp
)