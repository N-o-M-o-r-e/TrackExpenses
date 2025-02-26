package com.tstool.trackexpenses.ui.view.home.fragment.expenses.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.tstool.trackexpenses.data.room.entity.ExpenseEntity
import com.tstool.trackexpenses.databinding.ItemExpensesInDayBinding
import java.text.SimpleDateFormat
import java.util.Locale

class ExpensesInDayAdapter :
    ListAdapter<ExpenseEntity, ExpensesInDayAdapter.ExpensesInDayViewHolder>(ExpenseDiffUtil()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ExpensesInDayViewHolder {
        val binding = ItemExpensesInDayBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ExpensesInDayViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ExpensesInDayViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class ExpensesInDayViewHolder(private val binding: ItemExpensesInDayBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(expense: ExpenseEntity) {
            binding.tvTitleExpenses.text = expense.itemName
            binding.tvNote.text = expense.note ?: ""
            // Format giờ:phút từ date
            val timeFormat = SimpleDateFormat("hh:mm a", Locale.getDefault())
            binding.tvTime.text = timeFormat.format(expense.date)
            // Hiển thị ảnh nếu có
            expense.imageUri?.let {
                binding.imgDes.setImageURI(android.net.Uri.parse(it))
            }
            // imgTag mày tự xử lý, tao để mặc định
            // binding.imgTag.setImageResource(...)
        }
    }

    class ExpenseDiffUtil : DiffUtil.ItemCallback<ExpenseEntity>() {
        override fun areItemsTheSame(oldItem: ExpenseEntity, newItem: ExpenseEntity): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: ExpenseEntity, newItem: ExpenseEntity): Boolean {
            return oldItem == newItem
        }
    }
}