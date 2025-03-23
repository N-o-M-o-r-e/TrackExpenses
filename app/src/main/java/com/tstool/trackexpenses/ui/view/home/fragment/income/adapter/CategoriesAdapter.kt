package com.tstool.trackexpenses.ui.view.home.fragment.income.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.tstool.trackexpenses.R
import com.tstool.trackexpenses.data.model.IncomeTag
import com.tstool.trackexpenses.databinding.ItemCategoryBinding

class CategoriesAdapter(val listener: OnListenerTagIncome) :
    ListAdapter<IncomeTag, CategoriesViewHolder>(CategoriesDiffUtils()) {

    private var selectedPosition = 0

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CategoriesViewHolder {
        return CategoriesViewHolder.createViewHolder(parent, listener)
    }

    override fun onBindViewHolder(holder: CategoriesViewHolder, position: Int) {
        val tag = getItem(position)
        val isCheck = position == selectedPosition
        holder.bind(tag, isCheck)

        // Xử lý khi người dùng click để chọn item
        holder.itemView.setOnClickListener {
            val previousPosition = selectedPosition
            selectedPosition = holder.adapterPosition
            notifyItemChanged(previousPosition)
            notifyItemChanged(selectedPosition)
            listener.onClickTag(tag)
        }
    }

    fun getSelectedTag(): IncomeTag? {
        return if (selectedPosition >= 0 && selectedPosition < currentList.size) {
            getItem(selectedPosition)
        } else null
    }
}

class CategoriesDiffUtils : DiffUtil.ItemCallback<IncomeTag>() {
    override fun areItemsTheSame(oldItem: IncomeTag, newItem: IncomeTag): Boolean {
        return oldItem.nameTag == newItem.nameTag
    }

    override fun areContentsTheSame(oldItem: IncomeTag, newItem: IncomeTag): Boolean {
        return oldItem == newItem
    }
}

interface OnListenerTagIncome {
    fun onClickTag(tag: IncomeTag)
}

class CategoriesViewHolder(
    private val listener: OnListenerTagIncome, private val binding: ItemCategoryBinding
) : RecyclerView.ViewHolder(binding.root) {

    fun bind(tag: IncomeTag, isCheck: Boolean) {
        val resources = binding.root.context.resources
        val colorId = if (isCheck) R.color.color_button else R.color.color_button_off
        binding.tvCategory.text = tag.nameTag
        binding.tvCategory.setTextColor(resources.getColor(colorId))
        binding.ivCategory.setImageResource(tag.resource)
        binding.ivCategory.setColorFilter(resources.getColor(colorId))

        binding.root.isSelected = isCheck // Sử dụng thuộc tính isSelected để thay đổi giao diện
        binding.root.setBackgroundResource(if (isCheck) R.drawable.background_category_on else R.drawable.background_category_off)
    }

    companion object {
        fun createViewHolder(
            parent: ViewGroup, listener: OnListenerTagIncome
        ): CategoriesViewHolder {
            val binding =
                ItemCategoryBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            return CategoriesViewHolder(listener, binding)
        }
    }
}