package com.tstool.trackexpenses.data.room.entity

import android.os.Parcelable
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.android.parcel.Parcelize
@Parcelize
@Entity(tableName = "expenses_db")
data class ExpenseEntity(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id") val id: Int,
    @ColumnInfo(name = "category") val category: String,
    @ColumnInfo(name = "item_name") val itemName: String,
    @ColumnInfo(name = "price") val price: Double,
    @ColumnInfo(name = "image_uri") val imageUri: String? = null,
    @ColumnInfo(name = "note") val note: String? = null,
    @ColumnInfo(name = "date") val date: Long
) : Parcelable