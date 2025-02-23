package com.tstool.trackexpenses.ui.view.create.adapter

import android.view.LayoutInflater.from
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.tstool.trackexpenses.R
import com.tstool.trackexpenses.data.model.ExpenseTag
import com.tstool.trackexpenses.databinding.ItemExpensesTagBinding

class ExpensesTagAdapter(private val listener: ListenerExpensesTag) :
    ListAdapter<ExpenseTag, ExpensesTagViewHolder>(ExpensesTagDiffUtil()) {
    private var selectedPosition: Int = 0

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ExpensesTagViewHolder {
        return ExpensesTagViewHolder.createViewHolder(parent, listener)
    }

    override fun onBindViewHolder(holder: ExpensesTagViewHolder, position: Int) {
        val item = getItem(position)
        holder.bind(item, position == selectedPosition)

        holder.itemView.setOnClickListener {
            val previousPosition = selectedPosition
            selectedPosition = holder.adapterPosition

            notifyItemChanged(previousPosition)
            notifyItemChanged(selectedPosition)
        }
    }

    override fun submitList(list: List<ExpenseTag>?) {
        super.submitList(list?.toList())
        if (!list.isNullOrEmpty()) {
            notifyItemChanged(selectedPosition)
        }
    }
}

class ExpensesTagDiffUtil : DiffUtil.ItemCallback<ExpenseTag>() {
    override fun areItemsTheSame(oldItem: ExpenseTag, newItem: ExpenseTag): Boolean {
        return oldItem.nameTag == newItem.nameTag
    }

    override fun areContentsTheSame(oldItem: ExpenseTag, newItem: ExpenseTag): Boolean {
        return oldItem == newItem
    }

}

interface ListenerExpensesTag {
    fun onClickExpensesTag(folder: ExpenseTag)
}

class ExpensesTagViewHolder(
    private val binding: ItemExpensesTagBinding, private val listener: ListenerExpensesTag
) : RecyclerView.ViewHolder(binding.root) {
    fun bind(tag: ExpenseTag, isSelect: Boolean) {
        Glide.with(binding.root.context).load(tag.resource).into( binding.imgTag)
        binding.tvTag.text = tag.nameTag

        when(isSelect){
            true -> binding.bgItem.backgroundTintList = ContextCompat.getColorStateList(binding.root.context, R.color.primary_variant)
            false -> binding.bgItem.backgroundTintList = ContextCompat.getColorStateList(binding.root.context, R.color.color_button_text)
        }
        binding.root.setOnClickListener {
            listener.onClickExpensesTag(tag)
        }
    }

    companion object {
        fun createViewHolder(
            parent: ViewGroup,
            listener: ListenerExpensesTag
        ): ExpensesTagViewHolder {
            return ExpensesTagViewHolder(
                binding = ItemExpensesTagBinding.inflate(from(parent.context), parent, false),
                listener = listener
            )
        }
    }
}