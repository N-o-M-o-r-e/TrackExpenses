package com.tstool.trackexpenses.ui.view.home.fragment.expenses.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.tstool.trackexpenses.data.room.entity.ExpenseEntity
import com.tstool.trackexpenses.databinding.ItemDateTimeBinding
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

data class DayExpenses(val dayTimestamp: Long, val expenses: List<ExpenseEntity>)

class ExpensesAdapter(private val listener: OnListenerExpenses) :
    ListAdapter<DayExpenses, ExpensesAdapter.ExpensesViewHolder>(DayExpensesDiffUtil()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ExpensesViewHolder {
        val binding = ItemDateTimeBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ExpensesViewHolder(binding, listener)
    }

    override fun onBindViewHolder(holder: ExpensesViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class ExpensesViewHolder(
        private val binding: ItemDateTimeBinding,
        private val listener: OnListenerExpenses
    ) : RecyclerView.ViewHolder(binding.root) {

        private val expensesInDayAdapter = ExpensesInDayAdapter(listener)

        init {
            binding.rcvExpensesInDay.apply {
                layoutManager = LinearLayoutManager(context)
                adapter = expensesInDayAdapter
            }
        }

        fun bind(dayExpenses: DayExpenses) {
            val calendar = Calendar.getInstance().apply { timeInMillis = dayExpenses.dayTimestamp }
            binding.tvDay.text = SimpleDateFormat("dd", Locale.getDefault()).format(calendar.time)
            binding.tvMonth.text = SimpleDateFormat("MMM", Locale.getDefault()).format(calendar.time).lowercase()
            binding.tvYear.text = SimpleDateFormat("yyyy", Locale.getDefault()).format(calendar.time)
            expensesInDayAdapter.submitList(dayExpenses.expenses.reversed())
        }
    }

    class DayExpensesDiffUtil : DiffUtil.ItemCallback<DayExpenses>() {
        override fun areItemsTheSame(oldItem: DayExpenses, newItem: DayExpenses): Boolean {
            return oldItem.dayTimestamp == newItem.dayTimestamp
        }

        override fun areContentsTheSame(oldItem: DayExpenses, newItem: DayExpenses): Boolean {
            return oldItem == newItem
        }
    }
}

interface OnListenerExpenses {
    fun onClickExpense(expense: ExpenseEntity)
}