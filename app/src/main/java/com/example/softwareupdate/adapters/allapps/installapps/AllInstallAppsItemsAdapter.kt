package com.example.softwareupdate.adapters.allapps.installapps

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.softwareupdate.adapters.allapps.AllAppsEntity
import com.example.softwareupdate.databinding.ItemInstallAppsLayoutBinding

class AllInstallAppsItemsAdapter(
    private val callback: (AllAppsEntity) -> Unit,
    private var dataList: List<AllAppsEntity?>
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {


    @SuppressLint("NotifyDataSetChanged")
    fun updateVersionsList(mProducts: List<AllAppsEntity?>) {
        dataList = mProducts.sortedBy { it?.appName?.lowercase() }
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val binding = ItemInstallAppsLayoutBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return VersionsItemsViewHolder(binding)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val headerHolder = holder as VersionsItemsViewHolder
        dataList[position]?.let { headerHolder.bindData(it) }
    }

    override fun getItemCount(): Int = dataList.size
    inner class VersionsItemsViewHolder(private val binding: ItemInstallAppsLayoutBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bindData(item: AllAppsEntity) {
            binding.allAppsEntity = item
            binding.executePendingBindings()
            binding.tvVersion.isSelected = true
            itemView.setOnClickListener {
                callback.invoke(item)
            }
        }
    }
}
