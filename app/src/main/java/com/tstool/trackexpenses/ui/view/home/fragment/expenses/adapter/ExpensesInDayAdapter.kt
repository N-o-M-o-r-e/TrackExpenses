package com.tstool.trackexpenses.ui.view.home.fragment.expenses.adapter

import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.tstool.trackexpenses.data.room.entity.ExpenseEntity
import com.tstool.trackexpenses.databinding.ItemExpensesInDayBinding
import java.text.DecimalFormat
import java.text.SimpleDateFormat
import java.util.Locale

class ExpensesInDayAdapter(private val listener: OnListenerExpenses) :
    ListAdapter<ExpenseEntity, ExpensesInDayAdapter.ExpensesInDayViewHolder>(ExpenseDiffUtil()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ExpensesInDayViewHolder {
        val binding = ItemExpensesInDayBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ExpensesInDayViewHolder(binding, listener)
    }

    override fun onBindViewHolder(holder: ExpensesInDayViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class ExpensesInDayViewHolder(
        private val binding: ItemExpensesInDayBinding,
        private val listener: OnListenerExpenses
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(expense: ExpenseEntity) {
            binding.tvTitleExpenses.text = expense.itemName
            binding.tvNote.text = expense.note ?: ""
            val timeFormat = SimpleDateFormat("hh:mm a", Locale.getDefault())
            binding.tvTime.text = timeFormat.format(expense.date)
            if (expense.imageUri==null){
                binding.cardView.visibility = View.INVISIBLE
            }else{
                binding.cardView.visibility = View.VISIBLE
            }
            expense.imageUri?.let {
                binding.imgDes.setImageURI(Uri.parse(it))
            }
            val totalCost = expense.price
            val formatter = DecimalFormat("#,###")
            binding.tvCost.text = formatter.format(totalCost)

            // Gán sự kiện click
            binding.root.setOnClickListener {
                listener.onClickExpense(expense)
            }
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