package com.example.softwareupdate.adapters.appupdate

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.softwareupdate.data.PackageInfoEntity
import com.example.softwareupdate.databinding.ItemUpdatedAppsLayoutBinding

class UpdatedAppsItemsAdapter(
    private val callback: (PackageInfoEntity) -> Unit,
    private var dataList: List<PackageInfoEntity?>
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {


    @SuppressLint("NotifyDataSetChanged")
    fun updateVersionsList(mProducts: List<PackageInfoEntity?>) {
        dataList = mProducts.sortedBy { it?.appName?.lowercase() }
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val binding = ItemUpdatedAppsLayoutBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return VersionsItemsViewHolder(binding)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val headerHolder = holder as VersionsItemsViewHolder
        dataList[position]?.let { headerHolder.bindData(it) }
    }

    override fun getItemCount(): Int = dataList.size
    inner class VersionsItemsViewHolder(private val binding: ItemUpdatedAppsLayoutBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bindData(item: PackageInfoEntity) {
            binding.updatedAppsEntity = item
            binding.executePendingBindings()
            binding.tvVersion.isSelected = true
            itemView.setOnClickListener {
                callback.invoke(item)
            }
        }
    }
}
