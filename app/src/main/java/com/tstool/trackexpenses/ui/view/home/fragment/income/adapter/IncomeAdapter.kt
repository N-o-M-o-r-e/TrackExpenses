package com.tstool.trackexpenses.ui.view.home.fragment.income.adapter

import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.tstool.trackexpenses.data.room.entity.IncomeEntity
import com.tstool.trackexpenses.databinding.ItemIncomeBinding
import java.text.DecimalFormat

class IncomeAdapter(private val listener: OnListenerIncome) :
    ListAdapter<IncomeEntity, IncomeViewHolder>(IncomeDiffUtils()) {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): IncomeViewHolder {
        return IncomeViewHolder.create(parent, listener)
    }

    override fun onBindViewHolder(holder: IncomeViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

}

class IncomeDiffUtils : DiffUtil.ItemCallback<IncomeEntity>() {
    override fun areItemsTheSame(oldItem: IncomeEntity, newItem: IncomeEntity): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: IncomeEntity, newItem: IncomeEntity): Boolean {
        return oldItem == newItem
    }
}

interface OnListenerIncome {
    fun onClickIncome(income: IncomeEntity)
}

class IncomeViewHolder(
    private val binding: ItemIncomeBinding,
    private val listener: OnListenerIncome
) : RecyclerView.ViewHolder(binding.root) {

    fun bind(income: IncomeEntity) {
        binding.tvTitle.text = income.sourceName
        binding.tvNote.text = income.note
        if (income.imageUri == null) {
            binding.cardView.visibility = View.GONE
        } else {
            binding.cardView.visibility = View.VISIBLE
            income.imageUri?.let {
                binding.imgIncome.setImageURI(Uri.parse(it))
            }

        }
        if (income.note == "") {
            binding.tvNote.visibility = View.GONE
        } else {
            binding.tvNote.visibility = View.VISIBLE
        }

        val formatter = DecimalFormat("#,###")
        binding.tvCost.text = formatter.format(income.amount)

        binding.root.setOnClickListener {
            listener.onClickIncome(income)
        }

        binding.root.setOnClickListener {
            listener.onClickIncome(income)
        }
    }

    companion object {
        fun create(parent: ViewGroup, listener: OnListenerIncome): IncomeViewHolder {
            val binding =
                ItemIncomeBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            return IncomeViewHolder(binding, listener)
        }
    }
}