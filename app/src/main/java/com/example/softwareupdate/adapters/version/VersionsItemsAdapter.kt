package com.example.softwareupdate.adapters.version

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import androidx.recyclerview.widget.RecyclerView
import com.example.softwareupdate.databinding.ItemAndroidVersionsBinding

class VersionsItemsAdapter(
    private val callback: (VersionsEntity) -> Unit,
    private var dataList: List<VersionsEntity?>,
    private val adapterCallback: VersionsItemsAdapterCallback

) : RecyclerView.Adapter<RecyclerView.ViewHolder>(), Filterable {
    private var filteredList: List<VersionsEntity?> = dataList

    @SuppressLint("NotifyDataSetChanged")
    fun updateVersionsList(mProducts: List<VersionsEntity?>) {
        dataList = mProducts
        filteredList = mProducts
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val binding = ItemAndroidVersionsBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return VersionsItemsViewHolder(binding)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val headerHolder = holder as VersionsItemsViewHolder
        filteredList[position]?.let { headerHolder.bindData(it) }
    }

    override fun getItemCount(): Int = filteredList.size

    inner class VersionsItemsViewHolder(private val binding: ItemAndroidVersionsBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bindData(item: VersionsEntity) {
            binding.versionEntity = item
            binding.executePendingBindings()
            itemView.setOnClickListener {
                callback.invoke(item)
            }
        }
    }

    override fun getFilter(): Filter {
        return object : Filter() {
            override fun performFiltering(constraint: CharSequence?): FilterResults {
                val filteredResults = if (constraint.isNullOrEmpty()) {
                    dataList
                } else {
                    dataList.filter {
                        it?.versionTitle?.contains(constraint.toString(), ignoreCase = true) == true
                    }
                }
                val results = FilterResults()
                results.values = filteredResults
                return results
            }

            @SuppressLint("NotifyDataSetChanged")
            @Suppress("UNCHECKED_CAST")
            override fun publishResults(constraint: CharSequence?, results: FilterResults?) {
                filteredList = results?.values as List<VersionsEntity?>? ?: emptyList()
                notifyDataSetChanged()
                adapterCallback.onEmptySearchResults(filteredList.isEmpty())

            }
        }
    }

    interface VersionsItemsAdapterCallback {
        fun onEmptySearchResults(isEmpty: Boolean)
    }
}

