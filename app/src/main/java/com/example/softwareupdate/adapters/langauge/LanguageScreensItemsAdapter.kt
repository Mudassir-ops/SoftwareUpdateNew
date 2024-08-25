package com.example.softwareupdate.adapters.langauge

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.softwareupdate.databinding.LanguageItemsLayoutBinding

class LanguageScreensItemsAdapter(
    private val callback: (Int) -> Unit, private var dataList: List<LanguageItemsModel?>
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {


    @SuppressLint("NotifyDataSetChanged")
    fun updateLanguageLists(mProducts: List<LanguageItemsModel?>) {
        dataList = mProducts
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val binding = LanguageItemsLayoutBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return HomeItemsViewHolder(binding)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val headerHolder = holder as HomeItemsViewHolder
        dataList[position]?.let { headerHolder.bindData(it) }
    }

    override fun getItemCount(): Int = dataList.size
    inner class HomeItemsViewHolder(private val binding: LanguageItemsLayoutBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bindData(item: LanguageItemsModel) {
            binding.languageModel = item
            binding.executePendingBindings()
            itemView.setOnClickListener {
                callback.invoke(adapterPosition)
            }
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    fun updateSelectedIndex(position: Int) {
        for (l in dataList.indices) {
            dataList[l]?.isSelected = position == l
        }
        notifyDataSetChanged()
    }
}

