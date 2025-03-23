package com.tstool.trackexpenses.ui.view.create.income.adapter

import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.tstool.trackexpenses.R
import com.tstool.trackexpenses.data.model.ExpenseTag
import com.tstool.trackexpenses.data.model.IncomeTag
import com.tstool.trackexpenses.databinding.ItemIncomeTagBinding

class IncomeTagAdapter(private val listener: ListenerIncomeTag) :
    ListAdapter<IncomeTag, IncomeTagViewHolder>(IncomeDiffUtil()) {
    private var selectedPosition: Int = 0

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): IncomeTagViewHolder {
        return IncomeTagViewHolder.createViewHolder(parent, listener)
    }

    override fun onBindViewHolder(holder: IncomeTagViewHolder, position: Int) {
        val item = getItem(position)
        holder.bind(item, position == selectedPosition)

        holder.itemView.setOnClickListener {
            val previousPosition = selectedPosition
            selectedPosition = holder.adapterPosition

            notifyItemChanged(previousPosition)
            notifyItemChanged(selectedPosition)
            listener.onClickIncomeTag(item)
        }
    }

    override fun submitList(list: List<IncomeTag>?) {
        super.submitList(list?.toList())
        if (!list.isNullOrEmpty()) {
            notifyItemChanged(selectedPosition)
        }
    }
}

class IncomeDiffUtil : DiffUtil.ItemCallback<IncomeTag>() {
    override fun areItemsTheSame(oldItem: IncomeTag, newItem: IncomeTag): Boolean {
        return oldItem.nameTag == newItem.nameTag
    }

    override fun areContentsTheSame(oldItem: IncomeTag, newItem: IncomeTag): Boolean {
        return oldItem == newItem
    }
}

interface ListenerIncomeTag {
    fun onClickIncomeTag(folder: IncomeTag)
}

class IncomeTagViewHolder(
    private val binding: ItemIncomeTagBinding,
    private val listener: ListenerIncomeTag
) : RecyclerView.ViewHolder(binding.root) {

    fun bind(tag: IncomeTag, isSelect: Boolean) {
        Glide.with(binding.root.context).load(tag.resource).into( binding.imgTag)
        binding.tvTag.text = tag.nameTag

        when(isSelect){
            true -> {
                binding.bgItem.backgroundTintList = ContextCompat.getColorStateList(binding.root.context, R.color.primary_variant)
                binding.imgTag.imageTintList = ContextCompat.getColorStateList(binding.root.context, R.color.color_white)
            }
            false -> {
                binding.bgItem.backgroundTintList = ContextCompat.getColorStateList(binding.root.context, R.color.color_button_text)
                binding.imgTag.imageTintList = ContextCompat.getColorStateList(binding.root.context, R.color.primary_variant)

            }
        }
        binding.root.setOnClickListener {
            listener.onClickIncomeTag(tag)
            Log.i("__Create", "bind: $tag")
        }
    }

    companion object {
        fun createViewHolder(parent: ViewGroup, listener: ListenerIncomeTag): IncomeTagViewHolder {
            return IncomeTagViewHolder(
                ItemIncomeTagBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                ), listener
            )
        }
    }
}

